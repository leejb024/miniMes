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
import minimes.stock.dto.StockResponse;
import minimes.stock.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockService {

	private final StockRepository stockRepository;
	private final PurchaseLotRepository purchaseLotRepository;
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
}
