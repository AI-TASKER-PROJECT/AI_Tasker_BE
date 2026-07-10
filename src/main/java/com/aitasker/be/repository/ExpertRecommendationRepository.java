/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/ExpertRecommendationRepository.java
 * Day la file gi: File repository dinh nghia cong truy cap du lieu, de Spring Data JPA sinh truy van toi database.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.ExpertRecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpertRecommendationRepository extends JpaRepository<ExpertRecommendationEntity, Long> {
    // Note: Ham `findByJobPostingIdOrderByRankPositionAsc` truy cap hoac truy van du lieu phuc vu tang service.
    List<ExpertRecommendationEntity> findByJobPostingIdOrderByRankPositionAsc(Long jobPostingId);

    // Note: Ham `deleteByJobPostingId` truy cap hoac truy van du lieu phuc vu tang service.
    void deleteByJobPostingId(Long jobPostingId);

    // Note: Ham `existsByJobPostingId` truy cap hoac truy van du lieu phuc vu tang service.
    boolean existsByJobPostingId(Long jobPostingId);

    // Note: Ham `findByJobPostingIdAndExpertId` truy cap hoac truy van du lieu phuc vu tang service.
    Optional<ExpertRecommendationEntity> findByJobPostingIdAndExpertId(Long jobPostingId, Long expertId);
}
