package minimes.production.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import minimes.production.dto.ProdMaterialInputResponse;
import minimes.production.dto.ProdResultCompleteRequest;
import minimes.production.dto.ProdResultCreateRequest;
import minimes.production.dto.ProdResultResponse;
import minimes.production.dto.ProdResultUpdateRequest;
import minimes.production.service.ProdResultService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/prod-results")
@RequiredArgsConstructor
public class ProdResultController {

	private final ProdResultService prodResultService;

	@GetMapping
	public List<ProdResultResponse> list(
			@RequestParam String workOrderId) {
		return prodResultService.findGoodResults(workOrderId);
	}

	@GetMapping("/materials")
	public List<ProdMaterialInputResponse> materials(
			@RequestParam String workOrderId) {
		return prodResultService.findMaterialInputs(workOrderId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void create(@Valid @RequestBody ProdResultCreateRequest request) {
		prodResultService.createGoodResult(request);
	}

	@PostMapping("/complete")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void complete(@Valid @RequestBody ProdResultCompleteRequest request) {
		prodResultService.completeGoodResults(request.getWorkOrderId());
	}

	@PutMapping("/{prodResultSeq}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void update(
			@PathVariable Long prodResultSeq,
			@Valid @RequestBody ProdResultUpdateRequest request) {
		prodResultService.updateGoodResult(prodResultSeq, request);
	}

	@DeleteMapping("/{prodResultSeq}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long prodResultSeq) {
		prodResultService.deleteGoodResult(prodResultSeq);
	}
}
