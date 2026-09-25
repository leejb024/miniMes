package minimes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProdResultResponse {

	private String workOrderId;
	private String plantId;
	private String lotId;
	private BigDecimal prodQty;
	private LocalDateTime productionStartTime;
	private LocalDateTime productionEndTime;
	private String productionNo;
	private String unit;
	private BigDecimal wmsProdQty;
	private BigDecimal wmsConfirmQty;

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
				.wmsProdQty(view.getWmsProdQty())
				.wmsConfirmQty(view.getWmsConfirmQty())
				.build();
	}
}
