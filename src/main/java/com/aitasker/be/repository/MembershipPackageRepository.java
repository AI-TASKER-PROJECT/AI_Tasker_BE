package com.aitasker.be.repository;

import com.aitasker.be.entity.MembershipPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipPackageRepository extends JpaRepository<MembershipPackageEntity, Long> {
    List<MembershipPackageEntity> findByRoleTypeAndIsActiveTrueOrderByPriceAsc(String roleType);

    Optional<MembershipPackageEntity> findByPackageIdAndIsActiveTrue(Long packageId);
}
