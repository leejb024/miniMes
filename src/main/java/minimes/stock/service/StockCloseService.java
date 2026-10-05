package minimes.stock.service;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.stock.domain.StockClose;
import minimes.stock.repository.StockCloseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockCloseService {

	public static final String CLOSED_INBOUND_MESSAGE = "마감된 기간의 입고는 수정, 삭제할 수 없습니다.";

	private final StockCloseRepository stockCloseRepository;

	@Transactional(readOnly = true)
	public void assertDateOpen(LocalDate date, String message) {
		if (date != null && isClosed(date)) {
			throw new IllegalStateException(message);
		}
	}

	@Transactional(readOnly = true)
	public boolean isClosed(LocalDate date) {
		if (date == null) {
			return false;
		}
		for (StockClose close : stockCloseRepository.findAll()) {
			if (covers(close, date)) {
				return true;
			}
		}
		return false;
	}

	private boolean covers(StockClose close, LocalDate date) {
		if (StockClose.TYPE_MONTH.equals(close.getCloseType()) && StringUtils.hasText(close.getCloseMonth())) {
			YearMonth month = YearMonth.parse(close.getCloseMonth());
			return !date.isBefore(month.atDay(1)) && !date.isAfter(month.atEndOfMonth());
		}
		return close.getBaseDate() != null && !date.isAfter(close.getBaseDate());
	}
}
