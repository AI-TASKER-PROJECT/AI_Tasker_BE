package com.aitasker.be.repository;

import com.aitasker.be.entity.ExpertRecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ExpertRecommendationRepository extends JpaRepository<ExpertRecommendationEntity, Integer> {
    List<ExpertRecommendationEntity> findByJobIdOrderByMatchScoreDescCreatedAtDesc(Integer jobId);

    @Transactional
    void deleteByJobId(Integer jobId);
}
