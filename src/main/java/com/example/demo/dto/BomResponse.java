package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.domain.Bom;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BomResponse {

	private String bomId;
	private String bomVersion;
	private String itemId;
	private String itemName;
	private String useYn;
	private String creId;
	private LocalDateTime creDt;

	public static BomResponse fromHeader(Bom bom, String itemName) {
		return BomResponse.builder()
				.bomId(bom.getBomId())
				.bomVersion(bom.getBomVersion())
				.itemId(bom.getItemId())
				.itemName(itemName)
				.useYn(bom.getUseYn())
				.creId(bom.getCreId())
				.creDt(bom.getCreDt())
				.build();
	}
}
