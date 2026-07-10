package com.aitasker.be.repository;

import com.aitasker.be.entity.MilestoneProgressReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MilestoneProgressReportRepository extends JpaRepository<MilestoneProgressReportEntity, Long> {
    List<MilestoneProgressReportEntity> findByMilestoneIdOrderByCreatedAtAsc(Integer milestoneId);
    Optional<MilestoneProgressReportEntity> findFirstByContractIdAndMilestoneIdOrderByCreatedAtDesc(
            Integer contractId, Integer milestoneId);
}
