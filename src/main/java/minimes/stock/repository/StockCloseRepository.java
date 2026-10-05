package minimes.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.stock.domain.StockClose;

public interface StockCloseRepository extends JpaRepository<StockClose, Long> {
}
