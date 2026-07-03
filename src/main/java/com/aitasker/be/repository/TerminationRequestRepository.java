package com.aitasker.be.repository;

import com.aitasker.be.entity.TerminationRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TerminationRequestRepository extends JpaRepository<TerminationRequestEntity, Long> {
    List<TerminationRequestEntity> findByContractIdOrderByCreatedAtDesc(Integer contractId);
    List<TerminationRequestEntity> findByContractIdAndStatusIn(Integer contractId, List<String> statuses);
    List<TerminationRequestEntity> findByAssignedStaffIdAndStatusIn(Integer assignedStaffId, List<String> statuses);
}
