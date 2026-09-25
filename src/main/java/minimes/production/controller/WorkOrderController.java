package minimes.production.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import minimes.production.dto.WorkOrderResponse;
import minimes.production.service.WorkOrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

	private final WorkOrderService workOrderService;

	@GetMapping
	public List<WorkOrderResponse> list(
			@RequestParam String deptId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDateFrom,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDateTo) {
		return workOrderService.search(deptId, planDateFrom, planDateTo);
	}
}
