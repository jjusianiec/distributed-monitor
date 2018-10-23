import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeoutException;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class DistributedMonitorRunner {
	public static final int CONSUMER_COUNT = 5;
	public static final int PRODUCER_COUNT = 1;
	public static final String CONSUMER_PRODUCER = "consumer-producer";
	public static final HashSet<String> CONDITIONS = Sets.newHashSet("empty", "full");

	public static void main(String[] args) {
		List<Thread> consumers = Lists.newArrayList();

		for (int i = 0; i < CONSUMER_COUNT; i++) {
			DistributedMonitorConfiguration configuration = DistributedMonitorConfiguration
					.builder().conditions(CONDITIONS).monitorId(CONSUMER_PRODUCER).nodeId(i)
					.nodeCount(CONSUMER_COUNT + PRODUCER_COUNT)
					.sharedObject(new ConsumerProducerSharedModel()).build();
			consumers.add(new Thread(() -> {
				DistributedMonitor monitor = new DistributedMonitor(configuration);
			}));
		}

		List<Thread> producers = Lists.newArrayList();
		for (int i = 0; i < PRODUCER_COUNT; i++) {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");
			factory.setUsername("guest");
			factory.setPassword("guest");
			try {
				Connection connection = factory.newConnection();
				Channel channel = connection.createChannel();
				channel.exchangeDeclare(CONSUMER_PRODUCER, "direct", true);
				String queueName = channel.queueDeclare(String.valueOf(i), true, false, false, null)
						.getQueue();
				channel.queueBind(queueName, CONSUMER_PRODUCER, "");
				producers.add(new Thread(() -> {
					for (int j = 0; j < 3; j++) {
						try {
							channel.basicPublish(CONSUMER_PRODUCER, "", null,
									String.valueOf(j).getBytes("UTF-8"));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}));
			} catch (IOException | TimeoutException e) {
				e.printStackTrace();
			}

		}

		producers.forEach(Thread::start);
		consumers.forEach(Thread::start);
	}
}
