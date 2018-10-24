package service;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class ReceivingService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReceivingService.class);
	private final ScheduledExecutorService executorService = Executors
			.newSingleThreadScheduledExecutor();
	private Channel channel;
	private Integer channelId;

	public ReceivingService(String monitorId, Integer nodeId) {
		try {
			this.channelId = nodeId;
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost("localhost");
			factory.setUsername("guest");
			factory.setPassword("guest");
			Connection connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare(monitorId, "direct", true);
			String queueName = channel
					.queueDeclare(String.valueOf(nodeId), true, false, false, null).getQueue();
			channel.queueBind(queueName, monitorId, "");
		} catch (IOException | TimeoutException e) {
			LOGGER.error("Connection error", e);
		}
	}

	public void subscribe(MessageConsumer messageConsumer) {
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
					AMQP.BasicProperties properties, byte[] body) throws IOException {
				messageConsumer.consume(body);
			}
		};
		executorService.scheduleWithFixedDelay(() -> {
			try {
				channel.basicConsume(String.valueOf(channelId), true, consumer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, 2000, 500, TimeUnit.MILLISECONDS);
	}

	public interface MessageConsumer {
		void consume(byte[] body);
	}
}
