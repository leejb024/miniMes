package minimes.stock.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.purchase.domain.PurchaseLot;
import minimes.purchase.repository.PurchaseLotRepository;
import minimes.stock.domain.Stock;
import minimes.stock.domain.StockCarry;
import minimes.stock.dto.StockCarryRequest;
import minimes.stock.dto.StockCarryResponse;
import minimes.stock.dto.StockResponse;
import minimes.stock.repository.StockCarryRepository;
import minimes.stock.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockService {

	private final StockRepository stockRepository;
	private final PurchaseLotRepository purchaseLotRepository;
	private final StockCarryRepository stockCarryRepository;
	private final StockCloseService stockCloseService;

	@Transactional(readOnly = true)
	public List<StockResponse> findAll() {
		Set<String> inboundLotKeys = new HashSet<>();
		for (PurchaseLot lot : purchaseLotRepository.findByLotTypeOrderByLotSeqDesc(PurchaseLot.TYPE_INBOUND)) {
			inboundLotKeys.add(lotKey(lot.getItemId(), lot.getLotNo()));
		}

		List<StockResponse> responses = new ArrayList<>();
		for (Stock stock : stockRepository.findAllByOrderByWarehouseIdAscItemIdAsc()) {
			if (stock.getQty() == null || stock.getQty().signum() <= 0) {
				continue;
			}
			if (inboundLotKeys.contains(lotKey(stock.getItemId(), stock.getLotNo()))) {
				responses.add(StockResponse.from(stock));
			}
		}
		return responses;
	}

	private String lotKey(String itemId, String lotNo) {
		return itemId + "|" + lotNo;
	}

	@Transactional(readOnly = true)
	public List<StockCarryResponse> findCarries(Long stockSeq) {
		List<StockCarry> carries = stockSeq == null
				? stockCarryRepository.findAllByOrderByCarrySeqDesc()
				: stockCarryRepository.findByStockSeqOrderByCarrySeqDesc(stockSeq);
		List<StockCarryResponse> responses = new ArrayList<>();
		for (StockCarry carry : carries) {
			responses.add(StockCarryResponse.from(carry));
		}
		return responses;
	}

	@Transactional
	public StockCarryResponse carry(StockCarryRequest request, String creId) {
		String type = normalizeCarryType(request.getCarryType());
		LocalDate baseDate = request.getBaseDate();
		if (baseDate.isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("기준일자는 오늘 이전이어야 합니다.");
		}
		stockCloseService.assertDateOpen(baseDate, "마감된 기간에는 이월 또는 반입할 수 없습니다.");

		Stock stock = stockRepository.findById(request.getStockSeq())
				.orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다."));
		if (!isInboundStock(stock)) {
			throw new IllegalArgumentException("입고 LOT로 등록된 재고만 이월 또는 반입할 수 있습니다.");
		}
		BigDecimal qty = stock.getQty() == null ? BigDecimal.ZERO : stock.getQty();
		if (qty.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalStateException("재고 수량이 없어 처리할 수 없습니다.");
		}
		if (stockCarryRepository.existsByStockSeqAndCarryTypeAndBaseDate(stock.getStockSeq(), type, baseDate)) {
			throw new IllegalStateException("이미 같은 기준일자로 처리된 재고입니다.");
		}

		Long sourceCarrySeq = null;
		if (StockCarry.TYPE_BRING.equals(type)) {
			StockCarry source = findOpenCarry(stock.getStockSeq(), baseDate);
			sourceCarrySeq = source.getCarrySeq();
			qty = source.getQty() == null ? BigDecimal.ZERO : source.getQty();
		}

		StockCarry saved = stockCarryRepository.save(StockCarry.builder()
				.carryType(type)
				.baseDate(baseDate)
				.stockSeq(stock.getStockSeq())
				.sourceCarrySeq(sourceCarrySeq)
				.warehouseId(stock.getWarehouseId())
				.warehouseName(stock.getWarehouseName())
				.itemId(stock.getItemId())
				.itemName(stock.getItemName())
				.lotNo(stock.getLotNo())
				.unit(stock.getUnit())
				.qty(qty)
				.remark(trimToNull(request.getRemark()))
				.creId(StringUtils.hasText(creId) ? creId : "admin")
				.build());
		return StockCarryResponse.from(saved);
	}

	private StockCarry findOpenCarry(Long stockSeq, LocalDate baseDate) {
		for (StockCarry carry : stockCarryRepository.findByStockSeqAndCarryTypeOrderByBaseDateAscCarrySeqAsc(
				stockSeq, StockCarry.TYPE_CARRY)) {
			if (carry.getBaseDate() != null && carry.getBaseDate().isAfter(baseDate)) {
				continue;
			}
			if (!stockCarryRepository.existsBySourceCarrySeq(carry.getCarrySeq())) {
				return carry;
			}
		}
		throw new IllegalStateException("반입할 이월 재고가 없습니다.");
	}

	private boolean isInboundStock(Stock stock) {
		for (PurchaseLot lot : purchaseLotRepository.findByLotTypeOrderByLotSeqDesc(PurchaseLot.TYPE_INBOUND)) {
			if (stockKey(stock.getWarehouseId(), stock.getItemId(), stock.getLotNo())
					.equals(stockKey(lot.getWarehouseId(), lot.getItemId(), lot.getLotNo()))) {
				return true;
			}
		}
		return false;
	}

	private String normalizeCarryType(String carryType) {
		if (!StringUtils.hasText(carryType)) {
			throw new IllegalArgumentException("처리 구분을 선택하세요.");
		}
		String type = carryType.trim().toUpperCase();
		if (!StockCarry.TYPE_CARRY.equals(type) && !StockCarry.TYPE_BRING.equals(type)) {
			throw new IllegalArgumentException("처리 구분은 이월 또는 반입입니다.");
		}
		return type;
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}

	private String stockKey(String warehouseId, String itemId, String lotNo) {
		return warehouseId + "|" + itemId + "|" + lotNo;
	}
}
