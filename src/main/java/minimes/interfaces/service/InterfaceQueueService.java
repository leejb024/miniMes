package minimes.interfaces.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import minimes.interfaces.IfCodes;
import minimes.interfaces.InterfaceQueueEvent;
import minimes.interfaces.WmsInboundGateway;
import minimes.interfaces.WmsInboundPayload;
import minimes.interfaces.dto.InterfaceQueueResponse;
import minimes.interfaces.repository.InterfaceQueueRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.interfaces.domain.InterfaceQueue;
import minimes.master.domain.Item;
import minimes.master.domain.Warehouse;
import minimes.master.repository.ItemRepository;
import minimes.production.domain.ProdResult;
import minimes.production.domain.WorkOrder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterfaceQueueService {

	private final InterfaceQueueRepository interfaceQueueRepository;
	private final ItemRepository itemRepository;
	private final WmsInboundGateway wmsInboundGateway;
	private final JsonMapper jsonMapper;
	private final ApplicationEventPublisher eventPublisher;

	@Value("${app.wms.fail-on-send:false}")
	private boolean failOnSend;

	@Transactional
	public List<InterfaceQueue> queueWmsInbound(WorkOrder workOrder, Warehouse warehouse, List<ProdResult> results) {
		if (results == null || results.isEmpty()) {
			return List.of();
		}

		List<String> messageIds = new ArrayList<>();
		for (ProdResult result : results) {
			messageIds.add(IfCodes.prodInboundMessageId(result.getProdResultSeq()));
		}

		Set<String> existingIds = new HashSet<>();
		for (InterfaceQueue existing : interfaceQueueRepository.findByMessageIdIn(messageIds)) {
			existingIds.add(existing.getMessageId());
		}

		String itemName = itemName(workOrder.getItemId());
		String unit = StringUtils.hasText(workOrder.getUnit()) ? workOrder.getUnit() : "Kg";
		LocalDate planDate = LocalDate.now();
		List<InterfaceQueue> toSave = new ArrayList<>();
		for (ProdResult result : results) {
			String messageId = IfCodes.prodInboundMessageId(result.getProdResultSeq());
			if (existingIds.contains(messageId)) {
				continue;
			}
			WmsInboundPayload payload = WmsInboundPayload.builder()
					.messageId(messageId)
					.prodResultSeq(result.getProdResultSeq())
					.workOrderId(workOrder.getWorkOrderId())
					.itemId(workOrder.getItemId())
					.itemName(itemName)
					.lotNo(result.getLotId())
					.qty(result.getProdQty())
					.unit(unit)
					.warehouseId(warehouse.getWarehouseId())
					.warehouseName(warehouse.getWarehouseName())
					.planDate(planDate)
					.build();
			toSave.add(InterfaceQueue.builder()
					.messageId(messageId)
					.targetSystem(IfCodes.TARGET_WMS)
					.ifType(IfCodes.TYPE_PROD_INBOUND)
					.status(IfCodes.STATUS_WAIT)
					.payload(writePayload(payload))
					.retryCnt(0)
					.build());
		}

		if (toSave.isEmpty()) {
			return List.of();
		}

		List<InterfaceQueue> saved = interfaceQueueRepository.saveAll(toSave);
		List<Long> ifSeqs = new ArrayList<>();
		for (InterfaceQueue queue : saved) {
			ifSeqs.add(queue.getIfSeq());
		}
		eventPublisher.publishEvent(new InterfaceQueueEvent(ifSeqs));
		return saved;
	}

	@Transactional(readOnly = true)
	public List<InterfaceQueueResponse> findLogs(String status) {
		List<InterfaceQueue> rows = StringUtils.hasText(status)
				? interfaceQueueRepository.findByStatusOrderByIfSeqDesc(status.trim().toUpperCase())
				: interfaceQueueRepository.findAllByOrderByIfSeqDesc();
		List<InterfaceQueueResponse> responses = new ArrayList<>();
		for (InterfaceQueue row : rows) {
			responses.add(InterfaceQueueResponse.from(row));
		}
		return responses;
	}

	@Transactional(readOnly = true)
	public List<Long> findWaitingSeqs() {
		List<Long> seqs = new ArrayList<>();
		for (InterfaceQueue queue : interfaceQueueRepository.findByStatusOrderByIfSeqAsc(IfCodes.STATUS_WAIT)) {
			seqs.add(queue.getIfSeq());
		}
		return seqs;
	}

	@Transactional
	public InterfaceQueueResponse retry(String messageId) {
		InterfaceQueue queue = interfaceQueueRepository.findByMessageIdForUpdate(messageId.trim())
				.orElseThrow(() -> new IllegalArgumentException("전송 로그를 찾을 수 없습니다."));
		sendLocked(queue);
		return InterfaceQueueResponse.from(queue);
	}

	@Transactional
	public void send(Long ifSeq) {
		InterfaceQueue queue = interfaceQueueRepository.findByIdForUpdate(ifSeq).orElse(null);
		if (queue == null) {
			return;
		}
		sendLocked(queue);
	}

	private void sendLocked(InterfaceQueue queue) {
		if (IfCodes.STATUS_SUCCESS.equals(queue.getStatus())) {
			return;
		}

		try {
			if (failOnSend) {
				throw new IllegalStateException("WMS 연동 실패 시뮬레이션");
			}
			WmsInboundPayload payload = jsonMapper.readValue(queue.getPayload(), WmsInboundPayload.class);
			wmsInboundGateway.receiveInbound(payload);
			queue.setStatus(IfCodes.STATUS_SUCCESS);
			queue.setSentDt(LocalDateTime.now());
			queue.setLastError(null);
		} catch (Exception e) {
			queue.setStatus(IfCodes.STATUS_FAIL);
			queue.setRetryCnt((queue.getRetryCnt() == null ? 0 : queue.getRetryCnt()) + 1);
			queue.setLastError(trimError(e.getMessage()));
			log.warn("WMS 전송 실패 messageId={}", queue.getMessageId(), e);
		}
	}

	private String writePayload(WmsInboundPayload payload) {
		try {
			return jsonMapper.writeValueAsString(payload);
		} catch (Exception e) {
			throw new IllegalStateException("WMS 전송 페이로드를 만들 수 없습니다.", e);
		}
	}

	private String itemName(String itemId) {
		if (!StringUtils.hasText(itemId)) {
			return itemId;
		}
		Item item = itemRepository.findById(itemId).orElse(null);
		if (item == null || !StringUtils.hasText(item.getItemName())) {
			return itemId;
		}
		return item.getItemName();
	}

	private String trimError(String message) {
		if (!StringUtils.hasText(message)) {
			return "WMS 전송에 실패했습니다.";
		}
		return message.length() > 1000 ? message.substring(0, 1000) : message;
	}
}
