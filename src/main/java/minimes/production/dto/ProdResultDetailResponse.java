package minimes.production.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import minimes.production.domain.ProdResult;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProdResultDetailResponse {

	private Long prodResultSeq;
	private LocalDateTime productionStartTime;
	private LocalDateTime productionEndTime;
	private BigDecimal prodQty;

	public static ProdResultDetailResponse from(ProdResult result) {
		return ProdResultDetailResponse.builder()
				.prodResultSeq(result.getProdResultSeq())
				.productionStartTime(result.getProductionStartTime())
				.productionEndTime(result.getProductionEndTime())
				.prodQty(result.getProdQty())
				.build();
	}
}
