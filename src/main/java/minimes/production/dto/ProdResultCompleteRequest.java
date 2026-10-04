package minimes.production.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProdResultCompleteRequest {

	@NotBlank(message = "작업지시를 선택하세요.")
	private String workOrderId;
}
