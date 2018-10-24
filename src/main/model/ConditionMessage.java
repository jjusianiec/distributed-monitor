package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConditionMessage {
	private ConditionMessageType type;
	private String name;
	private Integer targetNodeId;
}
