package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.DeptResponse;
import com.example.demo.repository.DeptRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeptService {

	private final DeptRepository deptRepository;

	@Transactional(readOnly = true)
	public List<DeptResponse> findAll() {
		return deptRepository.findAllByOrderByDeptIdAsc().stream()
				.map(DeptResponse::from)
				.toList();
	}
}
