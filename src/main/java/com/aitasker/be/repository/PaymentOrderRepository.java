package com.aitasker.be.repository;

import com.aitasker.be.entity.PaymentOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrderEntity, Long> {
    Optional<PaymentOrderEntity> findByProviderOrderCode(Long providerOrderCode);

    boolean existsByProviderOrderCode(Long providerOrderCode);
}
