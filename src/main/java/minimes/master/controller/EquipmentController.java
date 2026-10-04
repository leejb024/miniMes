package minimes.master.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import minimes.master.dto.EquipmentResponse;
import minimes.master.dto.EquipmentSaveRequest;
import minimes.master.service.EquipmentService;
import minimes.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/equipments")
@RequiredArgsConstructor
public class EquipmentController {

	private final EquipmentService equipmentService;

	@GetMapping
	public List<EquipmentResponse> list() {
		return equipmentService.findAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EquipmentResponse create(
			@Valid @RequestBody EquipmentSaveRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		String creId = principal == null ? "admin" : principal.getUsername();
		return equipmentService.create(request, creId);
	}

	@PutMapping
	public EquipmentResponse update(
			@Valid @RequestBody EquipmentSaveRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		String modId = principal == null ? "admin" : principal.getUsername();
		return equipmentService.update(request, modId);
	}
}
