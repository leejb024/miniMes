package minimes.purchase.dto;

import java.time.LocalDateTime;

import minimes.purchase.domain.PurchaseScan;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchaseScanResponse {

	private Long scanSeq;
	private String barcode;
	private String poNo;
	private Long poItemSeq;
	private String itemId;
	private String itemName;
	private String unit;
	private String usedYn;
	private LocalDateTime scanDt;

	public static PurchaseScanResponse from(PurchaseScan scan) {
		return PurchaseScanResponse.builder()
				.scanSeq(scan.getScanSeq())
				.barcode(scan.getBarcode())
				.poNo(scan.getPoNo())
				.poItemSeq(scan.getPoItemSeq())
				.itemId(scan.getItemId())
				.itemName(scan.getItemName())
				.unit(scan.getUnit())
				.usedYn(scan.getUsedYn())
				.scanDt(scan.getScanDt())
				.build();
	}
}
