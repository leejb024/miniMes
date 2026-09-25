package minimes.production.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import minimes.production.domain.WorkOrder;
import minimes.production.dto.WorkOrderView;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, String> {

	@Query("""
			select t1.planDate as planDate,
			       t1.workOrderId as workOrderId,
			       t1.workcenterId as workcenterId,
			       w.workcenterName as workcenterName,
			       t1.itemId as itemId,
			       t1.deptId as deptId,
			       d.deptName as deptName,
			       t1.state as state,
			       t1.unit as unit,
			       t1.planQty as planQty,
			       t1.expiredDate as expiredDate,
			       t1.itemVersion as itemVersion,
			       t7.bomVersion as bomVersion
			from WorkOrder t1
			left join Bom t7
			  on t1.plantId = t7.plantId
			 and t1.itemId = t7.bomId
			 and t1.itemVersion = t7.bomVersion
			 and t7.useYn = 'Y'
			left join Dept d
			  on t1.deptId = d.deptId
			left join Workcenter w
			  on t1.workcenterId = w.workcenterId
			where t1.deptId = :deptId
			  and t1.planDate between :planDateFrom and :planDateTo
			group by t1.planDate,
			         t1.state,
			         t1.workOrderId,
			         t1.workcenterId,
			         w.workcenterName,
			         t1.itemId,
			         t1.deptId,
			         d.deptName,
			         t1.unit,
			         t1.planQty,
			         t1.expiredDate,
			         t1.itemVersion,
			         t7.bomVersion
			order by t1.planDate, t1.workOrderId
			""")
	List<WorkOrderView> search(
			@Param("deptId") String deptId,
			@Param("planDateFrom") LocalDate planDateFrom,
			@Param("planDateTo") LocalDate planDateTo);
}
