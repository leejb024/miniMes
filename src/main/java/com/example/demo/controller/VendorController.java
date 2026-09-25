package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.VendorCreateRequest;
import com.example.demo.dto.VendorResponse;
import com.example.demo.service.VendorService;
 
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 
@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

	private final VendorService vendorService;
 
	@GetMapping
	public List<VendorResponse> list() {
		return vendorService.findAll();
	}
 
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public VendorResponse create(@Valid @RequestBody VendorCreateRequest request) {
		return vendorService.create(request);
	}
}
