package minimes.purchase.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseLotRequest {

	@NotNull(message = "스캔 정보를 먼저 저장하세요.")
	private Long scanSeq;

	@NotNull(message = "입고 수량을 입력하세요.")
	private BigDecimal qty;

	private String lotNo;

	private String warehouseId;
}
