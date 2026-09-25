package minimes.production.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import minimes.production.dto.WorkOrderResponse;
import minimes.production.repository.WorkOrderRepository;

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
