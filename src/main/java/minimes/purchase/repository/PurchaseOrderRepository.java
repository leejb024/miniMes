package minimes.purchase.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.purchase.domain.PurchaseOrder;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {

	List<PurchaseOrder> findAllByOrderByPoNoAsc();

	Optional<PurchaseOrder> findTopByPoNoStartingWithOrderByPoNoDesc(String prefix);
}
