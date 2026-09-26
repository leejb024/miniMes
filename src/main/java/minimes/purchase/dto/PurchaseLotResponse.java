package minimes.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import minimes.purchase.domain.PurchaseLot;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseLotResponse {

	private Long lotSeq;
	private String lotNo;
	private String lotType;
	private String poNo;
	private Long poItemSeq;
	private String itemId;
	private String itemName;
	private String unit;
	private BigDecimal qty;
	private BigDecimal returnedQty;
	private BigDecimal remainQty;
	private String warehouseId;
	private String warehouseName;
	private String barcode;
	private String sourceLotNo;
	private String remark;
	private LocalDateTime creDt;

	public static PurchaseLotResponse from(PurchaseLot lot, BigDecimal returnedQty) {
		BigDecimal qty = lot.getQty() == null ? BigDecimal.ZERO : lot.getQty();
		BigDecimal returned = returnedQty == null ? BigDecimal.ZERO : returnedQty;
		return PurchaseLotResponse.builder()
				.lotSeq(lot.getLotSeq())
				.lotNo(lot.getLotNo())
				.lotType(lot.getLotType())
				.poNo(lot.getPoNo())
				.poItemSeq(lot.getPoItemSeq())
				.itemId(lot.getItemId())
				.itemName(lot.getItemName())
				.unit(lot.getUnit())
				.qty(qty)
				.returnedQty(returned)
				.remainQty(qty.subtract(returned))
				.warehouseId(lot.getWarehouseId())
				.warehouseName(lot.getWarehouseName())
				.barcode(lot.getBarcode())
				.sourceLotNo(lot.getSourceLotNo())
				.remark(lot.getRemark())
				.creDt(lot.getCreDt())
				.build();
	}
}
