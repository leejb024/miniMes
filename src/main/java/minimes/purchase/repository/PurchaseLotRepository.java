package minimes.purchase.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.purchase.domain.PurchaseLot;

public interface PurchaseLotRepository extends JpaRepository<PurchaseLot, Long> {

	List<PurchaseLot> findByLotTypeOrderByLotSeqDesc(String lotType);

	List<PurchaseLot> findByLotTypeAndPoNoOrderByLotSeqDesc(String lotType, String poNo);

	List<PurchaseLot> findByLotTypeAndSourceLotNoOrderByLotSeqDesc(String lotType, String sourceLotNo);

	List<PurchaseLot> findByLotTypeAndPoItemSeq(String lotType, Long poItemSeq);

	Optional<PurchaseLot> findByLotNo(String lotNo);

	boolean existsByLotNo(String lotNo);

	boolean existsByItemId(String itemId);
}
