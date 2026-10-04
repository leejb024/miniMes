package minimes.production.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductionSheetResponse {

	private String workOrderId;
	private String inputComment;
	private String workComment;
	private String finalLotNo;
	private List<ProdInputLotResponse> inputs;
}
