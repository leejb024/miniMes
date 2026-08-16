package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.WorkcenterResponse;
import com.example.demo.repository.WorkcenterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkcenterService {

	private final WorkcenterRepository workcenterRepository;

	@Transactional(readOnly = true)
	public List<WorkcenterResponse> findAll() {
		return workcenterRepository.findAllByOrderByWorkcenterIdAsc().stream()
				.map(WorkcenterResponse::from)
				.toList();
	}
}
