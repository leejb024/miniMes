package minimes.production.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProdResultUpdateRequest {

	@NotBlank(message = "LOT NO를 입력하세요.")
	private String lotId;

	@NotNull(message = "실적 수량을 입력하세요.")
	@DecimalMin(value = "0.0001", message = "실적 수량은 0보다 커야 합니다.")
	private BigDecimal prodQty;
}
