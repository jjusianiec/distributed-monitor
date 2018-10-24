import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import model.ConsumerProducerSharedModel;
import model.ConsumerProducerSharedModelSerializationImpl;
import model.DistributedMonitorConfiguration;

public class DistributedMonitorRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(DistributedMonitorRunner.class);

	public static final int CONSUMER_COUNT = 5;
	public static final int PRODUCER_COUNT = 1;
	public static final int NODE_COUNT = CONSUMER_COUNT + PRODUCER_COUNT;
	public static final String CONSUMER_PRODUCER = "consumer-producer";
	public static final String EMPTY = "empty";
	public static final String FULL = "full";
	public static final HashSet<String> CONDITIONS = Sets.newHashSet(EMPTY, FULL);
	public static final int BUFFER_SIZE = 10;
	public static final int TIME_DELAY = 500;
	public static final UUID RUN_INSTANCE_ID = UUID.randomUUID();

	public static void main(String[] args) {
		LOGGER.error(RUN_INSTANCE_ID.toString());
		List<Thread> consumers = Lists.newArrayList();

		for (int i = 0; i < CONSUMER_COUNT; i++) {
			DistributedMonitorConfiguration configuration = getConfiguration(i);
			consumers.add(new Thread(() -> {
				DistributedMonitor monitor = new DistributedMonitor<ConsumerProducerSharedModel>(
						configuration);
				while (true) {
					consume(monitor);
				}
			}));
		}

		List<Thread> producers = Lists.newArrayList();
		for (int i = 0; i < PRODUCER_COUNT; i++) {
			DistributedMonitorConfiguration configuration = getConfiguration(CONSUMER_COUNT + 1);
			producers.add(new Thread(() -> {
				DistributedMonitor monitor = new DistributedMonitor<ConsumerProducerSharedModel>(
						configuration);
				while (true) {
					produce(monitor);
				}
			}));
		}

		producers.forEach(Thread::start);
		consumers.forEach(Thread::start);
	}

	private static void produce(DistributedMonitor monitor) {
		monitor.synchronize(() -> {
			ConsumerProducerSharedModel sharedModel = (ConsumerProducerSharedModel) monitor
					.getSharedModel();
			while (sharedModel.getBuffer().size() >= sharedModel.getSize()) {
				monitor.waitUntil(FULL);
			}

			sleepQuietly(ThreadLocalRandom.current().nextInt(TIME_DELAY));
			int producedElement = ThreadLocalRandom.current().nextInt(10_000);
			long threadId = Thread.currentThread().getId();
			sharedModel.getBuffer().add(producedElement);
			LOGGER.info("Thread " + threadId + " host" + monitor.getConfiguration().getNodeId()
					+ " produced " + producedElement);

			monitor.signal(EMPTY);
		});
	}

	private static void consume(DistributedMonitor monitor) {
		monitor.synchronize(() -> {
			ConsumerProducerSharedModel sharedModel = (ConsumerProducerSharedModel) monitor
					.getSharedModel();
			while (sharedModel.getBuffer().isEmpty()) {
				monitor.waitUntil(EMPTY);
			}

			int delay = ThreadLocalRandom.current().nextInt(TIME_DELAY);
			sleepQuietly(delay);
			long threadId = Thread.currentThread().getId();
			Integer element = sharedModel.getBuffer().remove();
			LOGGER.info("Thread " + threadId + " host " + monitor.getConfiguration().getNodeId()
					+ " consumed " + element);
			monitor.signal(FULL);
		});

	}

	private static void sleepQuietly(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			LOGGER.error("Sleep error", e);
		}
	}

	private static DistributedMonitorConfiguration<ConsumerProducerSharedModel> getConfiguration(
			int nodeId) {
		DistributedMonitorConfiguration<ConsumerProducerSharedModel> configuration = new DistributedMonitorConfiguration<>();
		configuration.setNodeId(nodeId);
		configuration.setSharedObject(new ConsumerProducerSharedModel(BUFFER_SIZE, Queues.newArrayDeque()));
		configuration.setSharedObjectSerialization(new ConsumerProducerSharedModelSerializationImpl());
		configuration.setConditions(CONDITIONS);
		configuration.setNodeCount(NODE_COUNT);
		configuration.setRunInstanceId(RUN_INSTANCE_ID);
		configuration.setMonitorId(CONSUMER_PRODUCER);
		return configuration;
	}
}
