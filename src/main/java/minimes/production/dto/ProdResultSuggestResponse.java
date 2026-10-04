package minimes.production.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProdResultSuggestResponse {

	private String lotNo;
	private BigDecimal prodQty;
	private String unit;
}
