package minimes.production.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import minimes.production.dto.WorkOrderAmountRequest;
import minimes.production.dto.WorkOrderExpiredDateRequest;
import minimes.production.dto.WorkOrderResponse;
import minimes.production.service.WorkOrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "작업지시처리", description = "작업지시 수량·만기일·취소·시작")
@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
public class ProductionWorkOrderController {

	private final WorkOrderService workOrderService;

	@Operation(summary = "작업지시 수량 저장")
	@PostMapping("/WorkOrderAmount")
	public WorkOrderResponse saveAmount(@Valid @RequestBody WorkOrderAmountRequest request) {
		return workOrderService.saveAmount(request.getWorkOrderId(), request.getPlanQty());
	}

	@Operation(summary = "작업지시 수량 초기화")
	@PostMapping("/WorkOrderAmountReset")
	public WorkOrderResponse resetAmount(@RequestParam String workOrderId) {
		return workOrderService.resetAmount(workOrderId);
	}

	@Operation(summary = "작업지시 만기일 저장")
	@PostMapping("/workorderexpireddate")
	public WorkOrderResponse saveExpiredDate(@Valid @RequestBody WorkOrderExpiredDateRequest request) {
		return workOrderService.saveExpiredDate(request.getWorkOrderId(), request.getExpiredDate());
	}

	@Operation(summary = "작업지시 취소")
	@PostMapping("/cancelworkorder")
	public WorkOrderResponse cancel(@RequestParam String workOrderId) {
		return workOrderService.cancel(workOrderId);
	}

	@Operation(summary = "작업지시 시작")
	@PostMapping("/start/workorder")
	public WorkOrderResponse start(@RequestParam String workOrderId) {
		return workOrderService.start(workOrderId);
	}
}
