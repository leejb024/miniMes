package com.example.demo.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BomItemResponse {

	private String materialId;
	private String itemId;
	private String itemName;
	private BigDecimal qty;
	private String unit;
	private Long bomSeq;

	public static BomItemResponse from(BomMaterialView view) {
		BigDecimal qty = view.getQty();
		return BomItemResponse.builder()
				.materialId(view.getMaterialId())
				.itemId(view.getItemId())
				.itemName(view.getItemName())
				.qty(qty == null ? null : qty.setScale(5, RoundingMode.HALF_UP))
				.unit(view.getUnit())
				.bomSeq(view.getBomSeq())
				.build();
	}
}
