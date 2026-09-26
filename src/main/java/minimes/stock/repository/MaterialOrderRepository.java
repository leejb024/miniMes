package minimes.stock.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.stock.domain.MaterialOrder;

public interface MaterialOrderRepository extends JpaRepository<MaterialOrder, Long> {

	List<MaterialOrder> findAllByOrderByOrderSeqDesc();

	Optional<MaterialOrder> findByOrderNo(String orderNo);

	Optional<MaterialOrder> findTopByOrderNoStartingWithOrderByOrderNoDesc(String prefix);
}
