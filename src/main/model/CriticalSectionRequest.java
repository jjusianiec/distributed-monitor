package model;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CriticalSectionRequest extends InstanceMessage implements Message  {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final Logger LOGGER = LoggerFactory.getLogger(CriticalSectionRequest.class);

	private NodeIdWithTimestamp nodeIdWithTimestamp;
	private CriticalSectionRequestType type;

	@Override
	public String encode() {
		try {
			return OBJECT_MAPPER.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			LOGGER.error("ConsumerProducerSharedModel encode error", e);
		}
		return null;
	}

	@Override
	public Message decode(String object) {
		try {
			return OBJECT_MAPPER.readValue(object, CriticalSectionRequest.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
