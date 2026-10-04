package minimes.production.domain;

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
@Table(name = "TB_PROD_INPUT_LOT")
public class ProdInputLot {

	public static final String WEIGHED = "WEIGHED";
	public static final String INPUT = "INPUT";
	public static final String COMBINED = "COMBINED";
	public static final String RETURNED = "RETURNED";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "INPUT_LOT_SEQ")
	private Long inputLotSeq;

	@Column(name = "WORK_ORDER_ID", length = 50, nullable = false)
	private String workOrderId;

	@Column(name = "STOCK_SEQ")
	private Long stockSeq;

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

	@Column(name = "WEIGH_QTY")
	private BigDecimal weighQty;

	@Column(name = "INPUT_QTY")
	private BigDecimal inputQty;

	@Column(name = "COMBINE_RESULT", length = 200)
	private String combineResult;

	@Column(name = "STATUS", length = 20)
	private String status;

	@Column(name = "CRE_DT")
	private LocalDateTime creDt;

	@PrePersist
	void onCreate() {
		if (creDt == null) {
			creDt = LocalDateTime.now();
		}
	}
}
