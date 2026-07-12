/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/WithdrawalRequestRepository.java
 * Day la file gi: File repository dinh nghia cong truy cap du lieu, de Spring Data JPA sinh truy van toi database.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.WithdrawalRequestEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequestEntity, Long> {
    // Note: Ham `findByAccountIdOrderByRequestedAtDesc` truy cap hoac truy van du lieu phuc vu tang service.
    List<WithdrawalRequestEntity> findByAccountIdOrderByRequestedAtDesc(Integer accountId);

    // Note: Ham `findAllByOrderByRequestedAtDesc` truy cap hoac truy van du lieu phuc vu tang service.
    List<WithdrawalRequestEntity> findAllByOrderByRequestedAtDesc();

    Optional<WithdrawalRequestEntity> findByHoldTransactionId(Long holdTransactionId);

    Optional<WithdrawalRequestEntity> findByReviewTransactionId(Long reviewTransactionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wr from WithdrawalRequestEntity wr where wr.withdrawalId = :withdrawalId")
    Optional<WithdrawalRequestEntity> findByIdForUpdate(@Param("withdrawalId") Long withdrawalId);
}
