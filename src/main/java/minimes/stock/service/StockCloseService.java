package minimes.stock.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import minimes.stock.domain.StockClose;
import minimes.stock.dto.StockCloseRequest;
import minimes.stock.dto.StockCloseResponse;
import minimes.stock.repository.StockCloseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockCloseService {

	public static final String CLOSED_INBOUND_MESSAGE = "마감된 기간의 입고는 수정, 삭제, 반품할 수 없습니다.";

	private final StockCloseRepository stockCloseRepository;

	@Transactional(readOnly = true)
	public List<StockCloseResponse> findAll() {
		List<StockCloseResponse> responses = new ArrayList<>();
		for (StockClose close : stockCloseRepository.findAllByOrderByCloseSeqDesc()) {
			responses.add(StockCloseResponse.from(close));
		}
		return responses;
	}

	@Transactional
	public StockCloseResponse close(StockCloseRequest request, String creId) {
		String type = normalizeType(request.getCloseType());
		LocalDate today = LocalDate.now();
		String closeMonth = null;
		LocalDate baseDate;

		if (StockClose.TYPE_MONTH.equals(type)) {
			closeMonth = requireMonth(request.getCloseMonth());
			YearMonth month = YearMonth.parse(closeMonth);
			if (month.isAfter(YearMonth.from(today))) {
				throw new IllegalArgumentException("미래 월은 마감할 수 없습니다.");
			}
			if (stockCloseRepository.existsByCloseTypeAndCloseMonth(type, closeMonth)) {
				throw new IllegalStateException("이미 마감된 월입니다.");
			}
			baseDate = month.atEndOfMonth();
		} else {
			baseDate = request.getBaseDate();
			if (baseDate == null) {
				throw new IllegalArgumentException("기준일자를 선택하세요.");
			}
			if (baseDate.isAfter(today)) {
				throw new IllegalArgumentException("미래 일자는 마감할 수 없습니다.");
			}
			if (stockCloseRepository.existsByCloseTypeAndBaseDate(type, baseDate)) {
				throw new IllegalStateException("이미 마감된 기준일자입니다.");
			}
		}

		StockClose saved = stockCloseRepository.save(StockClose.builder()
				.closeType(type)
				.closeMonth(closeMonth)
				.baseDate(baseDate)
				.remark(trimToNull(request.getRemark()))
				.creId(StringUtils.hasText(creId) ? creId : "admin")
				.build());
		return StockCloseResponse.from(saved);
	}

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

	private String normalizeType(String closeType) {
		if (!StringUtils.hasText(closeType)) {
			throw new IllegalArgumentException("마감 구분을 선택하세요.");
		}
		String type = closeType.trim().toUpperCase();
		if (!StockClose.TYPE_MONTH.equals(type) && !StockClose.TYPE_DATE.equals(type)) {
			throw new IllegalArgumentException("마감 구분은 월마감 또는 기준일자입니다.");
		}
		return type;
	}

	private String requireMonth(String closeMonth) {
		if (!StringUtils.hasText(closeMonth)) {
			throw new IllegalArgumentException("마감월을 선택하세요.");
		}
		String month = closeMonth.trim();
		try {
			YearMonth.parse(month);
		} catch (DateTimeParseException ex) {
			throw new IllegalArgumentException("마감월 형식이 올바르지 않습니다.");
		}
		return month;
	}

	private String trimToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
