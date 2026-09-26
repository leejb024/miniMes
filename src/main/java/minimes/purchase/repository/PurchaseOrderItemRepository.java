package minimes.purchase.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.purchase.domain.PurchaseOrderItem;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

	List<PurchaseOrderItem> findByPoNoInOrderByPoItemSeqAsc(Collection<String> poNos);

	List<PurchaseOrderItem> findByPoNoOrderByPoItemSeqAsc(String poNo);

	void deleteByPoNo(String poNo);

	boolean existsByItemId(String itemId);
}
