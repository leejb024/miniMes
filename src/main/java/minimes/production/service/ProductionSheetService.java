package minimes.production.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.master.domain.Warehouse;
import minimes.master.repository.WarehouseRepository;
import minimes.production.domain.ProdInputLot;
import minimes.production.domain.WorkOrder;
import minimes.production.dto.ProdInputLotResponse;
import minimes.production.dto.ProdResultCreateRequest;
import minimes.production.dto.ProdResultSuggestResponse;
import minimes.production.dto.ProdResultUpdateRequest;
import minimes.production.dto.ProductionSheetRequest;
import minimes.production.dto.ProductionSheetResponse;
import minimes.production.repository.ProdInputLotRepository;
import minimes.production.repository.WorkOrderRepository;
import minimes.stock.domain.Stock;
import minimes.stock.dto.StockResponse;
import minimes.stock.repository.StockRepository;
import minimes.stock.service.StockService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductionSheetService {

	private final WorkOrderRepository workOrderRepository;
	private final ProdInputLotRepository prodInputLotRepository;
	private final StockRepository stockRepository;
	private final WarehouseRepository warehouseRepository;
	private final StockService stockService;
	private final ProdResultService prodResultService;
	private final CloseValidator closeValidator;

	@Transactional(readOnly = true)
	public ProductionSheetResponse findSheet(String workOrderId) {
		WorkOrder workOrder = requireWorkOrder(workOrderId);
		List<ProdInputLotResponse> inputs = new ArrayList<>();
		for (ProdInputLot lot : prodInputLotRepository.findByWorkOrderIdOrderByInputLotSeqAsc(workOrder.getWorkOrderId())) {
			inputs.add(ProdInputLotResponse.from(lot));
		}
		return ProductionSheetResponse.builder()
				.workOrderId(workOrder.getWorkOrderId())
				.inputComment(workOrder.getInputComment())
				.workComment(workOrder.getWorkComment())
				.finalLotNo(workOrder.getFinalLotNo())
				.inputs(inputs)
				.build();
	}

	@Transactional(readOnly = true)
	public List<StockResponse> findInputStocks() {
		Set<String> blocked = new HashSet<>();
		for (Warehouse warehouse : warehouseRepository.findAll()) {
			String type = warehouse.getWarehouseType() == null ? "" : warehouse.getWarehouseType();
			if (type.contains("자재") || type.toUpperCase().contains("WMS")) {
				blocked.add(warehouse.getWarehouseId());
			}
		}
		List<StockResponse> stocks = new ArrayList<>();
		for (StockResponse stock : stockService.findAll()) {
			if (!blocked.contains(stock.getWarehouseId()) && nullToZero(stock.getQty()).signum() > 0) {
				stocks.add(stock);
			}
		}
		return stocks;
	}

	@Transactional
	public ProdInputLotResponse weigh(ProductionSheetRequest request) {
		WorkOrder workOrder = requireOpen(request.getWorkOrderId());
		StockResponse stock = requireManufacturingStock(request.getStockSeq());
		BigDecimal qty = requirePositive(request.getWeighQty(), "계량 수량은 0보다 커야 합니다.");
		if (qty.compareTo(nullToZero(stock.getQty())) > 0) {
			throw new IllegalArgumentException("계량 수량이 창고 재고를 초과합니다.");
		}
		ProdInputLot saved = prodInputLotRepository.save(ProdInputLot.builder()
				.workOrderId(workOrder.getWorkOrderId())
				.stockSeq(stock.getStockSeq())
				.warehouseId(stock.getWarehouseId())
				.warehouseName(stock.getWarehouseName())
				.itemId(stock.getItemId())
				.itemName(stock.getItemName())
				.lotNo(stock.getLotNo())
				.unit(stock.getUnit())
				.weighQty(qty)
				.status(ProdInputLot.WEIGHED)
				.build());
		return ProdInputLotResponse.from(saved);
	}

	@Transactional
	public ProdInputLotResponse inputMaterial(ProductionSheetRequest request) {
		WorkOrder workOrder = requireOpen(request.getWorkOrderId());
		StockResponse stock = requireManufacturingStock(request.getStockSeq());
		BigDecimal qty = requirePositive(request.getWeighQty(), "투입 수량은 0보다 커야 합니다.");
		if (qty.compareTo(nullToZero(stock.getQty())) > 0) {
			throw new IllegalArgumentException("투입 수량이 창고 재고를 초과합니다.");
		}
		applyDelta(stock.getWarehouseId(), stock.getItemId(), stock.getLotNo(), qty.negate(), null);
		ProdInputLot saved = prodInputLotRepository.save(ProdInputLot.builder()
				.workOrderId(workOrder.getWorkOrderId())
				.stockSeq(stock.getStockSeq())
				.warehouseId(stock.getWarehouseId())
				.warehouseName(stock.getWarehouseName())
				.itemId(stock.getItemId())
				.itemName(stock.getItemName())
				.lotNo(stock.getLotNo())
				.unit(stock.getUnit())
				.weighQty(qty)
				.inputQty(qty)
				.status(ProdInputLot.INPUT)
				.build());
		return ProdInputLotResponse.from(saved);
	}

	@Transactional
	public ProdInputLotResponse registerLot(ProductionSheetRequest request) {
		ProdInputLot lot = requireInput(request.getInputLotSeq(), ProdInputLot.WEIGHED, "계량된 자재만 투입 LOT로 등록할 수 있습니다.");
		requireOpen(lot.getWorkOrderId());
		BigDecimal qty = nullToZero(lot.getWeighQty());
		applyDelta(lot.getWarehouseId(), lot.getItemId(), lot.getLotNo(), qty.negate(), null);
		lot.setInputQty(qty);
		lot.setStatus(ProdInputLot.INPUT);
		return ProdInputLotResponse.from(lot);
	}

	@Transactional
	public ProdInputLotResponse combine(ProductionSheetRequest request) {
		ProdInputLot lot = requireInput(request.getInputLotSeq(), ProdInputLot.INPUT, "투입 LOT가 등록된 자재만 조합 결과를 저장할 수 있습니다.");
		requireOpen(lot.getWorkOrderId());
		if (!StringUtils.hasText(request.getCombineResult())) {
			throw new IllegalArgumentException("조합 결과를 입력하세요.");
		}
		lot.setCombineResult(request.getCombineResult().trim());
		lot.setStatus(ProdInputLot.COMBINED);
		return ProdInputLotResponse.from(lot);
	}

	@Transactional
	public ProdInputLotResponse changeLot(ProductionSheetRequest request) {
		ProdInputLot lot = requireInputLot(request.getInputLotSeq());
		requireOpen(lot.getWorkOrderId());
		if (!ProdInputLot.INPUT.equals(lot.getStatus()) && !ProdInputLot.COMBINED.equals(lot.getStatus())) {
			throw new IllegalStateException("투입 LOT가 등록된 뒤에만 LOT를 변경할 수 있습니다.");
		}
		StockResponse next = requireManufacturingStock(request.getStockSeq());
		if (!lot.getItemId().equals(next.getItemId())) {
			throw new IllegalArgumentException("같은 품목의 LOT만 변경할 수 있습니다.");
		}
		if (next.getLotNo().equals(lot.getLotNo()) && next.getWarehouseId().equals(lot.getWarehouseId())) {
			throw new IllegalArgumentException("현재 투입 LOT와 같습니다.");
		}
		BigDecimal qty = nullToZero(lot.getInputQty());
		if (qty.compareTo(nullToZero(next.getQty())) > 0) {
			throw new IllegalArgumentException("변경 LOT의 재고가 부족합니다.");
		}
		applyDelta(lot.getWarehouseId(), lot.getItemId(), lot.getLotNo(), qty, stockTemplate(lot));
		applyDelta(next.getWarehouseId(), next.getItemId(), next.getLotNo(), qty.negate(), null);
		lot.setStockSeq(next.getStockSeq());
		lot.setWarehouseId(next.getWarehouseId());
		lot.setWarehouseName(next.getWarehouseName());
		lot.setLotNo(next.getLotNo());
		return ProdInputLotResponse.from(lot);
	}

	@Transactional
	public void updateInputComment(ProductionSheetRequest request) {
		WorkOrder workOrder = requireOpen(request.getWorkOrderId());
		workOrder.setInputComment(trimToNull(request.getComment()));
	}

	@Transactional
	public void updateWorkComment(ProductionSheetRequest request) {
		WorkOrder workOrder = requireOpen(request.getWorkOrderId());
		workOrder.setWorkComment(trimToNull(request.getComment()));
	}

	public ProdResultSuggestResponse suggestResult(String workOrderId) {
		return prodResultService.suggestResult(workOrderId);
	}

	@Transactional
	public void saveWeighingResult(ProductionSheetRequest request) {
		ProdResultCreateRequest create = toResultRequest(request, "WEIGH");
		prodResultService.createGoodResult(create);
	}

	@Transactional
	public void saveResult(ProductionSheetRequest request) {
		String resultType = "PACK".equalsIgnoreCase(request.getResultType()) ? "PACK" : "RESULT";
		prodResultService.createGoodResult(toResultRequest(request, resultType));
	}

	@Transactional
	public void updateResult(ProductionSheetRequest request) {
		if (request.getProdResultSeq() == null) {
			throw new IllegalArgumentException("수정할 실적을 선택하세요.");
		}
		ProdResultUpdateRequest update = new ProdResultUpdateRequest();
		update.setLotId(request.getLotId());
		update.setProdQty(request.getProdQty());
		prodResultService.updateGoodResult(request.getProdResultSeq(), update);
	}

	@Transactional
	public void finish(ProductionSheetRequest request) {
		WorkOrder workOrder = requireWorkOrder(request.getWorkOrderId());
		prodResultService.completeGoodResults(workOrder.getWorkOrderId());
	}

	@Transactional
	public void saveFinalLot(ProductionSheetRequest request) {
		WorkOrder workOrder = requireWorkOrder(request.getWorkOrderId());
		if (!StringUtils.hasText(request.getFinalLotNo())) {
			throw new IllegalArgumentException("최종 LOT를 입력하세요.");
		}
		workOrder.setFinalLotNo(request.getFinalLotNo().trim());
	}

	private ProdResultCreateRequest toResultRequest(ProductionSheetRequest request, String resultType) {
		BigDecimal qty = requirePositive(request.getProdQty(), "실적 수량은 0보다 커야 합니다.");
		ProdResultCreateRequest create = new ProdResultCreateRequest();
		create.setWorkOrderId(request.getWorkOrderId());
		create.setLotId(StringUtils.hasText(request.getLotId()) ? request.getLotId().trim() : null);
		create.setProdQty(qty);
		create.setResultType(resultType);
		create.setProductionStartTime(request.getProductionStartTime());
		create.setProductionEndTime(request.getProductionEndTime());
		return create;
	}

	private WorkOrder requireOpen(String workOrderId) {
		WorkOrder workOrder = requireWorkOrder(workOrderId);
		closeValidator.checkWorkOrder(workOrder);
		return workOrder;
	}

	private WorkOrder requireWorkOrder(String workOrderId) {
		if (!StringUtils.hasText(workOrderId)) {
			throw new IllegalArgumentException("작업지시를 선택하세요.");
		}
		return workOrderRepository.findById(workOrderId.trim())
				.orElseThrow(() -> new IllegalArgumentException("작업지시를 찾을 수 없습니다."));
	}

	private ProdInputLot requireInput(Long inputLotSeq, String status, String message) {
		ProdInputLot lot = requireInputLot(inputLotSeq);
		if (!status.equals(lot.getStatus())) {
			throw new IllegalStateException(message);
		}
		return lot;
	}

	private ProdInputLot requireInputLot(Long inputLotSeq) {
		if (inputLotSeq == null) {
			throw new IllegalArgumentException("투입 자재를 선택하세요.");
		}
		return prodInputLotRepository.findById(inputLotSeq)
				.orElseThrow(() -> new IllegalArgumentException("투입 자재를 찾을 수 없습니다."));
	}

	private StockResponse requireManufacturingStock(Long stockSeq) {
		if (stockSeq == null) {
			throw new IllegalArgumentException("재고를 선택하세요.");
		}
		for (StockResponse stock : findInputStocks()) {
			if (stock.getStockSeq().equals(stockSeq)) {
				return stock;
			}
		}
		throw new IllegalArgumentException("제조 창고의 입고 LOT 재고만 투입할 수 있습니다.");
	}

	private void applyDelta(String warehouseId, String itemId, String lotNo, BigDecimal delta, Stock template) {
		Stock stock = stockRepository.findFirstByWarehouseIdAndItemIdAndLotNo(warehouseId, itemId, lotNo).orElse(null);
		if (stock == null) {
			if (delta.signum() < 0 || template == null) {
				throw new IllegalStateException("재고가 부족합니다.");
			}
			stockRepository.save(Stock.builder()
					.warehouseId(template.getWarehouseId())
					.warehouseName(template.getWarehouseName())
					.itemId(template.getItemId())
					.itemName(template.getItemName())
					.lotNo(template.getLotNo())
					.unit(template.getUnit())
					.qty(delta)
					.build());
			return;
		}
		BigDecimal next = nullToZero(stock.getQty()).add(delta);
		if (next.signum() < 0) {
			throw new IllegalStateException("재고가 부족합니다.");
		}
		stock.setQty(next);
	}

	private Stock stockTemplate(ProdInputLot lot) {
		return Stock.builder()
				.warehouseId(lot.getWarehouseId())
				.warehouseName(lot.getWarehouseName())
				.itemId(lot.getItemId())
				.itemName(lot.getItemName())
				.lotNo(lot.getLotNo())
				.unit(lot.getUnit())
				.build();
	}

	private BigDecimal requirePositive(BigDecimal qty, String message) {
		if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException(message);
		}
		return qty;
	}

	private BigDecimal nullToZero(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
