package com.aitasker.be.repository;

import com.aitasker.be.entity.MembershipPurchaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipPurchaseRepository extends JpaRepository<MembershipPurchaseEntity, Long> {
    List<MembershipPurchaseEntity> findByAccountIdOrderByCreatedAtDesc(Integer accountId);
}
