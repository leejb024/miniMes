package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.WorkOrderResponse;
import com.example.demo.repository.WorkOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

	private final WorkOrderRepository workOrderRepository;

	@Transactional(readOnly = true)
	public List<WorkOrderResponse> search(String deptId, LocalDate planDateFrom, LocalDate planDateTo) {
		return workOrderRepository.search(deptId, planDateFrom, planDateTo).stream()
				.map(WorkOrderResponse::from)
				.toList();
	}
}
