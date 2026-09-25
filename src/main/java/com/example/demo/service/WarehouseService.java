package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.demo.domain.Warehouse;
import com.example.demo.dto.WarehouseCreateRequest;
import com.example.demo.dto.WarehouseResponse;
import com.example.demo.repository.WarehouseRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class WarehouseService {

	private final WarehouseRepository warehouseRepository;

	@Transactional(readOnly = true)
	public List<WarehouseResponse> findAll() {
        List<Warehouse> warehouses = warehouseRepository.findAllByOrderByWarehouseIdAsc();
        List<WarehouseResponse> responses = new ArrayList<>();
        for (Warehouse warehouse : warehouses) {
            responses.add(WarehouseResponse.from(warehouse));
        }
        return responses;
	}

	@Transactional
	public WarehouseResponse create(WarehouseCreateRequest request, String creId) {
		String warehouseId = request.getWarehouseId().trim();
		if (warehouseRepository.existsById(warehouseId)) {
			throw new IllegalArgumentException("이미 존재하는 창고ID입니다.");
		}
		Warehouse warehouse = new Warehouse();
		warehouse.setWarehouseId(warehouseId);
		warehouse.setWarehouseName(request.getWarehouseName().trim());
		warehouse.setWarehouseType(trimToNull(request.getWarehouseType()));
		warehouse.setUseYn(StringUtils.hasText(request.getUseYn()) ? request.getUseYn().trim() : "Y");
		warehouse.setCreId(StringUtils.hasText(creId) ? creId : "admin");
		warehouse.setCreDt(LocalDateTime.now());
		return WarehouseResponse.from(warehouseRepository.save(warehouse));
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
