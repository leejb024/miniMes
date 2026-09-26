package minimes.stock.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.master.domain.Warehouse;
import minimes.master.repository.WarehouseRepository;
import minimes.stock.domain.MaterialMove;
import minimes.stock.domain.MaterialOrder;
import minimes.stock.domain.Stock;
import minimes.stock.dto.MaterialMoveResponse;
import minimes.stock.dto.MaterialOrderActionRequest;
import minimes.stock.dto.MaterialOrderCreateRequest;
import minimes.stock.dto.MaterialOrderResponse;
import minimes.stock.dto.StockResponse;
import minimes.stock.repository.MaterialMoveRepository;
import minimes.stock.repository.MaterialOrderRepository;
import minimes.stock.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaterialFlowService {

	private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final MaterialOrderRepository materialOrderRepository;
	private final MaterialMoveRepository materialMoveRepository;
	private final StockRepository stockRepository;
	private final WarehouseRepository warehouseRepository;
	private final StockService stockService;
	private final StockCloseService stockCloseService;

	@Transactional
	public void trimHandoverToPick() {
		materialMoveRepository.deleteByMoveTypeIn(List.of("MOVE_INFO", "IN_MOVE_INFO"));
		for (MaterialOrder order : materialOrderRepository.findAll()) {
			if (MaterialOrder.MOVE_INFO.equals(order.getStatus()) || MaterialOrder.IN_MOVE.equals(order.getStatus())) {
				order.setStatus(MaterialOrder.MOVING);
			}
		}
	}

	@Transactional(readOnly = true)
	public List<MaterialOrderResponse> findAll() {
		List<MaterialOrderResponse> responses = new ArrayList<>();
		for (MaterialOrder order : materialOrderRepository.findAllByOrderByOrderSeqDesc()) {
			responses.add(toResponse(order));
		}
		return responses;
	}

	@Transactional
	public MaterialOrderResponse createOrder(MaterialOrderCreateRequest request, String creId) {
		assertPeriodOpen();
		BigDecimal qty = requirePositive(request.getQty(), "요청 수량은 0보다 커야 합니다.");
		StockResponse stock = findInboundStock(request.getStockSeq());
		if (qty.compareTo(nullToZero(stock.getQty())) > 0) {
			throw new IllegalArgumentException("요청 수량이 재고 수량을 초과합니다.");
		}
		Warehouse toWarehouse = requireWarehouse(request.getToWarehouseId());
		if (toWarehouse.getWarehouseId().equals(stock.getWarehouseId())) {
			throw new IllegalArgumentException("출고 창고와 입고 창고는 달라야 합니다.");
		}

		MaterialOrder order = materialOrderRepository.save(MaterialOrder.builder()
				.orderNo(nextOrderNo())
				.status(MaterialOrder.REQUEST)
				.stockSeq(stock.getStockSeq())
				.itemId(stock.getItemId())
				.itemName(stock.getItemName())
				.lotNo(stock.getLotNo())
				.unit(stock.getUnit())
				.qty(qty)
				.fromWarehouseId(stock.getWarehouseId())
				.fromWarehouseName(stock.getWarehouseName())
				.toWarehouseId(toWarehouse.getWarehouseId())
				.toWarehouseName(toWarehouse.getWarehouseName())
				.remark(trimToNull(request.getRemark()))
				.creId(actor(creId))
				.build());
		log(order, "MATERIAL_ORDER", qty, request.getRemark(), creId);
		return toResponse(order);
	}

	@Transactional
	public MaterialOrderResponse moveRequest(MaterialOrderActionRequest request, String creId) {
		MaterialOrder order = requireStatus(request.getOrderNo(), MaterialOrder.REQUEST, "불출 요청 상태에서만 이동 요청할 수 있습니다.");
		order.setStatus(MaterialOrder.MOVE_REQUEST);
		log(order, "MOVE_REQUEST", order.getQty(), "자재팀 창고에서 제조팀 창고로 이동 요청", creId);
		return toResponse(order);
	}

	@Transactional
	public MaterialOrderResponse pickOrder(MaterialOrderActionRequest request, String creId) {
		MaterialOrder order = requireStatus(request.getOrderNo(), MaterialOrder.MOVE_REQUEST, "이동 요청 상태에서만 피킹 오더를 등록할 수 있습니다.");
		if (!"Y".equalsIgnoreCase(trimToNull(request.getApprovedYn())) || !StringUtils.hasText(request.getPickNo())) {
			throw new IllegalArgumentException("승인된 피킹 오더가 있어야 이동중으로 전환됩니다.");
		}
		order.setApprovedYn("Y");
		order.setPickNo(request.getPickNo().trim());
		order.setStatus(MaterialOrder.MOVING);
		log(order, "PICK_ORDER", order.getQty(), order.getPickNo(), creId);
		return toResponse(order);
	}

	@Transactional
	public MaterialOrderResponse saveMoveInfo(MaterialOrderActionRequest request, String creId) {
		MaterialOrder order = requireStatus(request.getOrderNo(), MaterialOrder.MOVING, "이동중 상태에서만 이동 정보를 저장할 수 있습니다.");
		BigDecimal qty = request.getQty() == null ? order.getQty() : requirePositive(request.getQty(), "이동 수량은 0보다 커야 합니다.");
		if (qty.compareTo(nullToZero(order.getQty())) > 0) {
			throw new IllegalArgumentException("이동 수량이 요청 수량을 초과합니다.");
		}
		order.setQty(qty);
		order.setStatus(MaterialOrder.MOVE_INFO);
		log(order, "MOVE_INFO", qty, request.getRemark(), creId);
		return toResponse(order);
	}

	@Transactional
	public MaterialOrderResponse saveInMoveInfo(MaterialOrderActionRequest request, String creId) {
		MaterialOrder order = requireStatus(request.getOrderNo(), MaterialOrder.MOVE_INFO, "이동 정보가 저장된 요청만 입고 이동 정보를 저장할 수 있습니다.");
		order.setStatus(MaterialOrder.IN_MOVE);
		if (StringUtils.hasText(request.getRemark())) {
			order.setRemark(request.getRemark().trim());
		}
		log(order, "IN_MOVE_INFO", order.getQty(), request.getRemark(), creId);
		return toResponse(order);
	}

	@Transactional
	public MaterialOrderResponse confirmReceive(MaterialOrderActionRequest request, String creId) {
		assertPeriodOpen();
		MaterialOrder order = requireOrder(request.getOrderNo());
		if (MaterialOrder.RECEIVED.equals(order.getStatus()) || MaterialOrder.MOVED.equals(order.getStatus())) {
			throw new IllegalStateException("이미 인수확정된 요청입니다.");
		}
		if (!MaterialOrder.MOVING.equals(order.getStatus())
				&& !MaterialOrder.MOVE_INFO.equals(order.getStatus())
				&& !MaterialOrder.IN_MOVE.equals(order.getStatus())
				&& !MaterialOrder.DISPATCHED.equals(order.getStatus())) {
			throw new IllegalStateException("피킹이 완료된 요청만 인수확정할 수 있습니다.");
		}
		applyDelta(order.getFromWarehouseId(), order.getItemId(), order.getLotNo(), order.getQty().negate(), null);
		applyDelta(order.getToWarehouseId(), order.getItemId(), order.getLotNo(), order.getQty(), targetFromOrder(order));
		order.setStatus(MaterialOrder.RECEIVED);
		log(order, "RECEIVE", order.getQty(), order.getToWarehouseName(), creId);
		return toResponse(order);
	}

	@Transactional
	public MaterialOrderResponse dispatch(MaterialOrderActionRequest request, String creId) {
		MaterialOrder order = requireOrder(request.getOrderNo());
		if (!MaterialOrder.MOVING.equals(order.getStatus())
				&& !MaterialOrder.MOVE_INFO.equals(order.getStatus())
				&& !MaterialOrder.IN_MOVE.equals(order.getStatus())) {
			throw new IllegalStateException("피킹이 완료된 요청만 불출할 수 있습니다.");
		}
		order.setStatus(MaterialOrder.DISPATCHED);
		log(order, "DISPATCH", order.getQty(), null, creId);
		return toResponse(order);
	}

	@Transactional
	public MaterialOrderResponse move(MaterialOrderActionRequest request, String creId) {
		assertPeriodOpen();
		MaterialOrder order = requireStatus(request.getOrderNo(), MaterialOrder.DISPATCHED, "불출된 요청만 이동할 수 있습니다.");
		applyDelta(order.getFromWarehouseId(), order.getItemId(), order.getLotNo(), order.getQty().negate(), null);
		applyDelta(order.getToWarehouseId(), order.getItemId(), order.getLotNo(), order.getQty(), targetFromOrder(order));
		order.setStatus(MaterialOrder.MOVED);
		order.setInputLotNo(order.getLotNo());
		log(order, "MATERIAL_MOVE", order.getQty(), null, creId);
		return toResponse(order);
	}

	@Transactional
	public MaterialOrderResponse weigh(MaterialOrderActionRequest request, String creId) {
		MaterialOrder order = requireStatus(request.getOrderNo(), MaterialOrder.MOVED, "이동이 완료된 요청만 계량할 수 있습니다.");
		BigDecimal weighQty = requirePositive(request.getWeighQty(), "계량 수량은 0보다 커야 합니다.");
		if (weighQty.compareTo(nullToZero(order.getQty())) > 0) {
			throw new IllegalArgumentException("계량 수량이 이동 수량을 초과합니다.");
		}
		order.setWeighQty(weighQty);
		order.setStatus(MaterialOrder.WEIGHED);
		log(order, "WEIGHING", weighQty, null, creId);
		return toResponse(order);
	}

	@Transactional
	public MaterialOrderResponse registerInputLot(MaterialOrderActionRequest request, String creId) {
		MaterialOrder order = requireStatus(request.getOrderNo(), MaterialOrder.WEIGHED, "계량된 요청만 투입 LOT를 등록할 수 있습니다.");
		String lotNo = StringUtils.hasText(request.getLotNo()) ? request.getLotNo().trim() : order.getLotNo();
		if (!lotNo.equals(order.getLotNo())) {
			throw new IllegalArgumentException("이동한 LOT만 투입할 수 있습니다. 다른 LOT는 LOT 변경에서 처리하세요.");
		}
		order.setInputLotNo(lotNo);
		order.setStatus(MaterialOrder.INPUT);
		log(order, "INPUT_LOT", order.getWeighQty(), lotNo, creId);
		return toResponse(order);
	}

	@Transactional
	public MaterialOrderResponse saveCombineResult(MaterialOrderActionRequest request, String creId) {
		MaterialOrder order = requireStatus(request.getOrderNo(), MaterialOrder.INPUT, "투입 LOT가 등록된 요청만 조합 결과를 저장할 수 있습니다.");
		if (!StringUtils.hasText(request.getCombineResult())) {
			throw new IllegalArgumentException("조합 결과를 입력하세요.");
		}
		order.setCombineResult(request.getCombineResult().trim());
		order.setStatus(MaterialOrder.COMBINED);
		log(order, "COMBINE_RESULT", order.getWeighQty(), order.getCombineResult(), creId);
		return toResponse(order);
	}

	@Transactional
	public MaterialOrderResponse changeInputLot(MaterialOrderActionRequest request, String creId) {
		assertPeriodOpen();
		MaterialOrder order = requireOrder(request.getOrderNo());
		if (!MaterialOrder.INPUT.equals(order.getStatus())
				&& !MaterialOrder.COMBINED.equals(order.getStatus())
				&& !MaterialOrder.LOT_CHANGED.equals(order.getStatus())) {
			throw new IllegalStateException("투입 LOT가 등록된 뒤에만 LOT를 변경할 수 있습니다.");
		}
		if (!StringUtils.hasText(request.getLotNo())) {
			throw new IllegalArgumentException("변경할 LOT를 입력하세요.");
		}
		String nextLot = request.getLotNo().trim();
		String currentLot = StringUtils.hasText(order.getInputLotNo()) ? order.getInputLotNo() : order.getLotNo();
		if (nextLot.equals(currentLot)) {
			throw new IllegalArgumentException("현재 투입 LOT와 같습니다.");
		}
		StockResponse nextStock = findInboundStock(order.getItemId(), nextLot);
		if (nextStock.getWarehouseId().equals(order.getToWarehouseId())) {
			throw new IllegalArgumentException("제조팀 창고에 이미 있는 LOT는 변경 대상으로 쓸 수 없습니다.");
		}
		BigDecimal qty = nullToZero(order.getQty());
		if (qty.compareTo(nullToZero(nextStock.getQty())) > 0) {
			throw new IllegalArgumentException("변경 LOT의 재고가 부족합니다.");
		}
		applyDelta(order.getToWarehouseId(), order.getItemId(), currentLot, qty.negate(), null);
		applyDelta(order.getFromWarehouseId(), order.getItemId(), currentLot, qty, sourceTemplate(order, currentLot));
		applyDelta(nextStock.getWarehouseId(), order.getItemId(), nextLot, qty.negate(), null);
		applyDelta(order.getToWarehouseId(), order.getItemId(), nextLot, qty, targetTemplate(order, nextLot, nextStock));
		order.setInputLotNo(nextLot);
		order.setFromWarehouseId(nextStock.getWarehouseId());
		order.setFromWarehouseName(nextStock.getWarehouseName());
		order.setStatus(MaterialOrder.LOT_CHANGED);
		log(order, "LOT_CHANGE", qty, nextLot, creId);
		return toResponse(order);
	}

	private MaterialOrderResponse toResponse(MaterialOrder order) {
		List<MaterialMoveResponse> moves = new ArrayList<>();
		for (MaterialMove move : materialMoveRepository.findByOrderNoOrderByMoveSeqAsc(order.getOrderNo())) {
			moves.add(MaterialMoveResponse.from(move));
		}
		return MaterialOrderResponse.from(order, moves);
	}

	private void log(MaterialOrder order, String type, BigDecimal qty, String remark, String creId) {
		materialMoveRepository.save(MaterialMove.builder()
				.orderNo(order.getOrderNo())
				.moveType(type)
				.qty(qty)
				.remark(trimToNull(remark))
				.creId(actor(creId))
				.build());
	}

	private MaterialOrder requireStatus(String orderNo, String status, String message) {
		MaterialOrder order = requireOrder(orderNo);
		if (!status.equals(order.getStatus())) {
			throw new IllegalStateException(message);
		}
		return order;
	}

	private MaterialOrder requireOrder(String orderNo) {
		if (!StringUtils.hasText(orderNo)) {
			throw new IllegalArgumentException("불출 요청을 선택하세요.");
		}
		return materialOrderRepository.findByOrderNo(orderNo.trim())
				.orElseThrow(() -> new IllegalArgumentException("불출 요청을 찾을 수 없습니다."));
	}

	private StockResponse findInboundStock(Long stockSeq) {
		for (StockResponse stock : stockService.findAll()) {
			if (stock.getStockSeq().equals(stockSeq)) {
				return stock;
			}
		}
		throw new IllegalArgumentException("입고 LOT 재고만 불출 요청할 수 있습니다.");
	}

	private StockResponse findInboundStock(String itemId, String lotNo) {
		for (StockResponse stock : stockService.findAll()) {
			if (itemId.equals(stock.getItemId()) && lotNo.equals(stock.getLotNo())) {
				return stock;
			}
		}
		throw new IllegalArgumentException("입고 LOT로 등록된 재고만 변경할 수 있습니다.");
	}

	private void applyDelta(String warehouseId, String itemId, String lotNo, BigDecimal delta, Stock template) {
		Stock stock = stockRepository.findFirstByWarehouseIdAndItemIdAndLotNo(warehouseId, itemId, lotNo).orElse(null);
		if (stock == null) {
			if (delta.signum() < 0) {
				throw new IllegalStateException("재고가 부족합니다.");
			}
			if (template == null) {
				throw new IllegalStateException("이동할 재고 정보가 없습니다.");
			}
			stockRepository.save(Stock.builder()
					.warehouseId(warehouseId)
					.warehouseName(template.getWarehouseName())
					.itemId(template.getItemId())
					.itemName(template.getItemName())
					.lotNo(lotNo)
					.unit(template.getUnit())
					.qty(delta)
					.locationName(template.getLocationName())
					.build());
			return;
		}
		BigDecimal next = nullToZero(stock.getQty()).add(delta);
		if (next.signum() < 0) {
			throw new IllegalStateException("재고가 부족합니다.");
		}
		stock.setQty(next);
	}

	private Stock targetFromOrder(MaterialOrder order) {
		return Stock.builder()
				.warehouseId(order.getToWarehouseId())
				.warehouseName(order.getToWarehouseName())
				.itemId(order.getItemId())
				.itemName(order.getItemName())
				.lotNo(order.getLotNo())
				.unit(order.getUnit())
				.build();
	}

	private Stock sourceTemplate(MaterialOrder order, String lotNo) {
		return Stock.builder()
				.warehouseId(order.getFromWarehouseId())
				.warehouseName(order.getFromWarehouseName())
				.itemId(order.getItemId())
				.itemName(order.getItemName())
				.lotNo(lotNo)
				.unit(order.getUnit())
				.build();
	}

	private Stock targetTemplate(MaterialOrder order, String lotNo, StockResponse source) {
		return Stock.builder()
				.warehouseId(order.getToWarehouseId())
				.warehouseName(order.getToWarehouseName())
				.itemId(order.getItemId())
				.itemName(order.getItemName())
				.lotNo(lotNo)
				.unit(source.getUnit() == null ? order.getUnit() : source.getUnit())
				.locationName(source.getLocationName())
				.build();
	}

	private Warehouse requireWarehouse(String warehouseId) {
		Warehouse warehouse = warehouseRepository.findById(warehouseId.trim())
				.orElseThrow(() -> new IllegalArgumentException("창고를 찾을 수 없습니다."));
		if (warehouse.getUseYn() != null && !"Y".equalsIgnoreCase(warehouse.getUseYn())) {
			throw new IllegalArgumentException("사용할 수 없는 창고입니다.");
		}
		return warehouse;
	}

	private void assertPeriodOpen() {
		stockCloseService.assertDateOpen(LocalDate.now(), "마감된 기간에는 자재를 이동할 수 없습니다.");
	}

	private String nextOrderNo() {
		String prefix = "MO" + LocalDate.now().format(ORDER_DATE);
		MaterialOrder last = materialOrderRepository.findTopByOrderNoStartingWithOrderByOrderNoDesc(prefix).orElse(null);
		int next = 1;
		if (last != null && last.getOrderNo().length() > prefix.length()) {
			next = Integer.parseInt(last.getOrderNo().substring(prefix.length())) + 1;
		}
		return prefix + String.format("%04d", next);
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

	private String actor(String creId) {
		return StringUtils.hasText(creId) ? creId : "admin";
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
