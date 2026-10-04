package minimes.production.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "TB_PROD_RESULT")
public class ProdResult {

	@Id
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

	@Column(name = "IS_CONFIRMED", length = 1)
	private String isConfirmed;

	@Column(name = "RESULT_TYPE", length = 20)
	private String resultType;
}
