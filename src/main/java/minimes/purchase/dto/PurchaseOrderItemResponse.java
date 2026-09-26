package minimes.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import minimes.purchase.domain.PurchaseOrderItem;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseOrderItemResponse {

	private Long poItemSeq;
	private String itemId;
	private String itemName;
	private BigDecimal qty;
	private String unit;
	private LocalDate dueDate;

	public static PurchaseOrderItemResponse from(PurchaseOrderItem item) {
		return PurchaseOrderItemResponse.builder()
				.poItemSeq(item.getPoItemSeq())
				.itemId(item.getItemId())
				.itemName(item.getItemName())
				.qty(item.getQty())
				.unit(item.getUnit())
				.dueDate(item.getDueDate())
				.build();
	}
}
