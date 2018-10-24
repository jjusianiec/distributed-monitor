package model;

import java.util.Queue;

import com.google.common.collect.Queues;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsumerProducerSharedModel {
	private Integer size;
	private Queue<Integer> buffer = Queues.newArrayDeque();
}
