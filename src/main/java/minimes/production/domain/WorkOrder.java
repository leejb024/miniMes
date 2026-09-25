package minimes.production.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "TB_WORK_ORDER")
public class WorkOrder {

	@Id
	@Column(name = "WORK_ORDER_ID", length = 50)
	private String workOrderId;

	@Column(name = "PLAN_DATE")
	private LocalDate planDate;

	@Column(name = "WORKCENTER_ID", length = 50)
	private String workcenterId;

	@Column(name = "ITEM_ID", length = 50)
	private String itemId;

	@Column(name = "DEPT_ID", length = 50)
	private String deptId;

	@Column(name = "STATE", length = 20)
	private String state;

	@Column(name = "UNIT", length = 20)
	private String unit;

	@Column(name = "PLAN_QTY")
	private BigDecimal planQty;

	@Column(name = "EXPIRED_DATE")
	private LocalDate expiredDate;

	@Column(name = "ITEM_VERSION", length = 50)
	private String itemVersion;

	@Column(name = "PLANT_ID", length = 50)
	private String plantId;
}
