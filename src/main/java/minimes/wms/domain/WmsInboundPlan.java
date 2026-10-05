package minimes.wms.domain;

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
@Table(name = "TB_WMS_INBOUND_PLAN")
public class WmsInboundPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PLAN_SEQ")
	private Long planSeq;

	@Column(name = "PLAN_NO", length = 50, nullable = false)
	private String planNo;

	@Column(name = "MESSAGE_ID", length = 80, nullable = false, unique = true)
	private String messageId;

	@Column(name = "PLAN_DATE")
	private LocalDate planDate;

	@Column(name = "WORK_ORDER_ID", length = 50)
	private String workOrderId;

	@Column(name = "ITEM_ID", length = 50)
	private String itemId;

	@Column(name = "ITEM_NAME", length = 100)
	private String itemName;

	@Column(name = "LOT_NO", length = 50)
	private String lotNo;

	@Column(name = "QTY")
	private BigDecimal qty;

	@Column(name = "UNIT", length = 20)
	private String unit;

	@Column(name = "WAREHOUSE_ID", length = 50)
	private String warehouseId;

	@Column(name = "WAREHOUSE_NAME", length = 100)
	private String warehouseName;

	@Column(name = "STATUS", length = 20)
	private String status;

	@Column(name = "CRE_DT")
	private LocalDateTime creDt;

	@PrePersist
	void onCreate() {
		if (creDt == null) {
			creDt = LocalDateTime.now();
		}
		if (status == null) {
			status = "예정";
		}
	}
}
