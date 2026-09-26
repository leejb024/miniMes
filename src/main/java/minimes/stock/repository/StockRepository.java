package minimes.stock.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.stock.domain.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {

	List<Stock> findAllByOrderByWarehouseIdAscItemIdAsc();

	Optional<Stock> findFirstByWarehouseIdAndItemIdAndLotNo(String warehouseId, String itemId, String lotNo);

	List<Stock> findByWarehouseIdAndItemIdOrderByStockSeqAsc(String warehouseId, String itemId);

	boolean existsByItemId(String itemId);
}
