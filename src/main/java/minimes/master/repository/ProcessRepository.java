package minimes.master.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.master.domain.Process;

public interface ProcessRepository extends JpaRepository<Process, String> {

	List<Process> findAllByOrderByProcessIdAsc();
}
