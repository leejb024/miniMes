package minimes.purchase.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseReturnRequest {

	@NotNull(message = "반품할 입고 LOT를 선택하세요.")
	private Long lotSeq;

	@NotNull(message = "반품 수량을 입력하세요.")
	private BigDecimal qty;

	private String remark;
}
