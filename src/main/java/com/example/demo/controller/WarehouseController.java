package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.WarehouseCreateRequest;
import com.example.demo.dto.WarehouseResponse;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.WarehouseService;

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
