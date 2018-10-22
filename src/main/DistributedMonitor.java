import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class DistributedMonitor implements Observer {
	private static final Logger LOGGER = getLogger(DistributedMonitor.class);
	private DistributedMonitorConfiguration configuration;
	private final Lock lock = new ReentrantLock(true);

	private ReceivingService receivingService;

	public DistributedMonitor(DistributedMonitorConfiguration configuration) {
		this.configuration = configuration;
		receivingService.subscribe((byte[] body) -> {
			lock.lock();
			LOGGER.info("XD");
			lock.unlock();
		});
	}

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

	public void signal(String condition) {
		LOGGER.debug("signal");

	}

	public void waitUntil(String condition) {
		LOGGER.debug("waitUntil");
	}

	@Override
	public void update(Observable o, Object arg) {

	}
}
