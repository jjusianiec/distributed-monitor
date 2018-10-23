import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DistributedMonitorConfiguration<T extends SharedModel> {
	private String monitorId;
	private T sharedObject;
	private Set<String> conditions;
	private Integer nodeCount;
	private Integer nodeId;
	private UUID runInstanceId;
}
