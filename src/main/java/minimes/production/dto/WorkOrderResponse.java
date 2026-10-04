package minimes.production.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import minimes.production.domain.WorkOrder;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkOrderResponse {

	private LocalDate planDate;
	private String workOrderId;
	private String workcenterId;
	private String workcenterName;
	private String processId;
	private String processName;
	private String equipId;
	private String equipName;
	private String itemId;
	private String state;
	private String resultStatus;
	private String unit;
	private BigDecimal planQty;
	private BigDecimal resultQty;
	private Long packCount;
	private LocalDate expiredDate;
	private String itemVersion;
	private String bomVersion;
	private String closeYn;

	public static WorkOrderResponse from(WorkOrderView view) {
		return from(view, null);
	}

	public static WorkOrderResponse from(WorkOrderView view, String resultStatus) {
		return from(view, resultStatus, null);
	}

	public static WorkOrderResponse from(WorkOrderView view, String resultStatus, BigDecimal resultQty) {
		return from(view, resultStatus, resultQty, null);
	}

	public static WorkOrderResponse from(WorkOrderView view, String resultStatus, BigDecimal resultQty, Long packCount) {
		return WorkOrderResponse.builder()
				.planDate(view.getPlanDate())
				.workOrderId(view.getWorkOrderId())
				.workcenterId(view.getWorkcenterId())
				.workcenterName(view.getWorkcenterName())
				.processId(view.getProcessId())
				.processName(view.getProcessName())
				.equipId(view.getEquipId())
				.equipName(view.getEquipName())
				.itemId(view.getItemId())
				.state(view.getState())
				.resultStatus(resultStatus)
				.unit(view.getUnit())
				.planQty(view.getPlanQty())
				.resultQty(resultQty)
				.packCount(packCount)
				.expiredDate(view.getExpiredDate())
				.itemVersion(view.getItemVersion())
				.bomVersion(view.getBomVersion())
				.closeYn(view.getCloseYn())
				.build();
	}

	public static WorkOrderResponse from(WorkOrder workOrder, String bomVersion) {
		return WorkOrderResponse.builder()
				.planDate(workOrder.getPlanDate())
				.workOrderId(workOrder.getWorkOrderId())
				.workcenterId(workOrder.getWorkcenterId())
				.processId(workOrder.getProcessId())
				.equipId(workOrder.getEquipId())
				.itemId(workOrder.getItemId())
				.state(workOrder.getState())
				.resultStatus("미등록")
				.unit(workOrder.getUnit())
				.planQty(workOrder.getPlanQty())
				.expiredDate(workOrder.getExpiredDate())
				.itemVersion(workOrder.getItemVersion())
				.bomVersion(bomVersion)
				.closeYn(workOrder.getCloseYn())
				.build();
	}
}
