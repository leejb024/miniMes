package minimes.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "TB_PROD_RESULT")
public class ProdResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PROD_RESULT_SEQ")
	private Long prodResultSeq;

	@Column(name = "WORK_ORDER_ID", length = 50)
	private String workOrderId;

	@Column(name = "PLANT_ID", length = 50)
	private String plantId;

	@Column(name = "LOT_ID", length = 50)
	private String lotId;

	@Column(name = "PROD_QTY")
	private BigDecimal prodQty;

	@Column(name = "PRODUCTION_START_TIME")
	private LocalDateTime productionStartTime;

	@Column(name = "PRODUCTION_END_TIME")
	private LocalDateTime productionEndTime;

	@Column(name = "PRODUCTION_NO", length = 50)
	private String productionNo;

	@Column(name = "WMS_PROD_QTY")
	private BigDecimal wmsProdQty;

	@Column(name = "WMS_CONFIRM_QTY")
	private BigDecimal wmsConfirmQty;
}
