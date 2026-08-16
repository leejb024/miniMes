package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ProdResultDetailResponse;
import com.example.demo.dto.ProdResultResponse;
import com.example.demo.repository.ProdResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdResultService {

	private final ProdResultRepository prodResultRepository;

	@Transactional(readOnly = true)
	public List<ProdResultResponse> findGoodResults(String workOrderId) {
		return prodResultRepository.findGoodResults(workOrderId).stream()
				.map(ProdResultResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ProdResultDetailResponse> findGoodResultDetails(String workOrderId, String lotId) {
		return prodResultRepository.findByWorkOrderIdAndLotIdOrderByProductionStartTimeAsc(workOrderId, lotId).stream()
				.map(ProdResultDetailResponse::from)
				.toList();
	}
}
