package minimes.production.dto;

import java.math.BigDecimal;

import minimes.production.domain.ProdMaterialInput;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProdMaterialInputResponse {

	private Long inputSeq;
	private String materialId;
	private String itemName;
	private BigDecimal qty;
	private String unit;
	private String lotId;
	private String materialLotNo;

	public static ProdMaterialInputResponse from(ProdMaterialInput input) {
		return ProdMaterialInputResponse.builder()
				.inputSeq(input.getInputSeq())
				.materialId(input.getMaterialId())
				.itemName(input.getMaterialName())
				.qty(input.getQty())
				.unit(input.getUnit())
				.lotId(input.getLotId())
				.materialLotNo(input.getMaterialLotNo())
				.build();
	}
}
