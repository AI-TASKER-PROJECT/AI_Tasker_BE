package com.aitasker.be.repository;

import com.aitasker.be.entity.WalletTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity, Long> {
    List<WalletTransactionEntity> findByAccountIdOrderByCreatedAtDesc(Integer accountId);

    boolean existsByPaymentOrderIdAndTransactionType(Long paymentOrderId, String transactionType);
}
