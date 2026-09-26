package minimes.stock.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockCloseRequest {

	@NotBlank(message = "마감 구분을 선택하세요.")
	private String closeType;

	private String closeMonth;

	private LocalDate baseDate;

	private String remark;
}
