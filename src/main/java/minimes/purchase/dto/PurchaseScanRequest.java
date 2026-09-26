package minimes.purchase.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseScanRequest {

	@NotBlank(message = "바코드를 입력하세요.")
	private String barcode;

	private String poNo;
}
