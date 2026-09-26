package minimes.purchase.dto;

import java.time.LocalDate;
import java.util.List;

import minimes.purchase.domain.PurchaseOrder;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseOrderResponse {

	private String poNo;
	private String poType;
	private LocalDate poDate;
	private String vendorId;
	private String vendorName;
	private String managerName;
	private LocalDate dueDate;
	private String remark;
	private List<PurchaseOrderItemResponse> items;

	public static PurchaseOrderResponse from(PurchaseOrder order, List<PurchaseOrderItemResponse> items) {
		return PurchaseOrderResponse.builder()
				.poNo(order.getPoNo())
				.poType(order.getPoType())
				.poDate(order.getPoDate())
				.vendorId(order.getVendorId())
				.vendorName(order.getVendorName())
				.managerName(order.getManagerName())
				.dueDate(order.getDueDate())
				.remark(order.getRemark())
				.items(items)
				.build();
	}
}
