package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.WorkcenterResponse;
import com.example.demo.service.WorkcenterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workcenters")
@RequiredArgsConstructor
public class WorkcenterController {

	private final WorkcenterService workcenterService;

	@GetMapping
	public List<WorkcenterResponse> list() {
		return workcenterService.findAll();
	}
}
