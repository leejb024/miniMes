package minimes.interfaces.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import minimes.interfaces.domain.InterfaceQueue;

import jakarta.persistence.LockModeType;

public interface InterfaceQueueRepository extends JpaRepository<InterfaceQueue, Long> {

	boolean existsByMessageId(String messageId);

	Optional<InterfaceQueue> findByMessageId(String messageId);

	List<InterfaceQueue> findByMessageIdIn(Collection<String> messageIds);

	List<InterfaceQueue> findByStatusOrderByIfSeqAsc(String status);

	List<InterfaceQueue> findAllByOrderByIfSeqDesc();

	List<InterfaceQueue> findByStatusOrderByIfSeqDesc(String status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select o from InterfaceQueue o where o.ifSeq = :ifSeq")
	Optional<InterfaceQueue> findByIdForUpdate(@Param("ifSeq") Long ifSeq);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select o from InterfaceQueue o where o.messageId = :messageId")
	Optional<InterfaceQueue> findByMessageIdForUpdate(@Param("messageId") String messageId);
}
