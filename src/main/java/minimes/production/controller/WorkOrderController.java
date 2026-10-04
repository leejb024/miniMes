package minimes.production.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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

import minimes.production.dto.WorkOrderRequest;
import minimes.production.dto.WorkOrderResponse;
import minimes.production.service.WorkOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

	private final WorkOrderService workOrderService;

	@GetMapping
	public List<WorkOrderResponse> list(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDateFrom,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDateTo) {
		return workOrderService.search(planDateFrom, planDateTo);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public WorkOrderResponse create(@Valid @RequestBody WorkOrderRequest request) {
		return workOrderService.create(request);
	}

	@PutMapping("/{workOrderId}")
	public WorkOrderResponse update(
			@PathVariable String workOrderId,
			@Valid @RequestBody WorkOrderRequest request) {
		return workOrderService.update(workOrderId, request);
	}

	@DeleteMapping("/{workOrderId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String workOrderId) {
		workOrderService.delete(workOrderId);
	}
}
