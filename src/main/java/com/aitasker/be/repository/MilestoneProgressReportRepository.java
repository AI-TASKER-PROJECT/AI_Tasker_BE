package com.aitasker.be.repository;

import com.aitasker.be.entity.MilestoneProgressReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneProgressReportRepository extends JpaRepository<MilestoneProgressReportEntity, Long> {
    List<MilestoneProgressReportEntity> findByMilestoneIdOrderByCreatedAtAsc(Integer milestoneId);
}
