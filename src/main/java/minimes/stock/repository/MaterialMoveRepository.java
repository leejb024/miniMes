package minimes.stock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.stock.domain.MaterialMove;

public interface MaterialMoveRepository extends JpaRepository<MaterialMove, Long> {

	List<MaterialMove> findByOrderNoOrderByMoveSeqAsc(String orderNo);

	void deleteByMoveTypeIn(List<String> moveTypes);
}
