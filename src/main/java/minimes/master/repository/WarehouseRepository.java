package minimes.master.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.master.domain.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, String> {

	List<Warehouse> findAllByOrderByWarehouseIdAsc();
}
