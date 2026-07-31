/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/MembershipPackageRepository.java
 * Day la file gi: File repository dinh nghia cong truy cap du lieu, de Spring Data JPA sinh truy van toi database.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.MembershipPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipPackageRepository extends JpaRepository<MembershipPackageEntity, Long> {
    // Note: Ham `findByRoleTypeAndIsActiveTrueOrderByPriceAsc` truy cap hoac truy van du lieu phuc vu tang service.
    List<MembershipPackageEntity> findByRoleTypeAndIsActiveTrueOrderByPriceAsc(String roleType);

    // Note: Ham `findByPackageIdAndIsActiveTrue` truy cap hoac truy van du lieu phuc vu tang service.
    Optional<MembershipPackageEntity> findByPackageIdAndIsActiveTrue(Long packageId);

    // Note: Ham `findByPackageCodeIgnoreCase` truy cap goi membership theo ma khong phan biet hoa thuong.
    Optional<MembershipPackageEntity> findByPackageCodeIgnoreCase(String packageCode);

    List<MembershipPackageEntity> findByIsActiveTrueOrderByRoleTypeAscPriceAscPackageNameAsc();
}
