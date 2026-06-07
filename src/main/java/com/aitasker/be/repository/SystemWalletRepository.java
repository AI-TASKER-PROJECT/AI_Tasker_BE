package com.aitasker.be.repository;

import com.aitasker.be.entity.SystemWalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemWalletRepository extends JpaRepository<SystemWalletEntity, Long> {
    Optional<SystemWalletEntity> findTopByOrderBySystemWalletIdAsc();
    Optional<SystemWalletEntity> findByAccountId(Integer accountId);
}
