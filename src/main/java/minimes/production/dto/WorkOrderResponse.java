package minimes.production.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkOrderResponse {

	private LocalDate planDate;
	private String workOrderId;
	private String workcenterId;
	private String workcenterName;
	private String itemId;
	private String deptId;
	private String deptName;
	private String state;
	private String unit;
	private BigDecimal planQty;
	private LocalDate expiredDate;
	private String itemVersion;
	private String bomVersion;

	public static WorkOrderResponse from(WorkOrderView view) {
		return WorkOrderResponse.builder()
				.planDate(view.getPlanDate())
				.workOrderId(view.getWorkOrderId())
				.workcenterId(view.getWorkcenterId())
				.workcenterName(view.getWorkcenterName())
				.itemId(view.getItemId())
				.deptId(view.getDeptId())
				.deptName(view.getDeptName())
				.state(view.getState())
				.unit(view.getUnit())
				.planQty(view.getPlanQty())
				.expiredDate(view.getExpiredDate())
				.itemVersion(view.getItemVersion())
				.bomVersion(view.getBomVersion())
				.build();
	}
}
