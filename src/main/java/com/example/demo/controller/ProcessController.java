package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ProcessResponse;
import com.example.demo.service.ProcessService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/processes")
@RequiredArgsConstructor
public class ProcessController {

	private final ProcessService processService;

	@GetMapping
	public List<ProcessResponse> list() {
		return processService.findAll();
	}
}
