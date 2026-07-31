/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/MembershipPurchaseRepository.java
 * Day la file gi: File repository dinh nghia cong truy cap du lieu, de Spring Data JPA sinh truy van toi database.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.MembershipPurchaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MembershipPurchaseRepository extends JpaRepository<MembershipPurchaseEntity, Long> {
    // Note: Ham `findByAccountIdOrderByCreatedAtDesc` truy cap hoac truy van du lieu phuc vu tang service.
    List<MembershipPurchaseEntity> findByAccountIdOrderByCreatedAtDesc(Integer accountId);

    Optional<MembershipPurchaseEntity> findByWalletTransactionId(Long walletTransactionId);

    @Query("""
            select coalesce(sum(p.amount), 0)
            from MembershipPurchaseEntity p
            where p.status = 'SUCCESS'
            """)
    BigDecimal sumSuccessfulMembershipRevenue();
}
