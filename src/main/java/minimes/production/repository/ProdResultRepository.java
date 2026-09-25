package minimes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import minimes.domain.ProdResult;
import minimes.dto.ProdResultView;

public interface ProdResultRepository extends JpaRepository<ProdResult, Long> {

	@Query("""
			select a.workOrderId as workOrderId,
			       a.plantId as plantId,
			       a.lotId as lotId,
			       sum(a.prodQty) as prodQty,
			       min(a.productionStartTime) as productionStartTime,
			       max(a.productionEndTime) as productionEndTime,
			       max(a.productionNo) as productionNo,
			       max(b.unit) as unit,
			       sum(a.wmsProdQty) as wmsProdQty,
			       sum(a.wmsConfirmQty) as wmsConfirmQty
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
}
