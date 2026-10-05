package minimes.wms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import minimes.wms.domain.WmsInboundPlan;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WmsInboundPlanResponse {

	private String planNo;
	private LocalDate planDate;
	private String workOrderId;
	private String itemId;
	private String itemName;
	private String lotNo;
	private BigDecimal qty;
	private String unit;
	private String warehouseName;
	private String status;

	public static WmsInboundPlanResponse from(WmsInboundPlan plan) {
		return WmsInboundPlanResponse.builder()
				.planNo(plan.getPlanNo())
				.planDate(plan.getPlanDate())
				.workOrderId(plan.getWorkOrderId())
				.itemId(plan.getItemId())
				.itemName(plan.getItemName())
				.lotNo(plan.getLotNo())
				.qty(plan.getQty())
				.unit(plan.getUnit())
				.warehouseName(plan.getWarehouseName())
				.status(plan.getStatus())
				.build();
	}
}
