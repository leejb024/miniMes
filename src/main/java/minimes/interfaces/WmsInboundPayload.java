package minimes.interfaces;

import java.math.BigDecimal;
import java.time.LocalDate;

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
public class WmsInboundPayload {

	private String messageId;
	private Long prodResultSeq;
	private String workOrderId;
	private String itemId;
	private String itemName;
	private String lotNo;
	private BigDecimal qty;
	private String unit;
	private String warehouseId;
	private String warehouseName;
	private LocalDate planDate;
}
