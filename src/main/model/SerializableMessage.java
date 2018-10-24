package model;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class SerializableMessage {
	private static final Logger LOGGER = LoggerFactory.getLogger(SerializableMessage.class);
	protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private UUID runInstanceId;

	public String encode() {
		try {
			return OBJECT_MAPPER.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			LOGGER.error("SerializableMessage encode error", e);
		}
		return null;
	}

	public SerializableMessage decode(String object) {
		try {
			return OBJECT_MAPPER.readValue(object, getClass());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public UUID getRunInstanceId() {
		return runInstanceId;
	}

	public void setRunInstanceId(UUID runInstanceId) {
		this.runInstanceId = runInstanceId;
	}
}
