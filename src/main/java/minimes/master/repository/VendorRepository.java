package minimes.master.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.master.domain.Vendor;

public interface VendorRepository extends JpaRepository<Vendor, String> {

	List<Vendor> findAllByOrderByVendorIdAsc();
}
