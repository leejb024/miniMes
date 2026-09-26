package minimes.stock.domain;

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
@Table(name = "TB_MATERIAL_ORDER")
public class MaterialOrder {

	public static final String REQUEST = "REQUEST";
	public static final String MOVE_REQUEST = "MOVE_REQUEST";
	public static final String MOVING = "MOVING";
	public static final String MOVE_INFO = "MOVE_INFO";
	public static final String IN_MOVE = "IN_MOVE";
	public static final String DISPATCHED = "DISPATCHED";
	public static final String MOVED = "MOVED";
	public static final String RECEIVED = "RECEIVED";
	public static final String WEIGHED = "WEIGHED";
	public static final String INPUT = "INPUT";
	public static final String COMBINED = "COMBINED";
	public static final String LOT_CHANGED = "LOT_CHANGED";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ORDER_SEQ")
	private Long orderSeq;

	@Column(name = "ORDER_NO", length = 50, nullable = false)
	private String orderNo;

	@Column(name = "STATUS", length = 20, nullable = false)
	private String status;

	@Column(name = "STOCK_SEQ")
	private Long stockSeq;

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

	@Column(name = "FROM_WAREHOUSE_ID", length = 50)
	private String fromWarehouseId;

	@Column(name = "FROM_WAREHOUSE_NAME", length = 100)
	private String fromWarehouseName;

	@Column(name = "TO_WAREHOUSE_ID", length = 50)
	private String toWarehouseId;

	@Column(name = "TO_WAREHOUSE_NAME", length = 100)
	private String toWarehouseName;

	@Column(name = "APPROVED_YN", length = 1)
	private String approvedYn;

	@Column(name = "PICK_NO", length = 50)
	private String pickNo;

	@Column(name = "WEIGH_QTY")
	private BigDecimal weighQty;

	@Column(name = "INPUT_LOT_NO", length = 50)
	private String inputLotNo;

	@Column(name = "COMBINE_RESULT", length = 200)
	private String combineResult;

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
