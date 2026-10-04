package minimes.production.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.master.domain.Bom;
import minimes.master.domain.Equipment;
import minimes.master.domain.Item;
import minimes.master.domain.Process;
import minimes.master.repository.BomRepository;
import minimes.master.repository.EquipmentRepository;
import minimes.master.repository.ItemRepository;
import minimes.master.repository.ProcessRepository;
import minimes.production.domain.WorkOrder;
import minimes.production.dto.ProdResultPostStatusView;
import minimes.production.dto.WorkOrderRequest;
import minimes.production.dto.WorkOrderResponse;
import minimes.production.dto.WorkOrderView;
import minimes.production.repository.ProdResultRepository;
import minimes.production.repository.WorkOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

public static final String STATE_START = "시작";
	public static final String STATE_MIX = "믹스시작";
	public static final String STATE_CANCEL = "취소";

	private static final DateTimeFormatter WORK_ORDER_DATE_PREFIX = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final WorkOrderRepository workOrderRepository;
	private final ProdResultRepository prodResultRepository;
	private final ItemRepository itemRepository;
	private final BomRepository bomRepository;
	private final ProcessRepository processRepository;
	private final EquipmentRepository equipmentRepository;
	private final CloseValidator closeValidator;

	@Transactional(readOnly = true)
	public List<WorkOrderResponse> search(LocalDate planDateFrom, LocalDate planDateTo) {
		List<WorkOrderView> views = workOrderRepository.search(planDateFrom, planDateTo);
		Map<String, ProdResultPostStatusView> statuses = loadResultStatuses(views);
		List<WorkOrderResponse> responses = new ArrayList<>();
		for (WorkOrderView view : views) {
			ProdResultPostStatusView status = statuses.get(view.getWorkOrderId());
			BigDecimal resultQty = status == null ? null : status.getProdQty();
			Long packCount = status == null ? null : status.getPackCount();
			responses.add(WorkOrderResponse.from(view, resultStatus(view, statuses), resultQty, packCount));
		}
		return responses;
	}

	@Transactional
	public WorkOrderResponse create(WorkOrderRequest request) {
		String workOrderId = getWorkOrderId(request);
		if (workOrderRepository.existsById(workOrderId)) {
			throw new IllegalArgumentException("이미 존재하는 작업지시번호입니다.");
		}

		WorkOrder workOrder = new WorkOrder();
		workOrder.setWorkOrderId(workOrderId);
		workOrder.setCloseYn("N");
		setParameter(workOrder, request);
		if (!StringUtils.hasText(workOrder.getState())) {
			workOrder.setState("작업지시");
		}
		workOrderRepository.save(workOrder);
		return toResponse(workOrder);
	}

	@Transactional
	public WorkOrderResponse update(String workOrderId, WorkOrderRequest request) {
		WorkOrder workOrder = checkEditable(workOrderId);
		setParameter(workOrder, request);
		workOrderRepository.save(workOrder);
		return toResponse(workOrder);
	}

	@Transactional
	public void delete(String workOrderId) {
		WorkOrder workOrder = checkEditable(workOrderId);
		if (!prodResultRepository.findByWorkOrderIdOrderByLotIdAscProdResultSeqAsc(workOrder.getWorkOrderId())
				.isEmpty()) {
			throw new IllegalStateException("실적이 있는 작업지시는 삭제할 수 없습니다.");
		}
		workOrderRepository.delete(workOrder);
	}

	@Transactional
	public WorkOrderResponse saveAmount(String workOrderId, BigDecimal planQty) {
		if (planQty == null || planQty.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("계획수량은 0보다 커야 합니다.");
		}
		WorkOrder workOrder = requireOpen(workOrderId);
		workOrder.setPlanQty(planQty);
		return toResponse(workOrder);
	}

	@Transactional
	public WorkOrderResponse resetAmount(String workOrderId) {
		WorkOrder workOrder = requireOpen(workOrderId);
		workOrder.setPlanQty(BigDecimal.ZERO);
		return toResponse(workOrder);
	}

	@Transactional
	public WorkOrderResponse saveExpiredDate(String workOrderId, LocalDate expiredDate) {
		if (expiredDate == null) {
			throw new IllegalArgumentException("만기일을 선택하세요.");
		}
		WorkOrder workOrder = requireOpen(workOrderId);
		if (workOrder.getPlanDate() != null && expiredDate.isBefore(workOrder.getPlanDate())) {
			throw new IllegalArgumentException("만기일은 계획일 이후여야 합니다.");
		}
		workOrder.setExpiredDate(expiredDate);
		return toResponse(workOrder);
	}

	@Transactional
	public WorkOrderResponse cancel(String workOrderId) {
		WorkOrder workOrder = requireOpen(workOrderId);
		if (STATE_CANCEL.equals(workOrder.getState())) {
			throw new IllegalStateException("이미 취소된 작업지시입니다.");
		}
		workOrder.setState(STATE_CANCEL);
		return toResponse(workOrder);
	}

	@Transactional
	public WorkOrderResponse start(String workOrderId) {
		WorkOrder workOrder = requireStartable(workOrderId);
		workOrder.setState(STATE_START);
		return toResponse(workOrder);
	}

	private Map<String, ProdResultPostStatusView> loadResultStatuses(List<WorkOrderView> views) {
		List<String> workOrderIds = new ArrayList<>();
		for (WorkOrderView view : views) {
			String workOrderId = view.getWorkOrderId();
			if (!workOrderIds.contains(workOrderId)) {
				workOrderIds.add(workOrderId);
			}
		}
		if (workOrderIds.isEmpty()) {
			return Map.of();
		}

		Map<String, ProdResultPostStatusView> statuses = new HashMap<>();
		for (ProdResultPostStatusView status : prodResultRepository.countPostStatusByWorkOrderIds(workOrderIds)) {
			if (!statuses.containsKey(status.getWorkOrderId())) {
				statuses.put(status.getWorkOrderId(), status);
			}
		}
		return statuses;
	}

	private String resultStatus(WorkOrderView view, Map<String, ProdResultPostStatusView> statuses) {
		if (view != null && "Y".equalsIgnoreCase(view.getCloseYn())) {
			return "완료";
		}
		ProdResultPostStatusView status = statuses.get(view.getWorkOrderId());
		if (status == null) {
			return "미등록";
		}
		long pendingCount = status.getPendingCount() == null ? 0L : status.getPendingCount();
		long resultCount = status.getResultCount() == null ? 0L : status.getResultCount();
		if (pendingCount > 0) {
			return "임시";
		}
		if (resultCount > 0) {
			return "완료";
		}
		return "미등록";
	}

	private WorkOrder requireOpen(String workOrderId) {
		WorkOrder workOrder = checkEditable(workOrderId);
		if (STATE_CANCEL.equals(workOrder.getState())) {
			throw new IllegalStateException("취소된 작업지시는 변경할 수 없습니다.");
		}
		if (STATE_START.equals(workOrder.getState()) || STATE_MIX.equals(workOrder.getState())) {
			throw new IllegalStateException("시작된 작업지시는 변경할 수 없습니다.");
		}
		return workOrder;
	}

	private WorkOrder requireStartable(String workOrderId) {
		WorkOrder workOrder = requireOpen(workOrderId);
		if (workOrder.getPlanQty() == null || workOrder.getPlanQty().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalStateException("계획수량이 없는 작업지시는 시작할 수 없습니다.");
		}
		return workOrder;
	}

	private WorkOrder checkEditable(String workOrderId) {
		WorkOrder workOrder = workOrderRepository.findById(workOrderId.trim())
				.orElseThrow(() -> new IllegalArgumentException("작업지시를 찾을 수 없습니다."));
		closeValidator.checkWorkOrder(workOrder);
		return workOrder;
	}

	private void setParameter(WorkOrder workOrder, WorkOrderRequest request) {
		Item item = getItem(request.getItemId());
		Bom bom = getBom(item.getItemId(), request.getItemVersion());
		Process process = requireOrderProcess(request.getProcessId());
		Equipment equipment = requireEquipment(request.getEquipId(), process.getProcessId());
		workOrder.setPlanDate(request.getPlanDate());
		workOrder.setProcessId(process.getProcessId());
		workOrder.setEquipId(equipment.getEquipId());
		workOrder.setItemId(item.getItemId());
		workOrder.setState(trimToNull(request.getState()));
		workOrder.setUnit(StringUtils.hasText(request.getUnit())
				? request.getUnit().trim()
				: (bom != null && StringUtils.hasText(bom.getUnit()) ? bom.getUnit() : "Kg"));
		workOrder.setPlanQty(request.getPlanQty());
		workOrder.setItemVersion(StringUtils.hasText(request.getItemVersion())
				? request.getItemVersion().trim()
				: (bom != null ? bom.getBomVersion() : null));
		workOrder.setPlantId(bom != null ? bom.getPlantId() : null);
	}

	private WorkOrderResponse toResponse(WorkOrder workOrder) {
		Bom bom = getBom(workOrder.getItemId(), workOrder.getItemVersion());
		return WorkOrderResponse.from(workOrder, bom != null ? bom.getBomVersion() : null);
	}

	private Item getItem(String itemId) {
		if (!StringUtils.hasText(itemId)) {
			throw new IllegalArgumentException("품목을 선택하세요.");
		}
		Item item = itemRepository.findById(itemId.trim())
				.orElseThrow(() -> new IllegalArgumentException("품목관리에 없는 품목입니다."));
		if (item.getUseYn() != null && !"Y".equalsIgnoreCase(item.getUseYn())) {
			throw new IllegalArgumentException("사용할 수 없는 품목입니다.");
		}
		if (!item.getItemId().startsWith("2")) {
			throw new IllegalArgumentException("제품(2번 코드)만 작업지시할 수 있습니다.");
		}
		return item;
	}

	private Process requireOrderProcess(String processId) {
		if (!StringUtils.hasText(processId)) {
			throw new IllegalArgumentException("공정을 선택하세요.");
		}
		Process process = processRepository.findById(processId.trim())
				.orElseThrow(() -> new IllegalArgumentException("공정관리에 없는 공정입니다."));
		if (process.getUseYn() != null && !"Y".equalsIgnoreCase(process.getUseYn())) {
			throw new IllegalArgumentException("사용할 수 없는 공정입니다.");
		}
		String name = process.getProcessName() == null ? "" : process.getProcessName();
		if (!name.contains("배합")) {
			throw new IllegalArgumentException("배합 공정을 선택하세요.");
		}
		return process;
	}

	private Equipment requireEquipment(String equipId, String processId) {
		if (!StringUtils.hasText(equipId)) {
			throw new IllegalArgumentException("설비를 선택하세요.");
		}
		Equipment equipment = equipmentRepository.findById(equipId.trim())
				.orElseThrow(() -> new IllegalArgumentException("설비관리에 없는 설비입니다."));
		if (!processId.equals(equipment.getProcessId())) {
			throw new IllegalArgumentException("선택한 공정에 맞는 설비를 선택하세요.");
		}
		return equipment;
	}

	private Bom getBom(String itemId, String itemVersion) {
		List<Bom> boms;
		if (StringUtils.hasText(itemVersion)) {
			String version = itemVersion.trim();
			boms = bomRepository.findByBomIdAndBomVersionAndUseYn(itemId, version, "Y");
			if (boms.isEmpty()) {
				boms = bomRepository.findByItemIdAndBomVersionAndUseYn(itemId, version, "Y");
			}
		} else {
			boms = bomRepository.findByBomIdAndUseYnOrderByBomSeqAsc(itemId, "Y");
			if (boms.isEmpty()) {
				boms = bomRepository.findByItemIdAndUseYnOrderByBomSeqAsc(itemId, "Y");
			}
		}
		if (boms.isEmpty()) {
			return null;
		}
		return boms.get(0);
	}

	private String getWorkOrderId(WorkOrderRequest request) {
		if (StringUtils.hasText(request.getWorkOrderId())) {
			return request.getWorkOrderId().trim();
		}

		String prefix = request.getPlanDate().format(WORK_ORDER_DATE_PREFIX);
		WorkOrder lastOrder = workOrderRepository
				.findTopByWorkOrderIdStartingWithOrderByWorkOrderIdDesc(prefix)
				.orElse(null);

		int nextSeq = 1;
		if (lastOrder != null) {
			String lastWorkOrderId = lastOrder.getWorkOrderId();
			nextSeq = Integer.parseInt(lastWorkOrderId.substring(prefix.length())) + 1;
		}
		return prefix + String.format("%04d", nextSeq);
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
