package minimes.production.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import minimes.production.dto.ProdInputLotResponse;
import minimes.production.dto.ProdResultSuggestResponse;
import minimes.production.dto.ProductionSheetRequest;
import minimes.production.dto.ProductionSheetResponse;
import minimes.production.service.ProductionSheetService;
import minimes.stock.dto.StockResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
public class ProductionSheetController {

	private final ProductionSheetService productionSheetService;

	@GetMapping("/sheet")
	public ProductionSheetResponse sheet(@RequestParam String workOrderId) {
		return productionSheetService.findSheet(workOrderId);
	}

	@GetMapping("/input/stocks")
	public List<StockResponse> inputStocks() {
		return productionSheetService.findInputStocks();
	}

	@PostMapping("/input/material/lot/weighing")
	public ProdInputLotResponse weigh(@RequestBody ProductionSheetRequest request) {
		return productionSheetService.weigh(request);
	}

	@PostMapping("/input/material")
	public ProdInputLotResponse inputMaterial(@RequestBody ProductionSheetRequest request) {
		return productionSheetService.inputMaterial(request);
	}

	@PostMapping("/input/material/lot")
	public ProdInputLotResponse registerLot(@RequestBody ProductionSheetRequest request) {
		return productionSheetService.registerLot(request);
	}

	@PostMapping("/input/combine/result")
	public ProdInputLotResponse combine(@RequestBody ProductionSheetRequest request) {
		return productionSheetService.combine(request);
	}

	@PostMapping("/input/material/lot/change")
	public ProdInputLotResponse changeLot(@RequestBody ProductionSheetRequest request) {
		return productionSheetService.changeLot(request);
	}

	@PostMapping("/update/workOrder/inputcomment")
	public void inputComment(@RequestBody ProductionSheetRequest request) {
		productionSheetService.updateInputComment(request);
	}

	@PostMapping("/update/workOrder/workcomment")
	public void workComment(@RequestBody ProductionSheetRequest request) {
		productionSheetService.updateWorkComment(request);
	}

	@PostMapping("/result/weighing")
	public void weighingResult(@RequestBody ProductionSheetRequest request) {
		productionSheetService.saveWeighingResult(request);
	}

	@GetMapping("/result/suggest")
	public ProdResultSuggestResponse suggestResult(@RequestParam String workOrderId) {
		return productionSheetService.suggestResult(workOrderId);
	}

	@PostMapping("/result")
	public void result(@RequestBody ProductionSheetRequest request) {
		productionSheetService.saveResult(request);
	}

	@PostMapping("/update/result")
	public void updateResult(@RequestBody ProductionSheetRequest request) {
		productionSheetService.updateResult(request);
	}

	@PostMapping("/finish/workorder")
	public void finish(@RequestBody ProductionSheetRequest request) {
		productionSheetService.finish(request);
	}

	@PostMapping("/lot")
	public void finalLot(@RequestBody ProductionSheetRequest request) {
		productionSheetService.saveFinalLot(request);
	}
}
