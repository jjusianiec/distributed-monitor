import java.io.IOException;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Queues;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsumerProducerSharedModel implements SharedModel {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerProducerSharedModel.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private Integer size;
	private Queue<Integer> buffer = Queues.newArrayDeque();

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
	public SharedModel decode(String object) {
		try {
			return OBJECT_MAPPER.readValue(object, ConsumerProducerSharedModel.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
