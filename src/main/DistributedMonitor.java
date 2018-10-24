import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import model.CriticalSectionRequest;
import model.CriticalSectionRequestType;
import model.DistributedMonitorConfiguration;
import model.MonitorMessage;
import model.NodeIdWithTimestamp;
import service.MessageSerializationService;
import service.NodeIdWithTimestampComparator;
import service.ReceivingService;
import service.SendingService;

import static com.google.common.collect.Lists.newArrayList;
import static model.CriticalSectionRequestType.RELEASE;
import static model.CriticalSectionRequestType.REQUEST;
import static model.CriticalSectionRequestType.RESPONSE;
import static org.slf4j.LoggerFactory.getLogger;
import static service.LamportClockUtils.getNewTimestamp;

public class DistributedMonitor<T> {
	public static final int WAITING_TO_RECEIVE_ALL_CRITICAL_SECTION_RESPONSES_SLEEP_MILLIS = 100;
	public static final Integer ALL_NODES = null;
	public static final int WAITING_TO_BE_FIRST_IN_QUEUE_SLEEP_MILLIS = 100;
	public static final String SHARED_OBJECT_SYNCHRONIZATION = "SharedObjectSynchronization";
	private DistributedMonitorConfiguration<T> configuration;
	private static final Logger LOGGER = getLogger(DistributedMonitor.class);
	private final Lock lock = new ReentrantLock(true);
	private final ReceivingService receivingService;
	private final SendingService sendingService;
	private List<NodeIdWithTimestamp> criticalSectionQueue = newArrayList();
	private long currentTimestamp = 0;
	private int receivedCriticalSectionResponses = 0;

	public DistributedMonitor(DistributedMonitorConfiguration<T> configuration) {
		this.configuration = configuration;
		receivingService = new ReceivingService(configuration.getMonitorId(),
				configuration.getNodeId());
		sendingService = new SendingService(configuration.getMonitorId(),
				configuration.getNodeId());
		receivingService.subscribe(this::handleNewMessage);
	}

	public void synchronize(Runnable r) {
		acquireDistributedLock();
		try {
			r.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			releaseDistributedLock();
		}
	}

	public void signal(String condition) {
		LOGGER.info("signal");
	}

	public void waitUntil(String condition) {
		//		LOGGER.info("waitUntil");
	}

	public T getSharedModel() {
		return configuration.getSharedObject();
	}

	public DistributedMonitorConfiguration<T> getConfiguration() {
		return configuration;
	}

	private void handleNewMessage(byte[] body) {
		try {
			lock.lock();

			// TODO: implement handling requests, implement message which will have type and body fileds
			MonitorMessage message = MessageSerializationService
					.decode(new String(body, "UTF-8"), MonitorMessage.class);
			if (isMessageInvalid(message)) {
				return;
			}
			switch (message.getType()) {
			case "CriticalSectionRequest":
				CriticalSectionRequest criticalSectionRequest = MessageSerializationService
						.decode(message.getMessage(), CriticalSectionRequest.class);
				handleCriticalSectionRequest(criticalSectionRequest, message);
				break;

			case SHARED_OBJECT_SYNCHRONIZATION:
				currentTimestamp = getNewTimestamp(currentTimestamp, message);
				configuration.setSharedObject(getSharedObject(message));
				break;
			}
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Incoming message parsing error", e);
		} finally {
			lock.unlock();
		}
	}

	private T getSharedObject(MonitorMessage message) {
		return configuration.getSharedObjectSerialization().decode(message.getMessage());
	}

	private String getEncodedSharedObject() {
		return configuration.getSharedObjectSerialization().encode(configuration.getSharedObject());
	}

	private void handleCriticalSectionRequest(CriticalSectionRequest criticalSectionRequest,
			MonitorMessage message) {
		switch (criticalSectionRequest.getType()) {
		case REQUEST:
			criticalSectionQueue.add(message.getNodeIdWithTimestamp());
			currentTimestamp = getNewTimestamp(currentTimestamp, message);
			currentTimestamp++;
			sendingService.send(MessageSerializationService
					.encode(createCriticalSectionRequest(RESPONSE,
							message.getNodeIdWithTimestamp().getNodeId())));
			break;
		case RESPONSE:
			receivedCriticalSectionResponses++;
			currentTimestamp = getNewTimestamp(currentTimestamp, message);
			break;
		case RELEASE:
			removeNodeFromCriticalSectionQueue(message.getNodeIdWithTimestamp().getNodeId());
			break;
		}

	}

