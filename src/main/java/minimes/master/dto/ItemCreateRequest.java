package minimes.master.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCreateRequest {

	@NotBlank(message = "품목명을 입력하세요.")
	private String itemName;
}
