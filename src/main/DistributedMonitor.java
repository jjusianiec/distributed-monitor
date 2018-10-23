import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;

import model.CriticalSectionRequest;
import model.DistributedMonitorConfiguration;
import model.Message;
import model.NodeIdWithTimestamp;
import service.ReceivingService;
import service.SendingService;

import static com.google.common.collect.Lists.newArrayList;
import static model.CriticalSectionRequestType.REQUEST;
import static org.slf4j.LoggerFactory.getLogger;

public class DistributedMonitor<T extends Message> {
	public static final int RECEIVED_CRITICAL_SECTION_RESPONSES_REFRESHES_INTERVAL_MILLIS = 100;
	private DistributedMonitorConfiguration<T> configuration;
	private static final Logger LOGGER = getLogger(DistributedMonitor.class);
	private final Lock lock = new ReentrantLock(true);
	private final ReceivingService receivingService;
	private final SendingService sendingService;
	private final List<NodeIdWithTimestamp> criticalSectionQueue = newArrayList();
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
		LOGGER.info("waitUntil");
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
			new String(body, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Incoming message parsing error", e);
		} finally {
			lock.unlock();
		}
	}

	private void acquireDistributedLock() {
		lock.lock();
		try {
			currentTimestamp++;
			criticalSectionQueue.add(NodeIdWithTimestamp.builder().timestamp(currentTimestamp)
					.nodeId(configuration.getNodeId()).build());
			sendingService.send(createCriticalSectionRequest().encode());

			receivedCriticalSectionResponses = 0;
			while (receivedCriticalSectionResponses < configuration.getNodeCount()) {
				lock.unlock();
				silentSleep(RECEIVED_CRITICAL_SECTION_RESPONSES_REFRESHES_INTERVAL_MILLIS);
				lock.lock();
			}

		} finally {
			lock.unlock();
		}
	}

	private void silentSleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			LOGGER.error("Silent sleep error", e);
		}
	}

	private void releaseDistributedLock() {

	}

	private CriticalSectionRequest createCriticalSectionRequest() {
		return CriticalSectionRequest.builder().nodeIdWithTimestamp(
				NodeIdWithTimestamp.builder().nodeId(configuration.getNodeId())
						.timestamp(currentTimestamp).build()).type(REQUEST).build();
	}
}
