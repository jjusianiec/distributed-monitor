package service;

import java.util.Comparator;

import model.NodeIdWithTimestamp;

public class NodeIdWithTimestampComparator implements Comparator<NodeIdWithTimestamp> {
	public static final NodeIdWithTimestampComparator INSTANCE = new NodeIdWithTimestampComparator();

	@Override
	public int compare(NodeIdWithTimestamp o1, NodeIdWithTimestamp o2) {
		if (o1.getTimestamp().equals(o2.getTimestamp())) {
			return o1.getNodeId() - o2.getNodeId();
		}
		return (int) (o1.getTimestamp() - o2.getTimestamp());
	}

}
