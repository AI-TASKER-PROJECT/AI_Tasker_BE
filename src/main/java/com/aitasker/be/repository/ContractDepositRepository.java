package com.aitasker.be.repository;

import com.aitasker.be.entity.ContractDepositEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractDepositRepository extends JpaRepository<ContractDepositEntity, Long> {
    Optional<ContractDepositEntity> findByContractId(Integer contractId);
}
