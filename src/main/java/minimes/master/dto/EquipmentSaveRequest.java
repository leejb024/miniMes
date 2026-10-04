package minimes.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EquipmentSaveRequest {

	@NotBlank(message = "설비 ID를 입력하세요.")
	@Size(max = 50, message = "설비 ID는 50자 이하로 입력하세요.")
	private String equipId;

	@NotBlank(message = "설비명을 입력하세요.")
	@Size(max = 100, message = "설비명은 100자 이하로 입력하세요.")
	private String equipName;

	@NotBlank(message = "공장을 입력하세요.")
	@Size(max = 50, message = "공장은 50자 이하로 입력하세요.")
	private String plantId;

	@NotBlank(message = "공정을 선택하세요.")
	@Size(max = 50, message = "공정코드는 50자 이하로 입력하세요.")
	private String processId;

	@Size(max = 50, message = "창고는 50자 이하로 입력하세요.")
	private String warehouseId;

	@Size(max = 50, message = "설비 유형은 50자 이하로 입력하세요.")
	private String equipType;
}
