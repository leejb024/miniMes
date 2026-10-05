package minimes.master.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BomSaveRequest {

	@NotBlank(message = "제품을 선택하세요.")
	private String itemId;

	@NotBlank(message = "BOM 버전을 입력하세요.")
	@Size(max = 50, message = "BOM 버전은 50자 이하로 입력하세요.")
	private String bomVersion;

	@NotEmpty(message = "원료를 한 건 이상 등록하세요.")
	private List<@Valid BomMaterialRequest> materials;
}
