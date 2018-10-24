package service;

import model.MonitorMessage;

public class LamportClockUtils {
	public static long getNewTimestamp(long currentTimestamp, MonitorMessage receivedMessage) {
		return Math.max(currentTimestamp, receivedMessage.getNodeIdWithTimestamp().getTimestamp())
				+ 1;
	}
}
