package minimes.production.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import minimes.production.domain.WorkOrder;
import minimes.production.dto.WorkOrderView;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, String> {

	Optional<WorkOrder> findTopByWorkOrderIdStartingWithOrderByWorkOrderIdDesc(String prefix);

	boolean existsByItemId(String itemId);

	@Query("""
			select t1.planDate as planDate,
			       t1.workOrderId as workOrderId,
			       t1.workcenterId as workcenterId,
			       w.workcenterName as workcenterName,
			       t1.processId as processId,
			       pr.processName as processName,
			       t1.equipId as equipId,
			       eq.equipName as equipName,
			       t1.itemId as itemId,
			       t1.state as state,
			       t1.unit as unit,
			       t1.planQty as planQty,
			       t1.expiredDate as expiredDate,
			       t1.itemVersion as itemVersion,
			       t7.bomVersion as bomVersion,
			       t1.closeYn as closeYn
			from WorkOrder t1
			left join Bom t7
			  on t1.plantId = t7.plantId
			 and t1.itemId = t7.bomId
			 and t1.itemVersion = t7.bomVersion
			 and t7.useYn = 'Y'
			left join Workcenter w
			  on t1.workcenterId = w.workcenterId
			left join Process pr
			  on t1.processId = pr.processId
			left join Equipment eq
			  on t1.equipId = eq.equipId
			where t1.planDate between :planDateFrom and :planDateTo
			group by t1.planDate,
			         t1.state,
			         t1.workOrderId,
			         t1.workcenterId,
			         w.workcenterName,
			         t1.processId,
			         pr.processName,
			         t1.equipId,
			         eq.equipName,
			         t1.itemId,
			         t1.unit,
			         t1.planQty,
			         t1.expiredDate,
			         t1.itemVersion,
			         t7.bomVersion,
			         t1.closeYn
			order by t1.planDate, t1.workOrderId
			""")
	List<WorkOrderView> search(
			@Param("planDateFrom") LocalDate planDateFrom,
			@Param("planDateTo") LocalDate planDateTo);
}
