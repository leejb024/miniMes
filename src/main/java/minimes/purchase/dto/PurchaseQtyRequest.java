package minimes.purchase.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseQtyRequest {

	@NotNull(message = "LOT를 선택하세요.")
	private Long lotSeq;

	@NotNull(message = "수량을 입력하세요.")
	private BigDecimal qty;
}
