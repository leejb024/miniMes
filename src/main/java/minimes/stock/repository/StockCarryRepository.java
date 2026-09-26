package minimes.stock.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.stock.domain.StockCarry;

public interface StockCarryRepository extends JpaRepository<StockCarry, Long> {

	List<StockCarry> findAllByOrderByCarrySeqDesc();

	List<StockCarry> findByStockSeqOrderByCarrySeqDesc(Long stockSeq);

	boolean existsByStockSeqAndCarryTypeAndBaseDate(Long stockSeq, String carryType, LocalDate baseDate);

	List<StockCarry> findByStockSeqAndCarryTypeOrderByBaseDateAscCarrySeqAsc(Long stockSeq, String carryType);

	boolean existsBySourceCarrySeq(Long sourceCarrySeq);
}
