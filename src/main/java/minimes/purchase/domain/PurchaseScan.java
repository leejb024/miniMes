package minimes.purchase.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TB_PURCHASE_SCAN")
public class PurchaseScan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "SCAN_SEQ")
	private Long scanSeq;

	@Column(name = "BARCODE", length = 100, nullable = false)
	private String barcode;

	@Column(name = "PO_NO", length = 50)
	private String poNo;

	@Column(name = "PO_ITEM_SEQ")
	private Long poItemSeq;

	@Column(name = "ITEM_ID", length = 50)
	private String itemId;

	@Column(name = "ITEM_NAME", length = 100)
	private String itemName;

	@Column(name = "UNIT", length = 20)
	private String unit;

	@Column(name = "USED_YN", length = 1)
	private String usedYn;

	@Column(name = "SCAN_DT")
	private LocalDateTime scanDt;

	@PrePersist
	void onCreate() {
		if (scanDt == null) {
			scanDt = LocalDateTime.now();
		}
		if (usedYn == null) {
			usedYn = "N";
		}
	}
}
