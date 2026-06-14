package com.aitasker.be.repository;

import com.aitasker.be.entity.PaymentOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrderEntity, Long> {
    Optional<PaymentOrderEntity> findByVnpTxnRef(String vnpTxnRef);

    List<PaymentOrderEntity> findByBusinessId(Long businessId);

    boolean existsByVnpTxnRef(String vnpTxnRef);
}
