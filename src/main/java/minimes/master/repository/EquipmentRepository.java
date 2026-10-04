package minimes.master.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.master.domain.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, String> {

	List<Equipment> findAllByOrderByEquipIdAsc();
}
