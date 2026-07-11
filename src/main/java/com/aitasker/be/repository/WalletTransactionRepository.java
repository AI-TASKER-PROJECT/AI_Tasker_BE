/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/WalletTransactionRepository.java
 * Day la file gi: File repository dinh nghia cong truy cap du lieu, de Spring Data JPA sinh truy van toi database.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.WalletTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity, Long> {
    // Note: Ham `findAllByOrderByCreatedAtDesc` lay ledger toan he thong cho admin xem lich su vi nen tang.
    List<WalletTransactionEntity> findAllByOrderByCreatedAtDesc();

    // Note: Ham `findByAccountIdOrderByCreatedAtDesc` truy cap hoac truy van du lieu phuc vu tang service.
    List<WalletTransactionEntity> findByAccountIdOrderByCreatedAtDesc(Integer accountId);

    // Note: Ham `existsByPaymentOrderIdAndTransactionType` truy cap hoac truy van du lieu phuc vu tang service.
    boolean existsByPaymentOrderIdAndTransactionType(Long paymentOrderId, String transactionType);

    List<WalletTransactionEntity> findByReferenceTypeAndReferenceIdOrderByCreatedAtAsc(String referenceType, Long referenceId);
}
