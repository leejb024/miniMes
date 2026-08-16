package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.WarehouseResponse;
import com.example.demo.repository.WarehouseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WarehouseService {

	private final WarehouseRepository warehouseRepository;

	@Transactional(readOnly = true)
	public List<WarehouseResponse> findAll() {
		return warehouseRepository.findAllByOrderByWarehouseIdAsc().stream()
				.map(WarehouseResponse::from)
				.toList();
	}
}
