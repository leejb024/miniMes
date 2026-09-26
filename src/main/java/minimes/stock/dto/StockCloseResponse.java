package minimes.stock.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import minimes.stock.domain.StockClose;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockCloseResponse {

	private Long closeSeq;
	private String closeType;
	private String closeMonth;
	private LocalDate baseDate;
	private String remark;
	private String creId;
	private LocalDateTime creDt;

	public static StockCloseResponse from(StockClose close) {
		return StockCloseResponse.builder()
				.closeSeq(close.getCloseSeq())
				.closeType(close.getCloseType())
				.closeMonth(close.getCloseMonth())
				.baseDate(close.getBaseDate())
				.remark(close.getRemark())
				.creId(close.getCreId())
				.creDt(close.getCreDt())
				.build();
	}
}
