package minimes.production.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProdResultCreateRequest {

	@NotBlank(message = "작업지시를 선택하세요.")
	private String workOrderId;

	private String lotId;

	@NotNull(message = "실적 수량을 입력하세요.")
	@DecimalMin(value = "0.0001", message = "실적 수량은 0보다 커야 합니다.")
	private BigDecimal prodQty;

	private String resultType;
	private LocalDateTime productionStartTime;
	private LocalDateTime productionEndTime;
}
