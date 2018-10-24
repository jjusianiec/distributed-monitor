package model;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConsumerProducerSharedModelSerializationImpl
		implements SharedObjectSerialization<ConsumerProducerSharedModel> {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ConsumerProducerSharedModelSerializationImpl.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	public String encode(ConsumerProducerSharedModel consumerProducerSharedModel) {
		try {
			return OBJECT_MAPPER.writeValueAsString(consumerProducerSharedModel);
		} catch (JsonProcessingException e) {
			LOGGER.error("ConsumerProducerSharedModelSerializationImpl encode error", e);
		}
		return null;
	}

	@Override
	public ConsumerProducerSharedModel decode(String object) {
		try {
			return OBJECT_MAPPER.readValue(object, ConsumerProducerSharedModel.class);
		} catch (IOException e) {
			LOGGER.error("ConsumerProducerSharedModelSerializationImpl decode error", e);
			e.printStackTrace();
		}
		return null;
	}
}
