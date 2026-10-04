package minimes.production.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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

	@Column(name = "PROCESS_ID", length = 50)
	private String processId;

	@Column(name = "EQUIP_ID", length = 50)
	private String equipId;

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

	@Column(name = "CLOSE_YN", length = 1)
	private String closeYn;

	@Column(name = "INPUT_COMMENT", length = 200)
	private String inputComment;

	@Column(name = "WORK_COMMENT", length = 200)
	private String workComment;

	@Column(name = "FINAL_LOT_NO", length = 50)
	private String finalLotNo;
}
