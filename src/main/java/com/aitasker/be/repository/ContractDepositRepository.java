/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/ContractDepositRepository.java
 * Day la file gi: File repository dinh nghia cong truy cap du lieu, de Spring Data JPA sinh truy van toi database.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.ContractDepositEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContractDepositRepository extends JpaRepository<ContractDepositEntity, Long> {
    // Note: Ham `findByContractId` truy cap hoac truy van du lieu phuc vu tang service.
    Optional<ContractDepositEntity> findByContractIdAndOwnerRole(Integer contractId, String ownerRole);
    java.util.List<ContractDepositEntity> findByContractIdOrderByOwnerRoleAsc(Integer contractId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select cd
            from ContractDepositEntity cd
            where cd.contractId = :contractId
              and cd.ownerRole = :ownerRole
            """)
    Optional<ContractDepositEntity> findByContractIdAndOwnerRoleForUpdate(
            @Param("contractId") Integer contractId,
            @Param("ownerRole") String ownerRole
    );

    default Optional<ContractDepositEntity> findByContractId(Integer contractId) {
        return findByContractIdAndOwnerRole(contractId, "BUSINESS");
    }

    Optional<ContractDepositEntity> findByHoldTransactionId(Long holdTransactionId);

    Optional<ContractDepositEntity> findByRefundTransactionId(Long refundTransactionId);
}
