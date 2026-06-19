package com.aitasker.be.repository;

import com.aitasker.be.entity.UserQuotaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface UserQuotaRepository extends JpaRepository<UserQuotaEntity, Long> {
    Optional<UserQuotaEntity> findByAccountId(Integer accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from UserQuotaEntity q where q.accountId = :accountId")
    Optional<UserQuotaEntity> findByAccountIdForUpdate(Integer accountId);
}
