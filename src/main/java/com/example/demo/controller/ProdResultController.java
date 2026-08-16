package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ProdResultDetailResponse;
import com.example.demo.dto.ProdResultResponse;
import com.example.demo.service.ProdResultService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/prod-results")
@RequiredArgsConstructor
public class ProdResultController {

	private final ProdResultService prodResultService;

	@GetMapping
	public List<ProdResultResponse> list(@RequestParam String workOrderId) {
		return prodResultService.findGoodResults(workOrderId);
	}

	@GetMapping("/details")
	public List<ProdResultDetailResponse> details(
			@RequestParam String workOrderId,
			@RequestParam String lotId) {
		return prodResultService.findGoodResultDetails(workOrderId, lotId);
	}
}
