package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.DeptResponse;
import com.example.demo.service.DeptService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/depts")
@RequiredArgsConstructor
public class DeptController {

	private final DeptService deptService;

	@GetMapping
	public List<DeptResponse> list() {
		return deptService.findAll();
	}
}
