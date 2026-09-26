package minimes.purchase.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import minimes.purchase.dto.PurchaseOrderRequest;
import minimes.purchase.dto.PurchaseOrderResponse;
import minimes.purchase.service.PurchaseOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

	private final PurchaseOrderService purchaseOrderService;

	@GetMapping
	public List<PurchaseOrderResponse> list() {
		return purchaseOrderService.findAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PurchaseOrderResponse create(@Valid @RequestBody PurchaseOrderRequest request) {
		return purchaseOrderService.create(request);
	}

	@PutMapping("/{poNo}")
	public PurchaseOrderResponse update(
			@PathVariable String poNo,
			@Valid @RequestBody PurchaseOrderRequest request) {
		return purchaseOrderService.update(poNo, request);
	}

	@DeleteMapping("/{poNo}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String poNo) {
		purchaseOrderService.delete(poNo);
	}
}
