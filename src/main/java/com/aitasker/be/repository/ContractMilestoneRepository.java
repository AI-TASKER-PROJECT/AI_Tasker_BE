/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/ContractMilestoneRepository.java
 * Đây là file gì: Repository truy cập dữ liệu milestone đã chốt theo hợp đồng.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.ContractMilestoneEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContractMilestoneRepository extends JpaRepository<ContractMilestoneEntity, Integer> {
    List<ContractMilestoneEntity> findByContractIdOrderByOrderIndexAsc(Integer contractId);
    void deleteByContractId(Integer contractId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cm from ContractMilestoneEntity cm where cm.contractId = :contractId and cm.jobMilestoneId = :milestoneId")
    Optional<ContractMilestoneEntity> findByContractIdAndJobMilestoneIdForUpdate(
            @Param("contractId") Integer contractId,
            @Param("milestoneId") Integer milestoneId
    );
}
