package minimes.stock.dto;

import java.math.BigDecimal;

import minimes.stock.domain.Stock;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockResponse {

	private Long stockSeq;
	private String warehouseId;
	private String warehouseName;
	private String itemId;
	private String itemName;
	private String lotNo;
	private String unit;
	private BigDecimal qty;
	private String locationName;

	public static StockResponse from(Stock stock) {
		return StockResponse.builder()
				.stockSeq(stock.getStockSeq())
				.warehouseId(stock.getWarehouseId())
				.warehouseName(stock.getWarehouseName())
				.itemId(stock.getItemId())
				.itemName(stock.getItemName())
				.lotNo(stock.getLotNo())
				.unit(stock.getUnit())
				.qty(stock.getQty())
				.locationName(stock.getLocationName())
				.build();
	}
}
