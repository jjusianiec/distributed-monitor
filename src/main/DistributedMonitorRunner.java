public class DistributedMonitorRunner {
	private static DistributedMonitor distributedMonitor = new DistributedMonitor();

	public static void main(String[] args) {
		distributedMonitor.synchronize(() -> dupa(10));
	}


	private static void dupa(int value) throws InterruptedException {
		distributedMonitor.signal();
		distributedMonitor.signalAll();
		distributedMonitor.waitUntil();
		//sth
	}
}
