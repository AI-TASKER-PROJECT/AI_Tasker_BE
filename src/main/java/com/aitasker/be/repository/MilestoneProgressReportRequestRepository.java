package com.aitasker.be.repository;

import com.aitasker.be.entity.MilestoneProgressReportRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MilestoneProgressReportRequestRepository
        extends JpaRepository<MilestoneProgressReportRequestEntity, Long> {
    List<MilestoneProgressReportRequestEntity> findByContractIdAndMilestoneIdOrderByRequestNumberAsc(
            Integer contractId, Integer milestoneId);
    Optional<MilestoneProgressReportRequestEntity> findFirstByContractIdAndMilestoneIdAndStatusOrderByRequestNumberDesc(
            Integer contractId, Integer milestoneId, String status);
    Optional<MilestoneProgressReportRequestEntity> findFirstByContractIdAndMilestoneIdOrderByRequestNumberDesc(
            Integer contractId, Integer milestoneId);
    List<MilestoneProgressReportRequestEntity> findByStatusAndDueAtBefore(String status, java.time.LocalDateTime dueAt);
}
