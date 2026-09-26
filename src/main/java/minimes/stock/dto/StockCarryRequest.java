package minimes.stock.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockCarryRequest {

	@NotNull(message = "재고를 선택하세요.")
	private Long stockSeq;

	@NotBlank(message = "처리 구분을 선택하세요.")
	private String carryType;

	@NotNull(message = "기준일자를 선택하세요.")
	private LocalDate baseDate;

	private String remark;
}
