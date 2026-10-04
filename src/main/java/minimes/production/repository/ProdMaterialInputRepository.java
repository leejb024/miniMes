package minimes.production.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.production.domain.ProdMaterialInput;

public interface ProdMaterialInputRepository extends JpaRepository<ProdMaterialInput, Long> {

	List<ProdMaterialInput> findByWorkOrderIdOrderByInputSeqAsc(String workOrderId);
}
