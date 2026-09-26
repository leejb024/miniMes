package minimes.purchase.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.master.domain.Item;
import minimes.master.domain.Warehouse;
import minimes.master.repository.ItemRepository;
import minimes.master.repository.WarehouseRepository;
import minimes.purchase.domain.PurchaseLot;
import minimes.purchase.domain.PurchaseOrder;
import minimes.purchase.domain.PurchaseOrderItem;
import minimes.purchase.domain.PurchaseScan;
import minimes.purchase.dto.PurchaseLotRequest;
import minimes.purchase.dto.PurchaseLotResponse;
import minimes.purchase.dto.PurchaseQtyRequest;
import minimes.purchase.dto.PurchaseReturnRequest;
import minimes.purchase.dto.PurchaseScanRequest;
import minimes.purchase.dto.PurchaseScanResponse;
import minimes.purchase.repository.PurchaseLotRepository;
import minimes.purchase.repository.PurchaseOrderItemRepository;
import minimes.purchase.repository.PurchaseOrderRepository;
import minimes.purchase.repository.PurchaseScanRepository;
import minimes.stock.domain.Stock;
import minimes.stock.repository.StockRepository;
import minimes.stock.service.StockCloseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseReceiveService {

	private static final DateTimeFormatter LOT_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final PurchaseScanRepository purchaseScanRepository;
	private final PurchaseLotRepository purchaseLotRepository;
	private final PurchaseOrderRepository purchaseOrderRepository;
	private final PurchaseOrderItemRepository purchaseOrderItemRepository;
	private final WarehouseRepository warehouseRepository;
	private final ItemRepository itemRepository;
	private final StockRepository stockRepository;
	private final StockCloseService stockCloseService;

	@Transactional(readOnly = true)
	public List<PurchaseLotResponse> findLots(String lotType, String poNo, String sourceLotNo) {
		String type = normalizeType(lotType);
		List<PurchaseLot> lots;
		if (PurchaseLot.TYPE_RETURN.equals(type) && StringUtils.hasText(sourceLotNo)) {
			lots = purchaseLotRepository.findByLotTypeAndSourceLotNoOrderByLotSeqDesc(type, sourceLotNo.trim());
		} else if (PurchaseLot.TYPE_INBOUND.equals(type) && StringUtils.hasText(poNo)) {
			lots = purchaseLotRepository.findByLotTypeAndPoNoOrderByLotSeqDesc(type, poNo.trim());
		} else {
			lots = purchaseLotRepository.findByLotTypeOrderByLotSeqDesc(type);
		}

		List<PurchaseLotResponse> responses = new ArrayList<>();
		for (PurchaseLot lot : lots) {
			responses.add(toResponse(lot));
		}
		return responses;
	}

	@Transactional
	public PurchaseScanResponse scanInfo(PurchaseScanRequest request) {
		String barcode = request.getBarcode().trim();
		PurchaseOrderItem item = resolveScanItem(barcode, request.getPoNo());
		Item master = requirePurchaseItem(item.getItemId());

		PurchaseScan scan = purchaseScanRepository.save(PurchaseScan.builder()
				.barcode(barcode)
				.poNo(item.getPoNo())
				.poItemSeq(item.getPoItemSeq())
				.itemId(master.getItemId())
				.itemName(master.getItemName())
				.unit(item.getUnit())
				.usedYn("N")
				.build());
		return PurchaseScanResponse.from(scan);
	}

	@Transactional
	public PurchaseLotResponse createLot(PurchaseLotRequest request) {
		PurchaseScan scan = purchaseScanRepository.findById(request.getScanSeq())
				.orElseThrow(() -> new IllegalArgumentException("스캔 정보를 찾을 수 없습니다."));
		if ("Y".equalsIgnoreCase(scan.getUsedYn())) {
			throw new IllegalStateException("이미 LOT가 생성된 스캔입니다.");
		}

		BigDecimal qty = requirePositive(request.getQty(), "입고 수량은 0보다 커야 합니다.");
		PurchaseOrderItem orderItem = purchaseOrderItemRepository.findById(scan.getPoItemSeq())
				.orElseThrow(() -> new IllegalArgumentException("발주 자재를 찾을 수 없습니다."));
		assertDueDateReached(orderItem);
		stockCloseService.assertDateOpen(LocalDate.now(), "마감된 기간에는 입고할 수 없습니다.");
		assertWithinOrderQty(orderItem, qty, null);

		Warehouse warehouse = resolveWarehouse(request.getWarehouseId());
		String lotNo = StringUtils.hasText(request.getLotNo())
				? request.getLotNo().trim()
				: nextLotNo("L");
		if (purchaseLotRepository.existsByLotNo(lotNo)) {
			throw new IllegalArgumentException("이미 존재하는 LOT 번호입니다.");
		}

		PurchaseLot lot = purchaseLotRepository.save(PurchaseLot.builder()
				.lotNo(lotNo)
				.lotType(PurchaseLot.TYPE_INBOUND)
				.poNo(scan.getPoNo())
				.poItemSeq(scan.getPoItemSeq())
				.itemId(scan.getItemId())
				.itemName(scan.getItemName())
				.unit(scan.getUnit())
				.qty(qty)
				.warehouseId(warehouse.getWarehouseId())
				.warehouseName(warehouse.getWarehouseName())
				.barcode(scan.getBarcode())
				.build());
		scan.setUsedYn("Y");
		increaseStock(lot, qty);
		return toResponse(lot);
	}

	@Transactional
	public PurchaseLotResponse updateQty(PurchaseQtyRequest request) {
		PurchaseLot lot = requireInbound(request.getLotSeq());
		assertInboundOpen(lot);
		BigDecimal qty = requirePositive(request.getQty(), "입고 수량은 0보다 커야 합니다.");
		BigDecimal returned = returnedQty(lot.getLotNo());
		if (qty.compareTo(returned) < 0) {
			throw new IllegalStateException("반품된 수량보다 작게 수정할 수 없습니다.");
		}

		PurchaseOrderItem orderItem = purchaseOrderItemRepository.findById(lot.getPoItemSeq())
				.orElseThrow(() -> new IllegalArgumentException("발주 자재를 찾을 수 없습니다."));
		assertWithinOrderQty(orderItem, qty, lot.getLotSeq());

		BigDecimal delta = qty.subtract(nullToZero(lot.getQty()));
		lot.setQty(qty);
		applyStockDelta(lot, delta);
		return toResponse(lot);
	}

	@Transactional
	public void deleteQty(Long lotSeq) {
		PurchaseLot lot = requireInbound(lotSeq);
		assertInboundOpen(lot);
		if (returnedQty(lot.getLotNo()).compareTo(BigDecimal.ZERO) > 0) {
			throw new IllegalStateException("반품이 있는 입고는 삭제할 수 없습니다.");
		}
		removeStock(lot);
		purchaseLotRepository.delete(lot);
	}

	@Transactional
	public PurchaseLotResponse returnLot(PurchaseReturnRequest request) {
		PurchaseLot source = requireInbound(request.getLotSeq());
		assertInboundOpen(source);
		BigDecimal qty = requirePositive(request.getQty(), "반품 수량은 0보다 커야 합니다.");
		BigDecimal remain = nullToZero(source.getQty()).subtract(returnedQty(source.getLotNo()));
		if (qty.compareTo(remain) > 0) {
			throw new IllegalArgumentException("반품 수량이 입고 잔량을 초과합니다.");
		}

		String lotNo = nextLotNo("R");
		PurchaseLot returned = purchaseLotRepository.save(PurchaseLot.builder()
				.lotNo(lotNo)
				.lotType(PurchaseLot.TYPE_RETURN)
				.poNo(source.getPoNo())
				.poItemSeq(source.getPoItemSeq())
				.itemId(source.getItemId())
				.itemName(source.getItemName())
				.unit(source.getUnit())
				.qty(qty)
				.warehouseId(source.getWarehouseId())
				.warehouseName(source.getWarehouseName())
				.barcode(source.getBarcode())
				.sourceLotNo(source.getLotNo())
				.remark(trimToNull(request.getRemark()))
				.build());
		applyStockDelta(source, qty.negate());
		return toResponse(returned);
	}

	@Transactional
	public void deleteReturnQty(Long lotSeq) {
		PurchaseLot returned = purchaseLotRepository.findById(lotSeq)
				.orElseThrow(() -> new IllegalArgumentException("반품 LOT를 찾을 수 없습니다."));
		if (!PurchaseLot.TYPE_RETURN.equals(returned.getLotType())) {
			throw new IllegalArgumentException("반품 LOT가 아닙니다.");
		}
		PurchaseLot source = purchaseLotRepository.findByLotNo(returned.getSourceLotNo())
				.orElseThrow(() -> new IllegalArgumentException("원 입고 LOT를 찾을 수 없습니다."));
		assertInboundOpen(returned);
		assertInboundOpen(source);
		applyStockDelta(source, nullToZero(returned.getQty()));
		purchaseLotRepository.delete(returned);
	}

	private PurchaseLotResponse toResponse(PurchaseLot lot) {
		BigDecimal returned = PurchaseLot.TYPE_INBOUND.equals(lot.getLotType())
				? returnedQty(lot.getLotNo())
				: BigDecimal.ZERO;
		return PurchaseLotResponse.from(lot, returned);
	}

	private BigDecimal returnedQty(String sourceLotNo) {
		BigDecimal sum = BigDecimal.ZERO;
		for (PurchaseLot returned : purchaseLotRepository.findByLotTypeAndSourceLotNoOrderByLotSeqDesc(
				PurchaseLot.TYPE_RETURN, sourceLotNo)) {
			sum = sum.add(nullToZero(returned.getQty()));
		}
		return sum;
	}

	private void assertInboundOpen(PurchaseLot lot) {
		LocalDate date = lot.getCreDt() == null ? LocalDate.now() : lot.getCreDt().toLocalDate();
		stockCloseService.assertDateOpen(date, StockCloseService.CLOSED_INBOUND_MESSAGE);
	}

	private void assertDueDateReached(PurchaseOrderItem orderItem) {
		LocalDate dueDate = orderItem.getDueDate();
		if (dueDate == null) {
			PurchaseOrder order = purchaseOrderRepository.findById(orderItem.getPoNo()).orElse(null);
			dueDate = order == null ? null : order.getDueDate();
		}
		if (dueDate == null || dueDate.isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("납품예정일이 오늘 이전인 발주만 입고할 수 있습니다.");
		}
	}

	private void assertWithinOrderQty(PurchaseOrderItem orderItem, BigDecimal nextQty, Long excludeLotSeq) {
		BigDecimal received = BigDecimal.ZERO;
		for (PurchaseLot inbound : purchaseLotRepository.findByLotTypeAndPoItemSeq(
				PurchaseLot.TYPE_INBOUND, orderItem.getPoItemSeq())) {
			if (excludeLotSeq != null && excludeLotSeq.equals(inbound.getLotSeq())) {
				continue;
			}
			received = received.add(nullToZero(inbound.getQty()));
		}
		BigDecimal orderQty = nullToZero(orderItem.getQty());
		if (received.add(nextQty).compareTo(orderQty) > 0) {
			throw new IllegalArgumentException("입고 수량이 발주 수량을 초과합니다.");
		}
	}

	private PurchaseOrderItem findOrderItem(String poNo, String itemId) {
		if (StringUtils.hasText(poNo)) {
			if (!purchaseOrderRepository.existsById(poNo)) {
				throw new IllegalArgumentException("발주를 찾을 수 없습니다.");
			}
			for (PurchaseOrderItem item : purchaseOrderItemRepository.findByPoNoOrderByPoItemSeqAsc(poNo)) {
				if (itemId.equals(item.getItemId())) {
					return item;
				}
			}
			throw new IllegalArgumentException("선택한 발주에 없는 자재입니다.");
		}

		List<PurchaseOrderItem> matches = new ArrayList<>();
		for (PurchaseOrderItem item : purchaseOrderItemRepository.findAll()) {
			if (itemId.equals(item.getItemId())) {
				matches.add(item);
			}
		}
		if (matches.isEmpty()) {
			throw new IllegalArgumentException("발주 자재와 일치하는 바코드가 아닙니다.");
		}
		if (matches.size() > 1) {
			throw new IllegalArgumentException("같은 자재의 발주가 여러 건입니다. 발주를 선택하세요.");
		}
		return matches.get(0);
	}

	private PurchaseOrderItem resolveScanItem(String barcode, String requestedPoNo) {
		String selectedPoNo = trimToNull(requestedPoNo);
		int separator = indexOfSeparator(barcode);
		if (separator > 0 && separator < barcode.length() - 1) {
			return findOrderItem(barcode.substring(0, separator).trim(), barcode.substring(separator + 1).trim());
		}
		if (purchaseOrderRepository.existsById(barcode)) {
			if (selectedPoNo != null && !selectedPoNo.equals(barcode)) {
				throw new IllegalArgumentException("선택한 발주와 바코드의 발주번호가 다릅니다.");
			}
			return onlyOrderItem(barcode);
		}
		return findOrderItem(selectedPoNo, barcode);
	}

	private PurchaseOrderItem onlyOrderItem(String poNo) {
		List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPoNoOrderByPoItemSeqAsc(poNo);
		if (items.isEmpty()) {
			throw new IllegalArgumentException("발주 자재가 없습니다.");
		}
		if (items.size() > 1) {
			throw new IllegalArgumentException("발주에 자재가 여러 건입니다. 발주번호|자재ID 형식으로 스캔하세요.");
		}
		return items.get(0);
	}

	private int indexOfSeparator(String barcode) {
		int pipe = barcode.indexOf('|');
		if (pipe >= 0) {
			return pipe;
		}
		return barcode.indexOf(',');
	}

	private Item requirePurchaseItem(String itemId) {
		if (!StringUtils.hasText(itemId) || !itemId.startsWith("1")) {
			throw new IllegalArgumentException("품번이 1로 시작하는 자재만 입고할 수 있습니다.");
		}
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("품목관리에 없는 자재입니다."));
		if (item.getUseYn() != null && !"Y".equalsIgnoreCase(item.getUseYn())) {
			throw new IllegalArgumentException("사용할 수 없는 품목입니다.");
		}
		return item;
	}

	private Warehouse resolveWarehouse(String warehouseId) {
		if (!StringUtils.hasText(warehouseId)) {
			throw new IllegalArgumentException("창고를 선택하세요.");
		}
		Warehouse warehouse = warehouseRepository.findById(warehouseId.trim())
				.orElseThrow(() -> new IllegalArgumentException("창고관리에 없는 창고입니다."));
		if (warehouse.getUseYn() != null && !"Y".equalsIgnoreCase(warehouse.getUseYn())) {
			throw new IllegalArgumentException("사용할 수 없는 창고입니다.");
		}
		return warehouse;
	}

	private PurchaseLot requireInbound(Long lotSeq) {
		PurchaseLot lot = purchaseLotRepository.findById(lotSeq)
				.orElseThrow(() -> new IllegalArgumentException("입고 LOT를 찾을 수 없습니다."));
		if (!PurchaseLot.TYPE_INBOUND.equals(lot.getLotType())) {
			throw new IllegalArgumentException("입고 LOT가 아닙니다.");
		}
		return lot;
	}

	private String nextLotNo(String prefix) {
		String head = prefix + LocalDate.now().format(LOT_DATE);
		for (int seq = 1; seq <= 9999; seq++) {
			String lotNo = head + String.format("%04d", seq);
			if (!purchaseLotRepository.existsByLotNo(lotNo)) {
				return lotNo;
			}
		}
		throw new IllegalStateException("LOT 번호를 채번할 수 없습니다.");
	}

	private void increaseStock(PurchaseLot lot, BigDecimal qty) {
		applyStockDelta(lot, qty);
	}

	private void applyStockDelta(PurchaseLot lot, BigDecimal delta) {
		if (delta.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		Stock stock = stockRepository
				.findFirstByWarehouseIdAndItemIdAndLotNo(lot.getWarehouseId(), lot.getItemId(), lot.getLotNo())
				.orElse(null);
		if (stock == null) {
			if (delta.compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalStateException("재고가 없어 수량을 줄일 수 없습니다.");
			}
			stockRepository.save(Stock.builder()
					.warehouseId(lot.getWarehouseId())
					.warehouseName(lot.getWarehouseName())
					.itemId(lot.getItemId())
					.itemName(lot.getItemName())
					.lotNo(lot.getLotNo())
					.unit(StringUtils.hasText(lot.getUnit()) ? lot.getUnit() : "Kg")
					.qty(delta)
					.locationName(lot.getWarehouseName())
					.build());
			return;
		}
		BigDecimal next = nullToZero(stock.getQty()).add(delta);
		if (next.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalStateException("재고가 부족합니다.");
		}
		stock.setQty(next);
	}

	private void removeStock(PurchaseLot lot) {
		stockRepository
				.findFirstByWarehouseIdAndItemIdAndLotNo(lot.getWarehouseId(), lot.getItemId(), lot.getLotNo())
				.ifPresent(stockRepository::delete);
	}

	private String normalizeType(String lotType) {
		if (!StringUtils.hasText(lotType) || PurchaseLot.TYPE_INBOUND.equalsIgnoreCase(lotType.trim())) {
			return PurchaseLot.TYPE_INBOUND;
		}
		if (PurchaseLot.TYPE_RETURN.equalsIgnoreCase(lotType.trim())) {
			return PurchaseLot.TYPE_RETURN;
		}
		throw new IllegalArgumentException("LOT 구분이 올바르지 않습니다.");
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
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}

}
