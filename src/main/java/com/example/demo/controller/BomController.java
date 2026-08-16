package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.BomItemResponse;
import com.example.demo.dto.BomResponse;
import com.example.demo.service.BomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
public class BomController {

	private final BomService bomService;

	@GetMapping
	public List<BomResponse> list() {
		return bomService.findHeaders();
	}

	@GetMapping("/materials")
	public List<BomItemResponse> materials(
			@RequestParam String itemId,
			@RequestParam String bomVersion) {
		return bomService.findMaterials(itemId, bomVersion);
	}
}
