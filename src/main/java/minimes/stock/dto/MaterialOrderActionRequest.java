package minimes.stock.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialOrderActionRequest {

	@NotBlank(message = "불출 요청을 선택하세요.")
	private String orderNo;

	private String approvedYn;
	private String pickNo;
	private BigDecimal qty;
	private BigDecimal weighQty;
	private String lotNo;
	private String combineResult;
	private String remark;
}
