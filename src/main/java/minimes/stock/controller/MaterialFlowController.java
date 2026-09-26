package minimes.stock.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import minimes.security.UserPrincipal;
import minimes.stock.dto.MaterialOrderActionRequest;
import minimes.stock.dto.MaterialOrderCreateRequest;
import minimes.stock.dto.MaterialOrderResponse;
import minimes.stock.service.MaterialFlowService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class MaterialFlowController {

	private final MaterialFlowService materialFlowService;

	@GetMapping("/materialOrder")
	public List<MaterialOrderResponse> list() {
		return materialFlowService.findAll();
	}

	@PostMapping("/materialOrder")
	@ResponseStatus(HttpStatus.CREATED)
	public MaterialOrderResponse create(
			@Valid @RequestBody MaterialOrderCreateRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return materialFlowService.createOrder(request, actor(principal));
	}

	@PostMapping("/moveRequest")
	public MaterialOrderResponse moveRequest(
			@Valid @RequestBody MaterialOrderActionRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return materialFlowService.moveRequest(request, actor(principal));
	}

	@PostMapping("/pickorder")
	public MaterialOrderResponse pickOrder(
			@Valid @RequestBody MaterialOrderActionRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return materialFlowService.pickOrder(request, actor(principal));
	}

	@PostMapping("/moveinfo")
	public MaterialOrderResponse moveInfo(
			@Valid @RequestBody MaterialOrderActionRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return materialFlowService.saveMoveInfo(request, actor(principal));
	}

	@PostMapping("/in/moveinfo")
	public MaterialOrderResponse inMoveInfo(
			@Valid @RequestBody MaterialOrderActionRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return materialFlowService.saveInMoveInfo(request, actor(principal));
	}

	@PostMapping("/material/receive")
	public MaterialOrderResponse receive(
			@Valid @RequestBody MaterialOrderActionRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return materialFlowService.confirmReceive(request, actor(principal));
	}

	@PostMapping("/material/dispatch")
	public MaterialOrderResponse dispatch(
			@Valid @RequestBody MaterialOrderActionRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return materialFlowService.dispatch(request, actor(principal));
	}

	@PostMapping("/material/move")
	public MaterialOrderResponse move(
			@Valid @RequestBody MaterialOrderActionRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return materialFlowService.move(request, actor(principal));
	}

	@PostMapping("/input/material/lot/weighing")
	public MaterialOrderResponse weigh(
			@Valid @RequestBody MaterialOrderActionRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return materialFlowService.weigh(request, actor(principal));
	}

	@PostMapping("/input/material/lot")
	public MaterialOrderResponse inputLot(
			@Valid @RequestBody MaterialOrderActionRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return materialFlowService.registerInputLot(request, actor(principal));
	}

	@PostMapping("/input/combine/result")
	public MaterialOrderResponse combine(
			@Valid @RequestBody MaterialOrderActionRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return materialFlowService.saveCombineResult(request, actor(principal));
	}

	@PostMapping("/input/material/lot/change")
	public MaterialOrderResponse changeLot(
			@Valid @RequestBody MaterialOrderActionRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return materialFlowService.changeInputLot(request, actor(principal));
	}

	private String actor(UserPrincipal principal) {
		return principal == null ? "admin" : principal.getUsername();
	}
}
