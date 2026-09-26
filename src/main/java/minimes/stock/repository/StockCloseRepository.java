package minimes.stock.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.stock.domain.StockClose;

public interface StockCloseRepository extends JpaRepository<StockClose, Long> {

	List<StockClose> findAllByOrderByCloseSeqDesc();

	boolean existsByCloseTypeAndCloseMonth(String closeType, String closeMonth);

	boolean existsByCloseTypeAndBaseDate(String closeType, LocalDate baseDate);
}
