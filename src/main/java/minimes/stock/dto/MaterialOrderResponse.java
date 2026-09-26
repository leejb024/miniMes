package minimes.stock.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import minimes.stock.domain.MaterialOrder;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MaterialOrderResponse {

	private Long orderSeq;
	private String orderNo;
	private String status;
	private Long stockSeq;
	private String itemId;
	private String itemName;
	private String lotNo;
	private String unit;
	private BigDecimal qty;
	private String fromWarehouseId;
	private String fromWarehouseName;
	private String toWarehouseId;
	private String toWarehouseName;
	private String approvedYn;
	private String pickNo;
	private BigDecimal weighQty;
	private String inputLotNo;
	private String combineResult;
	private String remark;
	private String creId;
	private LocalDateTime creDt;
	private List<MaterialMoveResponse> moves;

	public static MaterialOrderResponse from(MaterialOrder order, List<MaterialMoveResponse> moves) {
		return MaterialOrderResponse.builder()
				.orderSeq(order.getOrderSeq())
				.orderNo(order.getOrderNo())
				.status(order.getStatus())
				.stockSeq(order.getStockSeq())
				.itemId(order.getItemId())
				.itemName(order.getItemName())
				.lotNo(order.getLotNo())
				.unit(order.getUnit())
				.qty(order.getQty())
				.fromWarehouseId(order.getFromWarehouseId())
				.fromWarehouseName(order.getFromWarehouseName())
				.toWarehouseId(order.getToWarehouseId())
				.toWarehouseName(order.getToWarehouseName())
				.approvedYn(order.getApprovedYn())
				.pickNo(order.getPickNo())
				.weighQty(order.getWeighQty())
				.inputLotNo(order.getInputLotNo())
				.combineResult(order.getCombineResult())
				.remark(order.getRemark())
				.creId(order.getCreId())
				.creDt(order.getCreDt())
				.moves(moves)
				.build();
	}
}
