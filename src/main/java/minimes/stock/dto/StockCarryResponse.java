package minimes.stock.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import minimes.stock.domain.StockCarry;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockCarryResponse {

	private Long carrySeq;
	private String carryType;
	private LocalDate baseDate;
	private Long stockSeq;
	private Long sourceCarrySeq;
	private String warehouseId;
	private String warehouseName;
	private String itemId;
	private String itemName;
	private String lotNo;
	private String unit;
	private BigDecimal qty;
	private String remark;
	private String creId;
	private LocalDateTime creDt;

	public static StockCarryResponse from(StockCarry carry) {
		return StockCarryResponse.builder()
				.carrySeq(carry.getCarrySeq())
				.carryType(carry.getCarryType())
				.baseDate(carry.getBaseDate())
				.stockSeq(carry.getStockSeq())
				.sourceCarrySeq(carry.getSourceCarrySeq())
				.warehouseId(carry.getWarehouseId())
				.warehouseName(carry.getWarehouseName())
				.itemId(carry.getItemId())
				.itemName(carry.getItemName())
				.lotNo(carry.getLotNo())
				.unit(carry.getUnit())
				.qty(carry.getQty())
				.remark(carry.getRemark())
				.creId(carry.getCreId())
				.creDt(carry.getCreDt())
				.build();
	}
}
