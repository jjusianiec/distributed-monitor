package model;

import java.io.IOException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorMessage extends SerializableMessage {
	private String type;
	private SerializableMessage message;

	@Override
	public SerializableMessage decode(String object) {
		try {
			RawMonitorMessage rawMonitorMessage = OBJECT_MAPPER
					.readValue(object, RawMonitorMessage.class);
			if ("CriticalSectionRequest".equals(rawMonitorMessage.type)) {
				SerializableMessage criticalSectionRequest = new CriticalSectionRequest()
						.decode(rawMonitorMessage.message);

				MonitorMessage message = new MonitorMessage();
				message.setMessage(criticalSectionRequest);
				message.setType(rawMonitorMessage.getType());
				message.setRunInstanceId(rawMonitorMessage.getRunInstanceId());
				return message;
			}
			return rawMonitorMessage;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	private class RawMonitorMessage extends SerializableMessage {
		private String type;
		private String message;
	}
}
