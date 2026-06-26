/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/ContractDepositRepository.java
 * Day la file gi: File repository dinh nghia cong truy cap du lieu, de Spring Data JPA sinh truy van toi database.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.ContractDepositEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractDepositRepository extends JpaRepository<ContractDepositEntity, Long> {
    // Note: Ham `findByContractId` truy cap hoac truy van du lieu phuc vu tang service.
    Optional<ContractDepositEntity> findByContractId(Integer contractId);

    Optional<ContractDepositEntity> findByHoldTransactionId(Long holdTransactionId);

    Optional<ContractDepositEntity> findByRefundTransactionId(Long refundTransactionId);
}
