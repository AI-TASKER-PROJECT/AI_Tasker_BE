/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/UserQuotaRepository.java
 * Day la file gi: File repository dinh nghia cong truy cap du lieu, de Spring Data JPA sinh truy van toi database.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.UserQuotaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface UserQuotaRepository extends JpaRepository<UserQuotaEntity, Long> {
    // Note: Ham `findByAccountId` truy cap hoac truy van du lieu phuc vu tang service.
    Optional<UserQuotaEntity> findByAccountId(Integer accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from UserQuotaEntity q where q.accountId = :accountId")
    // Note: Ham `findByAccountIdForUpdate` truy cap hoac truy van du lieu phuc vu tang service.
    Optional<UserQuotaEntity> findByAccountIdForUpdate(Integer accountId);
}
