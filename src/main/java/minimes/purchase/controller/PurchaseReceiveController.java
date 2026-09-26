package minimes.purchase.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import minimes.purchase.dto.PurchaseLotRequest;
import minimes.purchase.dto.PurchaseLotResponse;
import minimes.purchase.dto.PurchaseQtyRequest;
import minimes.purchase.dto.PurchaseReturnRequest;
import minimes.purchase.dto.PurchaseScanRequest;
import minimes.purchase.dto.PurchaseScanResponse;
import minimes.purchase.service.PurchaseReceiveService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
public class PurchaseReceiveController {

	private final PurchaseReceiveService purchaseReceiveService;

	@GetMapping("/lots")
	public List<PurchaseLotResponse> lots(
			@RequestParam(required = false) String lotType,
			@RequestParam(required = false) String poNo,
			@RequestParam(required = false) String sourceLotNo) {
		return purchaseReceiveService.findLots(lotType, poNo, sourceLotNo);
	}

	@PostMapping("/scanInfo")
	@ResponseStatus(HttpStatus.CREATED)
	public PurchaseScanResponse scanInfo(@Valid @RequestBody PurchaseScanRequest request) {
		return purchaseReceiveService.scanInfo(request);
	}

	@PostMapping("/createLot")
	@ResponseStatus(HttpStatus.CREATED)
	public PurchaseLotResponse createLot(@Valid @RequestBody PurchaseLotRequest request) {
		return purchaseReceiveService.createLot(request);
	}

	@PutMapping("/updateQty")
	public PurchaseLotResponse updateQty(@Valid @RequestBody PurchaseQtyRequest request) {
		return purchaseReceiveService.updateQty(request);
	}

	@DeleteMapping("/deleteQty")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteQty(@RequestParam Long lotSeq) {
		purchaseReceiveService.deleteQty(lotSeq);
	}

	@PostMapping("/returnLot")
	@ResponseStatus(HttpStatus.CREATED)
	public PurchaseLotResponse returnLot(@Valid @RequestBody PurchaseReturnRequest request) {
		return purchaseReceiveService.returnLot(request);
	}

	@DeleteMapping("/delete/ReturnQty")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteReturnQty(@RequestParam Long lotSeq) {
		purchaseReceiveService.deleteReturnQty(lotSeq);
	}
}
