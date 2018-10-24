import java.util.ArrayDeque;

import org.junit.Test;

import com.google.common.collect.Queues;

import model.ConsumerProducerSharedModel;
import model.ConsumerProducerSharedModelSerializationImpl;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsumerProducerSharedModelSerializationImplTest {
	public static final int SIZE = 10;
	public static final int FIRST_VALUE = 10;
	public static final int SECOND_VALUE = 5;
	public static final int THIRD_VALUE = 2;

	private ConsumerProducerSharedModelSerializationImpl tested = new ConsumerProducerSharedModelSerializationImpl();

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
		String encoded = tested.encode(sharedModel);
		ConsumerProducerSharedModel decode = tested.decode(encoded);
		// then
		assertThat(decode.getBuffer().remove()).isEqualTo(FIRST_VALUE);
		assertThat(decode.getBuffer().remove()).isEqualTo(SECOND_VALUE);
		assertThat(decode.getBuffer().remove()).isEqualTo(THIRD_VALUE);
		assertThat(decode.getSize()).isEqualTo(SIZE);
	}
}