import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class SendingService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SendingService.class);
	private Channel channel;
	private String monitorId;

	public SendingService(String monitorId, Integer nodeId) {
		this.monitorId = monitorId;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		factory.setUsername("guest");
		factory.setPassword("guest");
		try {
			Connection connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare(monitorId, "direct", true);
			String queueName = channel
					.queueDeclare(String.valueOf(nodeId), true, false, false, null).getQueue();
			channel.queueBind(queueName, monitorId, "");
		} catch (IOException | TimeoutException e) {
			LOGGER.error("Sending service initializing error", e);
		}
	}

	public void send(String message) {
		try {
			channel.basicPublish(monitorId, "", null, message.getBytes("UTF-8"));
		} catch (IOException e) {
			LOGGER.error("Message sending error", e);
		}
	}
}
