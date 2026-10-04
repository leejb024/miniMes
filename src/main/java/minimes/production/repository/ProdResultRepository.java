package minimes.production.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import minimes.production.domain.ProdResult;
import minimes.production.dto.ProdResultPostStatusView;
import minimes.production.dto.ProdResultView;

public interface ProdResultRepository extends JpaRepository<ProdResult, Long> {

	@Query("""
			select a.workOrderId as workOrderId,
			       a.plantId as plantId,
			       a.lotId as lotId,
			       sum(a.prodQty) as prodQty,
			       min(a.productionStartTime) as productionStartTime,
			       max(a.productionEndTime) as productionEndTime,
			       max(a.productionNo) as productionNo,
			       max(b.unit) as unit
			from ProdResult a
			left join WorkOrder b
			  on a.plantId = b.plantId
			 and a.workOrderId = b.workOrderId
			where a.workOrderId = :workOrderId
			group by a.plantId, a.workOrderId, a.lotId
			order by a.lotId
			""")
	List<ProdResultView> findGoodResults(@Param("workOrderId") String workOrderId);

	List<ProdResult> findByWorkOrderIdAndLotIdOrderByProductionStartTimeAsc(String workOrderId, String lotId);

	List<ProdResult> findByWorkOrderIdOrderByLotIdAscProdResultSeqAsc(String workOrderId);

	List<ProdResult> findByWorkOrderIdAndIsConfirmedOrderByProdResultSeqAsc(String workOrderId, String isConfirmed);

	boolean existsByLotId(String lotId);

	@Query("select coalesce(max(p.prodResultSeq), 0) from ProdResult p")
	Long findMaxSeq();

	@Query("""
			select p.workOrderId as workOrderId,
			       sum(case when p.isConfirmed = 'N' then 1 else 0 end) as pendingCount,
			       count(p) as resultCount,
			       coalesce(sum(p.prodQty), 0) as prodQty,
			       sum(case when p.resultType = 'PACK' then 1 else 0 end) as packCount
			from ProdResult p
			where p.workOrderId in :workOrderIds
			group by p.workOrderId
			""")
	List<ProdResultPostStatusView> countPostStatusByWorkOrderIds(@Param("workOrderIds") List<String> workOrderIds);
}
