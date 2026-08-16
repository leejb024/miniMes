package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ProcessResponse;
import com.example.demo.repository.ProcessRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessService {

	private final ProcessRepository processRepository;

	@Transactional(readOnly = true)
	public List<ProcessResponse> findAll() {
		return processRepository.findAllByOrderByProcessIdAsc().stream()
				.map(ProcessResponse::from)
				.toList();
	}
}
