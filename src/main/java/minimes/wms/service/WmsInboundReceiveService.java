package minimes.wms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import minimes.wms.domain.WmsInboundPlan;
import minimes.interfaces.WmsInboundPayload;
import minimes.wms.repository.WmsInboundPlanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WmsInboundReceiveService {

	private final WmsInboundPlanRepository wmsInboundPlanRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void accept(WmsInboundPayload payload) {
		if (wmsInboundPlanRepository.existsByMessageId(payload.getMessageId())) {
			log.info("WMS 입고예정 중복 수신을 무시합니다. messageId={}", payload.getMessageId());
			return;
		}

		wmsInboundPlanRepository.save(WmsInboundPlan.builder()
				.planNo(planNo(payload))
				.messageId(payload.getMessageId())
				.planDate(payload.getPlanDate())
				.workOrderId(payload.getWorkOrderId())
				.itemId(payload.getItemId())
				.itemName(payload.getItemName())
				.lotNo(payload.getLotNo())
				.qty(payload.getQty())
				.unit(payload.getUnit())
				.warehouseId(payload.getWarehouseId())
				.warehouseName(payload.getWarehouseName())
				.status("예정")
				.build());
	}

	private String planNo(WmsInboundPayload payload) {
		if (payload.getProdResultSeq() != null) {
			return "INB" + payload.getProdResultSeq();
		}
		return payload.getMessageId();
	}
}
