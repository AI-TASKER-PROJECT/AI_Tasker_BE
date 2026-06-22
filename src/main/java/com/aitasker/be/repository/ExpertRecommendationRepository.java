package com.aitasker.be.repository;

import com.aitasker.be.entity.ExpertRecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpertRecommendationRepository extends JpaRepository<ExpertRecommendationEntity, Long> {
    List<ExpertRecommendationEntity> findByJobPostingIdOrderByRankPositionAsc(Long jobPostingId);

    void deleteByJobPostingId(Long jobPostingId);

    boolean existsByJobPostingId(Long jobPostingId);

    Optional<ExpertRecommendationEntity> findByJobPostingIdAndExpertId(Long jobPostingId, Long expertId);
}
