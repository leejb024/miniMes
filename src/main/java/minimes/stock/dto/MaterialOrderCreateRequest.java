package minimes.stock.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialOrderCreateRequest {

	@NotNull(message = "재고를 선택하세요.")
	private Long stockSeq;

	@NotNull(message = "요청 수량을 입력하세요.")
	private BigDecimal qty;

	@NotBlank(message = "입고 창고를 선택하세요.")
	private String toWarehouseId;

	private String remark;
}
