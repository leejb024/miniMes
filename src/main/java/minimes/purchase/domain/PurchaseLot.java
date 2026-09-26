package minimes.purchase.domain;

import java.math.BigDecimal;
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
@Table(name = "TB_PURCHASE_LOT")
public class PurchaseLot {

	public static final String TYPE_INBOUND = "INBOUND";
	public static final String TYPE_RETURN = "RETURN";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "LOT_SEQ")
	private Long lotSeq;

	@Column(name = "LOT_NO", length = 50, nullable = false)
	private String lotNo;

	@Column(name = "LOT_TYPE", length = 20, nullable = false)
	private String lotType;

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

	@Column(name = "QTY")
	private BigDecimal qty;

	@Column(name = "WAREHOUSE_ID", length = 50)
	private String warehouseId;

	@Column(name = "WAREHOUSE_NAME", length = 100)
	private String warehouseName;

	@Column(name = "BARCODE", length = 100)
	private String barcode;

	@Column(name = "SOURCE_LOT_NO", length = 50)
	private String sourceLotNo;

	@Column(name = "REMARK", length = 200)
	private String remark;

	@Column(name = "CRE_DT")
	private LocalDateTime creDt;

	@PrePersist
	void onCreate() {
		if (creDt == null) {
			creDt = LocalDateTime.now();
		}
	}
}
