package minimes.master.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EquipmentResponse {

	private String equipId;
	private String equipName;
	private String plantId;
	private String processId;
	private String processName;
	private String warehouseId;
	private String warehouseName;
	private String equipType;
}
