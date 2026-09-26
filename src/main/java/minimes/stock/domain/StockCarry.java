package minimes.stock.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "TB_STOCK_CARRY")
public class StockCarry {

	public static final String TYPE_CARRY = "CARRY";
	public static final String TYPE_BRING = "BRING";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CARRY_SEQ")
	private Long carrySeq;

	@Column(name = "CARRY_TYPE", length = 20, nullable = false)
	private String carryType;

	@Column(name = "BASE_DATE", nullable = false)
	private LocalDate baseDate;

	@Column(name = "STOCK_SEQ")
	private Long stockSeq;

	@Column(name = "SOURCE_CARRY_SEQ")
	private Long sourceCarrySeq;

	@Column(name = "WAREHOUSE_ID", length = 50)
	private String warehouseId;

	@Column(name = "WAREHOUSE_NAME", length = 100)
	private String warehouseName;

	@Column(name = "ITEM_ID", length = 50)
	private String itemId;

	@Column(name = "ITEM_NAME", length = 100)
	private String itemName;

	@Column(name = "LOT_NO", length = 50)
	private String lotNo;

	@Column(name = "UNIT", length = 20)
	private String unit;

	@Column(name = "QTY")
	private BigDecimal qty;

	@Column(name = "REMARK", length = 200)
	private String remark;

	@Column(name = "CRE_ID", length = 50)
	private String creId;

	@Column(name = "CRE_DT")
	private LocalDateTime creDt;

	@PrePersist
	void onCreate() {
		if (creDt == null) {
			creDt = LocalDateTime.now();
		}
	}
}
