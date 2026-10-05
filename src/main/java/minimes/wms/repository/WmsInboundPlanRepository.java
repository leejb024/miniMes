package minimes.wms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.wms.domain.WmsInboundPlan;

public interface WmsInboundPlanRepository extends JpaRepository<WmsInboundPlan, Long> {

	boolean existsByMessageId(String messageId);

	Optional<WmsInboundPlan> findByMessageId(String messageId);

	List<WmsInboundPlan> findAllByOrderByPlanSeqDesc();
}
