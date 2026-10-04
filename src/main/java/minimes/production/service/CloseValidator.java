package minimes.production.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import minimes.production.domain.ProdResult;
import minimes.production.domain.WorkOrder;
import minimes.production.repository.ProdResultRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CloseValidator {

	private final ProdResultRepository prodResultRepository;

	public void checkWorkOrder(WorkOrder workOrder) {
		if (isWorkOrderClosed(workOrder)) {
			throw new IllegalStateException("마감된 작업지시는 수정하거나 삭제할 수 없습니다.");
		}
	}

	public void checkProdResult(ProdResult result, WorkOrder workOrder) {
		if (isWorkOrderClosed(workOrder) || isConfirmed(result)) {
			throw new IllegalStateException("마감된 실적은 수정하거나 삭제할 수 없습니다.");
		}
	}

	public boolean isWorkOrderClosed(WorkOrder workOrder) {
		if (workOrder == null) {
			return false;
		}
		if ("Y".equalsIgnoreCase(workOrder.getCloseYn())) {
			return true;
		}
		boolean hasPending = !prodResultRepository
				.findByWorkOrderIdAndIsConfirmedOrderByProdResultSeqAsc(workOrder.getWorkOrderId(), "N")
				.isEmpty();
		if (hasPending) {
			return false;
		}
		return prodResultRepository.findByWorkOrderIdOrderByLotIdAscProdResultSeqAsc(workOrder.getWorkOrderId())
				.stream()
				.anyMatch(this::isConfirmed);
	}

	public boolean isConfirmed(ProdResult result) {
		if (result == null) {
			return false;
		}
		return !StringUtils.hasText(result.getIsConfirmed()) || !"N".equalsIgnoreCase(result.getIsConfirmed());
	}
}
