package minimes.production.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.production.domain.ProdInputLot;

public interface ProdInputLotRepository extends JpaRepository<ProdInputLot, Long> {

	List<ProdInputLot> findByWorkOrderIdOrderByInputLotSeqAsc(String workOrderId);
}
