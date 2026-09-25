package minimes.master.controller;

import java.util.List;

import minimes.master.dto.WarehouseCreateRequest;
import minimes.master.dto.WarehouseResponse;
import minimes.master.service.WarehouseService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import minimes.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

	private final WarehouseService warehouseService;

	@GetMapping
	public List<WarehouseResponse> list() {
		return warehouseService.findAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public WarehouseResponse create(
			@Valid @RequestBody WarehouseCreateRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		String creId = principal == null ? "admin" : principal.getUsername();
		return warehouseService.create(request, creId);
	}
}
