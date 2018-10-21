import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class DistributedMonitor {
	private static final Logger LOGGER = getLogger(DistributedMonitor.class);

	public void synchronize(Runnable r) {
		LOGGER.debug("start");
		try {
			r.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			LOGGER.debug("stop");
		}
	}

	public void signal() {
		LOGGER.debug("signal");

	}

	public void signalAll() {
		LOGGER.debug("signalAll");
	}

	public void waitUntil() {
		LOGGER.debug("waitUntil");
	}
}
