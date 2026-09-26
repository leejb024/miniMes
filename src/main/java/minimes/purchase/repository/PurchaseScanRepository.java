package minimes.purchase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import minimes.purchase.domain.PurchaseScan;

public interface PurchaseScanRepository extends JpaRepository<PurchaseScan, Long> {
}
