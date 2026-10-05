package minimes.master.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import minimes.master.dto.BomItemResponse;
import minimes.master.dto.BomResponse;
import minimes.master.dto.BomSaveRequest;
import minimes.master.service.BomService;
import minimes.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
public class BomController {

	private final BomService bomService;

	@GetMapping
	public List<BomResponse> list(
			@RequestParam(required = false) String itemId,
			@RequestParam(required = false) String itemName,
			@RequestParam(defaultValue = "false") boolean productsOnly) {
		return bomService.findHeaders(itemId, itemName, productsOnly);
	}

	@GetMapping("/materials")
	public List<BomItemResponse> materials(
			@RequestParam String itemId,
			@RequestParam String bomVersion) {
		return bomService.findMaterials(itemId, bomVersion);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BomResponse create(
			@Valid @RequestBody BomSaveRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return bomService.create(request, actor(principal));
	}

	@PutMapping
	public BomResponse update(
			@Valid @RequestBody BomSaveRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return bomService.update(request, actor(principal));
	}

	private String actor(UserPrincipal principal) {
		return principal == null ? "admin" : principal.getUsername();
	}
}
