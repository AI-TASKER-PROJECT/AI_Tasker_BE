/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/PaymentOrderRepository.java
 * Day la file gi: File repository dinh nghia cong truy cap du lieu, de Spring Data JPA sinh truy van toi database.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.PaymentOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrderEntity, Long> {
    // Note: Ham `findByProviderOrderCode` truy cap hoac truy van du lieu phuc vu tang service.
    Optional<PaymentOrderEntity> findByProviderOrderCode(Long providerOrderCode);

    // Note: Ham `existsByProviderOrderCode` truy cap hoac truy van du lieu phuc vu tang service.
    boolean existsByProviderOrderCode(Long providerOrderCode);
}
