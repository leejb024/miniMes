package minimes.production.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkOrderAmountRequest {

	@NotBlank(message = "작업지시를 선택하세요.")
	private String workOrderId;

	@NotNull(message = "계획수량을 입력하세요.")
	private BigDecimal planQty;
}
