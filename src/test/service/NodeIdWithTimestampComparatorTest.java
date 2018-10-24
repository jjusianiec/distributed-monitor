package service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import model.NodeIdWithTimestamp;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeIdWithTimestampComparatorTest {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(NodeIdWithTimestampComparatorTest.class);

	private NodeIdWithTimestampComparator tested = new NodeIdWithTimestampComparator();

	@Test
	public void shouldCompare() {
		// given
		NodeIdWithTimestamp o1 = NodeIdWithTimestamp.builder().nodeId(13).timestamp(200L).build();
		NodeIdWithTimestamp o2 = NodeIdWithTimestamp.builder().nodeId(12).timestamp(200L).build();
		NodeIdWithTimestamp o3 = NodeIdWithTimestamp.builder().nodeId(10).timestamp(100L).build();
		NodeIdWithTimestamp o4 = NodeIdWithTimestamp.builder().nodeId(10).timestamp(90L).build();
		List<NodeIdWithTimestamp> nodeIdWithTimestamps = Lists.newArrayList(o1, o2, o3, o4);
		// when
		nodeIdWithTimestamps.sort(tested);
		// then
		assertThat(nodeIdWithTimestamps.get(0)).isEqualTo(o4);
		assertThat(nodeIdWithTimestamps.get(1)).isEqualTo(o3);
		assertThat(nodeIdWithTimestamps.get(2)).isEqualTo(o2);
		assertThat(nodeIdWithTimestamps.get(3)).isEqualTo(o1);
	}
}