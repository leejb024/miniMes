package minimes.production.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.master.domain.Item;
import minimes.production.domain.ProdInputLot;
import minimes.production.domain.ProdMaterialInput;
import minimes.production.domain.ProdResult;
import minimes.stock.domain.Stock;
import minimes.master.domain.Warehouse;
import minimes.production.domain.WorkOrder;
import minimes.master.dto.BomMaterialView;
import minimes.production.dto.ProdMaterialInputResponse;
import minimes.production.dto.ProdResultCreateRequest;
import minimes.production.dto.ProdResultDetailResponse;
import minimes.production.dto.ProdResultResponse;
import minimes.production.dto.ProdResultSuggestResponse;
import minimes.production.dto.ProdResultUpdateRequest;
import minimes.master.repository.BomRepository;
import minimes.master.repository.ItemRepository;
import minimes.production.repository.ProdInputLotRepository;
import minimes.production.repository.ProdMaterialInputRepository;
import minimes.production.repository.ProdResultRepository;
import minimes.stock.repository.StockRepository;
import minimes.master.repository.WarehouseRepository;
import minimes.production.repository.WorkOrderRepository;
import minimes.interfaces.service.InterfaceQueueService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdResultService {

	private static final DateTimeFormatter LOT_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final ProdResultRepository prodResultRepository;
	private final ProdInputLotRepository prodInputLotRepository;
	private final ProdMaterialInputRepository prodMaterialInputRepository;
	private final WorkOrderRepository workOrderRepository;
	private final BomRepository bomRepository;
	private final StockRepository stockRepository;
	private final WarehouseRepository warehouseRepository;
	private final ItemRepository itemRepository;
	private final InterfaceQueueService interfaceQueueService;
	private final CloseValidator closeValidator;

	@Transactional(readOnly = true)
	public List<ProdResultResponse> findGoodResults(String workOrderId) {
		String unit = null;
		WorkOrder workOrder = workOrderRepository.findById(workOrderId).orElse(null);
		if (workOrder != null) {
			unit = workOrder.getUnit();
		}

		List<ProdResultResponse> responses = new ArrayList<>();
		for (ProdResult result : prodResultRepository.findByWorkOrderIdOrderByLotIdAscProdResultSeqAsc(workOrderId)) {
			responses.add(ProdResultResponse.from(result, unit));
		}
		return responses;
	}

	@Transactional(readOnly = true)
	public List<ProdMaterialInputResponse> findMaterialInputs(String workOrderId) {
		List<ProdMaterialInputResponse> responses = new ArrayList<>();
		for (ProdMaterialInput input : prodMaterialInputRepository.findByWorkOrderIdOrderByInputSeqAsc(workOrderId)) {
			responses.add(ProdMaterialInputResponse.from(input));
		}
		return responses;
	}

	@Transactional
	public void createGoodResult(ProdResultCreateRequest request) {
		WorkOrder workOrder = workOrderRepository.findById(request.getWorkOrderId().trim())
				.orElseThrow(() -> new IllegalArgumentException("작업지시를 찾을 수 없습니다."));
		closeValidator.checkWorkOrder(workOrder);

		String lotId = StringUtils.hasText(request.getLotId()) ? request.getLotId().trim() : nextFinishedLotNo();
		if (prodResultRepository.existsByLotId(lotId)) {
			lotId = nextFinishedLotNo();
		}
		BigDecimal prodQty = request.getProdQty();
		LocalDateTime start = request.getProductionStartTime();
		LocalDateTime end = request.getProductionEndTime();
		if (start == null || end == null) {
			LocalDateTime now = LocalDateTime.now();
			if (start == null) {
				start = now;
			}
			if (end == null) {
				end = now;
			}
		}
		if (end.isBefore(start)) {
			throw new IllegalArgumentException("종료 일시는 시작 일시 이후여야 합니다.");
		}

		ProdResult result = ProdResult.builder()
				.prodResultSeq(nextProdResultSeq())
				.workOrderId(workOrder.getWorkOrderId())
				.plantId(workOrder.getPlantId())
				.lotId(lotId)
				.prodQty(prodQty)
				.productionStartTime(start)
				.productionEndTime(end)
				.productionNo(workOrder.getWorkOrderId())
				.isConfirmed("N")
				.resultType(normalizeResultType(request.getResultType()))
				.build();
		prodResultRepository.save(result);
	}

	@Transactional
	public void updateGoodResult(Long prodResultSeq, ProdResultUpdateRequest request) {
		ProdResult result = prodResultRepository.findById(prodResultSeq)
				.orElseThrow(() -> new IllegalArgumentException("양품 실적을 찾을 수 없습니다."));
		WorkOrder workOrder = workOrderRepository.findById(result.getWorkOrderId())
				.orElseThrow(() -> new IllegalArgumentException("작업지시를 찾을 수 없습니다."));
		closeValidator.checkProdResult(result, workOrder);
		result.setLotId(request.getLotId().trim());
		result.setProdQty(request.getProdQty());
	}

	@Transactional
	public void deleteGoodResult(Long prodResultSeq) {
		ProdResult result = prodResultRepository.findById(prodResultSeq)
				.orElseThrow(() -> new IllegalArgumentException("양품 실적을 찾을 수 없습니다."));
		WorkOrder workOrder = workOrderRepository.findById(result.getWorkOrderId())
				.orElseThrow(() -> new IllegalArgumentException("작업지시를 찾을 수 없습니다."));
		closeValidator.checkProdResult(result, workOrder);
		prodResultRepository.delete(result);
	}

	@Transactional
	public void completeGoodResults(String workOrderId) {
		WorkOrder workOrder = workOrderRepository.findById(workOrderId.trim())
				.orElseThrow(() -> new IllegalArgumentException("작업지시를 찾을 수 없습니다."));
		if (closeValidator.isWorkOrderClosed(workOrder)) {
			throw new IllegalStateException("마감된 작업지시는 다시 마감할 수 없습니다.");
		}
		List<ProdResult> pending = prodResultRepository.findByWorkOrderIdAndIsConfirmedOrderByProdResultSeqAsc(
				workOrder.getWorkOrderId(),
				"N");
		if (pending.isEmpty()) {
			throw new IllegalArgumentException("마감할 임시 양품실적이 없습니다.");
		}

		Warehouse warehouse = getWareHouse();
		for (ProdResult result : pending) {
			increaseStock(workOrder, warehouse, result.getLotId(), result.getProdQty());
			consumeMaterials(workOrder, warehouse, result.getLotId(), result.getProdQty());
			result.setIsConfirmed("Y");
		}
		prodResultRepository.saveAll(pending);
		workOrder.setCloseYn("Y");
		interfaceQueueService.queueWmsInbound(workOrder, warehouse, pending);
	}

	private String normalizeResultType(String resultType) {
		if ("WEIGH".equalsIgnoreCase(resultType)) {
			return "WEIGH";
		}
		if ("PACK".equalsIgnoreCase(resultType)) {
			return "PACK";
		}
		return "RESULT";
	}

	private Long nextProdResultSeq() {
		Long maxSeq = prodResultRepository.findMaxSeq();
		return (maxSeq == null ? 0L : maxSeq) + 1L;
	}

	@Transactional(readOnly = true)
	public ProdResultSuggestResponse suggestResult(String workOrderId) {
		WorkOrder workOrder = workOrderRepository.findById(workOrderId.trim())
				.orElseThrow(() -> new IllegalArgumentException("작업지시를 찾을 수 없습니다."));
		String unit = StringUtils.hasText(workOrder.getUnit()) ? workOrder.getUnit() : "Kg";
		return ProdResultSuggestResponse.builder()
				.lotNo(nextFinishedLotNo())
				.prodQty(suggestProdQty(workOrder))
				.unit(unit)
				.build();
	}

	private BigDecimal suggestProdQty(WorkOrder workOrder) {
		Map<String, BigDecimal> inputByItem = new HashMap<>();
		for (ProdInputLot lot : prodInputLotRepository.findByWorkOrderIdOrderByInputLotSeqAsc(workOrder.getWorkOrderId())) {
			if (ProdInputLot.RETURNED.equals(lot.getStatus())) {
				continue;
			}
			BigDecimal qty = ProdInputLot.WEIGHED.equals(lot.getStatus()) ? lot.getWeighQty() : lot.getInputQty();
			if (qty == null || qty.signum() <= 0 || !StringUtils.hasText(lot.getItemId())) {
				continue;
			}
			inputByItem.merge(lot.getItemId(), qty, BigDecimal::add);
		}
		String version = StringUtils.hasText(workOrder.getItemVersion()) ? workOrder.getItemVersion() : "";
		List<BomMaterialView> materials = bomRepository.findMaterials(workOrder.getItemId(), version);
		BigDecimal suggested = null;
		for (BomMaterialView material : materials) {
			String materialId = material.getMaterialId();
			if (!StringUtils.hasText(materialId) || !materialId.startsWith("1")) {
				continue;
			}
			BigDecimal bomQty = material.getQty();
			BigDecimal inputQty = inputByItem.get(materialId);
			if (bomQty == null || bomQty.signum() <= 0 || inputQty == null) {
				continue;
			}
			BigDecimal possible = inputQty.divide(bomQty, 4, RoundingMode.HALF_UP).stripTrailingZeros();
			suggested = suggested == null ? possible : suggested.min(possible);
		}
		return suggested;
	}

	private String nextFinishedLotNo() {
		String head = "F" + LocalDate.now().format(LOT_DATE);
		for (int seq = 1; seq <= 9999; seq++) {
			String lotNo = head + String.format("%04d", seq);
			if (!prodResultRepository.existsByLotId(lotNo)) {
				return lotNo;
			}
		}
		throw new IllegalStateException("완제품 LOT 번호를 채번할 수 없습니다.");
	}

	private void increaseStock(
			WorkOrder workOrder,
			Warehouse warehouse,
			String lotId,
			BigDecimal prodQty) {
		String itemId = workOrder.getItemId();
		if (!StringUtils.hasText(itemId)) {
			return;
		}

		Stock stock = stockRepository
				.findFirstByWarehouseIdAndItemIdAndLotNo(warehouse.getWarehouseId(), itemId, lotId)
				.orElse(null);
		if (stock == null) {
			stockRepository.save(Stock.builder()
					.warehouseId(warehouse.getWarehouseId())
					.warehouseName(warehouse.getWarehouseName())
					.itemId(itemId)
					.itemName(itemName(itemId))
					.lotNo(lotId)
					.unit(StringUtils.hasText(workOrder.getUnit()) ? workOrder.getUnit() : "Kg")
					.qty(prodQty)
					.locationName(warehouse.getWarehouseName())
					.build());
			return;
		}
		stock.setQty(nullToZero(stock.getQty()).add(prodQty));
	}

	private void consumeMaterials(
			WorkOrder workOrder,
			Warehouse warehouse,
			String lotId,
			BigDecimal prodQty) {
		String bomVersion = StringUtils.hasText(workOrder.getItemVersion()) ? workOrder.getItemVersion() : "";
		List<BomMaterialView> materials = bomRepository.findMaterials(workOrder.getItemId(), bomVersion);
		for (BomMaterialView material : materials) {
			String materialId = material.getMaterialId();
			if (!StringUtils.hasText(materialId) || !materialId.startsWith("1")) {
				continue;
			}
			BigDecimal bomQty = nullToZero(material.getQty());
			BigDecimal consumeQty = bomQty.multiply(prodQty);
			if (consumeQty.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			deductMaterialStock(workOrder.getWorkOrderId(), lotId, warehouse, materialId, consumeQty, material.getUnit());
		}
	}

	private void deductMaterialStock(
			String workOrderId,
			String lotId,
			Warehouse warehouse,
			String materialId,
			BigDecimal consumeQty,
			String unit) {
		List<Stock> stocks = stockRepository.findByWarehouseIdAndItemIdOrderByStockSeqAsc(
				warehouse.getWarehouseId(),
				materialId);
		BigDecimal remain = consumeQty;
		String materialName = itemName(materialId);
		String materialUnit = StringUtils.hasText(unit) ? unit : "Kg";

		for (Stock stock : stocks) {
			if (remain.compareTo(BigDecimal.ZERO) <= 0) {
				break;
			}
			BigDecimal available = nullToZero(stock.getQty());
			if (available.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			BigDecimal take = available.min(remain);
			stock.setQty(available.subtract(take));
			remain = remain.subtract(take);
			prodMaterialInputRepository.save(ProdMaterialInput.builder()
					.workOrderId(workOrderId)
					.lotId(lotId)
					.materialId(materialId)
					.materialName(materialName)
					.qty(take)
					.unit(StringUtils.hasText(stock.getUnit()) ? stock.getUnit() : materialUnit)
					.materialLotNo(stock.getLotNo())
					.build());
		}

		if (remain.compareTo(BigDecimal.ZERO) > 0) {
			throw new IllegalArgumentException("자재 " + materialId + " 재고가 부족합니다.");
		}
	}

	private Warehouse getWareHouse() {
		for (Warehouse warehouse : warehouseRepository.findAllByOrderByWarehouseIdAsc()) {
			if (warehouse.getUseYn() != null && !"Y".equalsIgnoreCase(warehouse.getUseYn())) {
				continue;
			}
			String type = warehouse.getWarehouseType() == null ? "" : warehouse.getWarehouseType();
			if (type.contains("자재") || type.toUpperCase().contains("WMS")) {
				continue;
			}
			return warehouse;
		}
		throw new IllegalArgumentException("사용할 창고가 없습니다.");
	}

	private String itemName(String itemId) {
		Item item = itemRepository.findById(itemId).orElse(null);
		if (item != null && StringUtils.hasText(item.getItemName())) {
			return item.getItemName();
		}
		return itemId;
	}

	private BigDecimal nullToZero(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}
}
