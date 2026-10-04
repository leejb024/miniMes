package minimes.production.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductionSheetRequest {

	private String workOrderId;
	private Long inputLotSeq;
	private Long stockSeq;
	private Long prodResultSeq;
	private BigDecimal qty;
	private BigDecimal weighQty;
	private BigDecimal prodQty;
	private String lotNo;
	private String lotId;
	private String comment;
	private String combineResult;
	private String resultType;
	private String finalLotNo;
	private LocalDateTime productionStartTime;
	private LocalDateTime productionEndTime;
}
