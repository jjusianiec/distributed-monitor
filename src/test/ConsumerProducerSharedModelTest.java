import java.util.ArrayDeque;

import org.junit.Test;

import com.google.common.collect.Queues;

import model.ConsumerProducerSharedModel;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsumerProducerSharedModelTest {
	public static final int SIZE = 10;
	public static final int FIRST_VALUE = 10;
	public static final int SECOND_VALUE = 5;
	public static final int THIRD_VALUE = 2;

	@Test
	public void shouldDecode() {
		// given
		ArrayDeque<Integer> buffer = Queues.newArrayDeque();
		buffer.add(FIRST_VALUE);
		buffer.add(SECOND_VALUE);
		buffer.add(THIRD_VALUE);
		ConsumerProducerSharedModel sharedModel = ConsumerProducerSharedModel.builder()
				.buffer(buffer).size(SIZE).build();
		// when
		String encoded = sharedModel.encode();
		ConsumerProducerSharedModel decode = (ConsumerProducerSharedModel) sharedModel
				.decode(encoded);
		// then
		assertThat(decode.getBuffer().remove()).isEqualTo(FIRST_VALUE);
		assertThat(decode.getBuffer().remove()).isEqualTo(SECOND_VALUE);
		assertThat(decode.getBuffer().remove()).isEqualTo(THIRD_VALUE);
		assertThat(decode.getSize()).isEqualTo(SIZE);
	}
}