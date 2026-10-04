package minimes.production.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import minimes.production.domain.ProdResult;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProdResultResponse {

	private Long prodResultSeq;
	private String workOrderId;
	private String plantId;
	private String lotId;
	private BigDecimal prodQty;
	private LocalDateTime productionStartTime;
	private LocalDateTime productionEndTime;
	private String productionNo;
	private String unit;
	private String isConfirmed;
	private String resultType;

	public static ProdResultResponse from(ProdResult result, String unit) {
		return ProdResultResponse.builder()
				.prodResultSeq(result.getProdResultSeq())
				.workOrderId(result.getWorkOrderId())
				.plantId(result.getPlantId())
				.lotId(result.getLotId())
				.prodQty(result.getProdQty())
				.productionStartTime(result.getProductionStartTime())
				.productionEndTime(result.getProductionEndTime())
				.productionNo(result.getProductionNo())
				.unit(unit)
				.isConfirmed(result.getIsConfirmed())
				.resultType(result.getResultType())
				.build();
	}

	public static ProdResultResponse from(ProdResultView view) {
		return ProdResultResponse.builder()
				.workOrderId(view.getWorkOrderId())
				.plantId(view.getPlantId())
				.lotId(view.getLotId())
				.prodQty(view.getProdQty())
				.productionStartTime(view.getProductionStartTime())
				.productionEndTime(view.getProductionEndTime())
				.productionNo(view.getProductionNo())
				.unit(view.getUnit())
				.build();
	}
}
