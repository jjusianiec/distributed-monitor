package service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.ConditionMessage;
import model.CriticalSectionRequest;
import model.MonitorMessage;

public class MessageSerializationService {
	public static final MessageSerializationService INSTANCE = new MessageSerializationService();
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageSerializationService.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static String encode(CriticalSectionRequest model) {
		return encodeQuietly(model);
	}

	public static String encode(MonitorMessage monitorMessage) {
		return encodeQuietly(monitorMessage);
	}

	public static String encode(ConditionMessage conditionMessage) {
		return encodeQuietly(conditionMessage);
	}

	public static <T> T decode(String object, Class<T> tClass) {
		try {
			return OBJECT_MAPPER.readValue(object, tClass);
		} catch (IOException e) {
			LOGGER.error("MessageSerializationService decoding error", e);
		}
		return null;
	}

	private static String encodeQuietly(Object model) {
		try {
			return OBJECT_MAPPER.writeValueAsString(model);
		} catch (JsonProcessingException e) {
			LOGGER.error("CriticalSectionRequest encoding error", e);
		}
		return null;
	}
}
