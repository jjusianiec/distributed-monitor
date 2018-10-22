import java.io.IOException;
import java.util.List;

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
public class ConsumerProducerSharedModel implements SharedModel {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerProducerSharedModel.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private List<String> buffer;

	@Override
	public String encode() {
		try {
			return OBJECT_MAPPER.writeValueAsString(buffer);
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