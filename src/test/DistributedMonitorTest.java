import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.CriticalSectionRequest;

import static org.junit.Assert.*;

public class DistributedMonitorTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(DistributedMonitorTest.class);

	@Test
	public void shouldName() {
		// given
		// when
		LOGGER.info(CriticalSectionRequest.class.getSimpleName());
		// then
	}
}