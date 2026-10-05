package minimes.master.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCreateRequest {

	@NotBlank(message = "품목명을 입력하세요.")
	private String itemName;

	@NotBlank(message = "구분을 선택하세요.")
	private String kind;
}
