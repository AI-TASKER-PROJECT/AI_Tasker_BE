/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/WalletTransactionRepository.java
 * Day la file gi: File repository dinh nghia cong truy cap du lieu, de Spring Data JPA sinh truy van toi database.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.WalletTransactionEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity, Long> {
    // Note: Ham `findAllByOrderByCreatedAtDesc` lay ledger toan he thong cho admin xem lich su vi nen tang.
    List<WalletTransactionEntity> findAllByOrderByCreatedAtDesc();

    // Note: Ham `findByAccountIdOrderByCreatedAtDesc` truy cap hoac truy van du lieu phuc vu tang service.
    List<WalletTransactionEntity> findByAccountIdOrderByCreatedAtDesc(Integer accountId);

    // Note: Ham `existsByPaymentOrderIdAndTransactionType` truy cap hoac truy van du lieu phuc vu tang service.
    boolean existsByPaymentOrderIdAndTransactionType(Long paymentOrderId, String transactionType);

    List<WalletTransactionEntity> findByReferenceTypeAndReferenceIdOrderByCreatedAtAsc(String referenceType, Long referenceId);

    Optional<WalletTransactionEntity> findByOperationKeyAndOperationLeg(String operationKey, String operationLeg);

    List<WalletTransactionEntity> findByOperationKeyOrderByCreatedAtAscIdAsc(String operationKey);

    @Query("""
            select coalesce(sum(wt.amount), 0)
            from WalletTransactionEntity wt
            where wt.status = 'POSTED'
              and wt.direction = 'DEBIT'
              and wt.balanceType = 'AVAILABLE'
              and wt.transactionType in ('MEMBERSHIP_PURCHASE', 'CREDIT_PURCHASE')
            """)
    BigDecimal sumPostedPlatformPurchaseRevenue();

    @Query("""
            select coalesce(sum(
                case
                    when wt.direction = 'HOLD' then wt.amount
                    when wt.direction in ('RELEASE', 'DEBIT') then -wt.amount
                    else 0
                end
            ), 0)
            from WalletTransactionEntity wt
            where wt.status = 'POSTED'
              and wt.balanceType = 'ESCROW'
            """)
    BigDecimal calculatePostedEscrowBalance();

    @Query("""
            select wt
            from WalletTransactionEntity wt
            where wt.accountId = :accountId
              and wt.referenceType = :referenceType
              and wt.referenceId = :referenceId
              and wt.transactionType = :transactionType
              and wt.createdAt between :windowStart and :windowEnd
            order by wt.createdAt asc, wt.id asc
            """)
    List<WalletTransactionEntity> findLegacyOperationWindow(
            @Param("accountId") Integer accountId,
            @Param("referenceType") String referenceType,
            @Param("referenceId") Long referenceId,
            @Param("transactionType") String transactionType,
            @Param("windowStart") java.time.LocalDateTime windowStart,
            @Param("windowEnd") java.time.LocalDateTime windowEnd
    );
}
