package minimes.stock.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import minimes.security.UserPrincipal;
import minimes.stock.dto.StockCarryRequest;
import minimes.stock.dto.StockCarryResponse;
import minimes.stock.dto.StockCloseRequest;
import minimes.stock.dto.StockCloseResponse;
import minimes.stock.service.StockCloseService;
import minimes.stock.service.StockService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventory/stock")
@RequiredArgsConstructor
public class InventoryStockController {

	private final StockService stockService;
	private final StockCloseService stockCloseService;

	@GetMapping("/carry")
	public List<StockCarryResponse> carries(@RequestParam(required = false) Long stockSeq) {
		return stockService.findCarries(stockSeq);
	}

	@PostMapping("/carry")
	@ResponseStatus(HttpStatus.CREATED)
	public StockCarryResponse carry(
			@Valid @RequestBody StockCarryRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		String creId = principal == null ? "admin" : principal.getUsername();
		return stockService.carry(request, creId);
	}

	@GetMapping("/close")
	public List<StockCloseResponse> closes() {
		return stockCloseService.findAll();
	}

	@PostMapping("/close")
	@ResponseStatus(HttpStatus.CREATED)
	public StockCloseResponse close(
			@Valid @RequestBody StockCloseRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		String creId = principal == null ? "admin" : principal.getUsername();
		return stockCloseService.close(request, creId);
	}
}
