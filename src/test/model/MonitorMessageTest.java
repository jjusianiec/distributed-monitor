package model;

import org.junit.Test;

import static org.junit.Assert.*;

public class MonitorMessageTest {

	public static final long TIMESTAMP = 100L;
	public static final int NODE_ID = 50;

	@Test
	public void shouldDecode() {
		// given
		// when
		NodeIdWithTimestamp nodeIdWithTimestamp = NodeIdWithTimestamp.builder().timestamp(TIMESTAMP)
				.nodeId(NODE_ID).build();
		CriticalSectionRequest criticalSectionRequest = CriticalSectionRequest.builder()
				.type(CriticalSectionRequestType.REQUEST).nodeIdWithTimestamp(nodeIdWithTimestamp).
				.build();
		MonitorMessage.builder().message(criticalSectionRequest)
				.type(CriticalSectionRequest.class.getName())

		// then
	}
}