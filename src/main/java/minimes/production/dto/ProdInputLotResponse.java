package minimes.production.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import minimes.production.domain.ProdInputLot;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProdInputLotResponse {

	private Long inputLotSeq;
	private String workOrderId;
	private Long stockSeq;
	private String warehouseId;
	private String warehouseName;
	private String itemId;
	private String itemName;
	private String lotNo;
	private String unit;
	private BigDecimal weighQty;
	private BigDecimal inputQty;
	private String combineResult;
	private String status;
	private LocalDateTime creDt;

	public static ProdInputLotResponse from(ProdInputLot lot) {
		return ProdInputLotResponse.builder()
				.inputLotSeq(lot.getInputLotSeq())
				.workOrderId(lot.getWorkOrderId())
				.stockSeq(lot.getStockSeq())
				.warehouseId(lot.getWarehouseId())
				.warehouseName(lot.getWarehouseName())
				.itemId(lot.getItemId())
				.itemName(lot.getItemName())
				.lotNo(lot.getLotNo())
				.unit(lot.getUnit())
				.weighQty(lot.getWeighQty())
				.inputQty(lot.getInputQty())
				.combineResult(lot.getCombineResult())
				.status(lot.getStatus())
				.creDt(lot.getCreDt())
				.build();
	}
}