	private void removeNodeFromCriticalSectionQueue(Integer nodeId) {
		criticalSectionQueue = criticalSectionQueue.stream()
				.filter(o -> !o.getNodeId().equals(nodeId)).collect(Collectors.toList());
	}

	private void acquireDistributedLock() {
		lock.lock();
		try {
			currentTimestamp++;
			criticalSectionQueue.add(getActualNodeIdWithTimestamp());
			sendingService.send(MessageSerializationService
					.encode(createCriticalSectionRequest(REQUEST, ALL_NODES)));

			receivedCriticalSectionResponses = 0;
			while (receivedCriticalSectionResponses < configuration.getNodeCount() - 1) {
				lock.unlock();
				sleepQuietly(WAITING_TO_RECEIVE_ALL_CRITICAL_SECTION_RESPONSES_SLEEP_MILLIS);
				lock.lock();
			}
			receivedCriticalSectionResponses = 0;

			criticalSectionQueue.sort(NodeIdWithTimestampComparator.INSTANCE);
			while (!configuration.getNodeId().equals(criticalSectionQueue.get(0).getNodeId())) {
				criticalSectionQueue.sort(NodeIdWithTimestampComparator.INSTANCE);
				lock.unlock();
				sleepQuietly(WAITING_TO_BE_FIRST_IN_QUEUE_SLEEP_MILLIS);
				lock.lock();
			}

			LOGGER.info("enters CS");

		} finally {
			lock.unlock();
		}
	}

	private void releaseDistributedLock() {
		lock.lock();
		try {
			currentTimestamp++;
			MonitorMessage message = createMonitorMessage(SHARED_OBJECT_SYNCHRONIZATION,
					getEncodedSharedObject(), ALL_NODES);
			sendingService.send(MessageSerializationService.encode(message));

			removeNodeFromCriticalSectionQueue(configuration.getNodeId());
			sendingService.send(MessageSerializationService
					.encode(createCriticalSectionRequest(RELEASE, ALL_NODES)));
		} finally {
			lock.unlock();
		}
	}

	private NodeIdWithTimestamp getActualNodeIdWithTimestamp() {
		return NodeIdWithTimestamp.builder().timestamp(currentTimestamp)
				.nodeId(configuration.getNodeId()).build();
	}

	private void sleepQuietly(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			LOGGER.error("Silent sleep error", e);
		}
	}

	private MonitorMessage createCriticalSectionRequest(CriticalSectionRequestType type,
			Integer recipientNodeId) {
		CriticalSectionRequest criticalSectionRequest = CriticalSectionRequest.builder().type(type)
				.build();
		String encodedMessage = MessageSerializationService.encode(criticalSectionRequest);
		return createMonitorMessage(criticalSectionRequest.getClass().getSimpleName(),
				encodedMessage, recipientNodeId);
	}

	private MonitorMessage createMonitorMessage(String type, String encode,
			Integer recipientNodeId) {
		NodeIdWithTimestamp nodeIdWithTimestamp = getActualNodeIdWithTimestamp();
		return MonitorMessage.builder().type(type).message(encode)
				.runInstanceId(configuration.getRunInstanceId())
				.nodeIdWithTimestamp(nodeIdWithTimestamp).recipientNodeId(recipientNodeId).build();
	}

	private boolean isMessageInvalid(MonitorMessage message) {
		if (!configuration.getRunInstanceId().equals(message.getRunInstanceId())) {
			LOGGER.info("Received messages from previous application run, omitting");
			return true;
		}
		if (message.getRecipientNodeId() != null && !message.getRecipientNodeId()
				.equals(configuration.getNodeId())) {
			return true;
		}
		if (configuration.getNodeId().equals(message.getNodeIdWithTimestamp().getNodeId())) {
			return true;
		}
		return false;
	}
}
