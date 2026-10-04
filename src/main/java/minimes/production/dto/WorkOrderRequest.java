package minimes.production.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkOrderRequest {

	private String workOrderId;

	@NotNull(message = "계획일을 입력하세요.")
	private LocalDate planDate;

	private String workcenterId;

	@NotBlank(message = "공정을 선택하세요.")
	private String processId;

	@NotBlank(message = "설비를 선택하세요.")
	private String equipId;

	@NotBlank(message = "품목을 선택하세요.")
	private String itemId;

	private String state;
	private String unit;

	@NotNull(message = "계획수량을 입력하세요.")
	@DecimalMin(value = "0.0001", message = "계획수량은 0보다 커야 합니다.")
	private BigDecimal planQty;

	private LocalDate expiredDate;
	private String itemVersion;
}
