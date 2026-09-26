package minimes.stock.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import minimes.stock.domain.MaterialMove;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MaterialMoveResponse {

	private Long moveSeq;
	private String orderNo;
	private String moveType;
	private BigDecimal qty;
	private String remark;
	private String creId;
	private LocalDateTime creDt;

	public static MaterialMoveResponse from(MaterialMove move) {
		return MaterialMoveResponse.builder()
				.moveSeq(move.getMoveSeq())
				.orderNo(move.getOrderNo())
				.moveType(move.getMoveType())
				.qty(move.getQty())
				.remark(move.getRemark())
				.creId(move.getCreId())
				.creDt(move.getCreDt())
				.build();
	}
}
