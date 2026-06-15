/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/ContractMilestoneRepository.java
 * Đây là file gì: Repository truy cập dữ liệu milestone đã chốt theo hợp đồng.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.ContractMilestoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractMilestoneRepository extends JpaRepository<ContractMilestoneEntity, Integer> {
    List<ContractMilestoneEntity> findByContractIdOrderByOrderIndexAsc(Integer contractId);
    void deleteByContractId(Integer contractId);
}
