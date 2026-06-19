package com.aitasker.be.repository;

import com.aitasker.be.entity.WithdrawalRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequestEntity, Long> {
    List<WithdrawalRequestEntity> findByAccountIdOrderByRequestedAtDesc(Integer accountId);

    List<WithdrawalRequestEntity> findAllByOrderByRequestedAtDesc();
}
