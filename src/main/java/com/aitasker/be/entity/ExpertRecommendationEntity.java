/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/ExpertRecommendationEntity.java
 * Day la file gi: File entity anh xa du lieu nghiep vu voi bang trong database de JPA doc/ghi.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Note: Annotation nay anh xa class Java voi mot bang trong database.
@Entity
// Note: Annotation nay chi ro bang database tuong ung voi entity.
@Table(name = "expert_recommendations")
// Note: Annotation nay giup Lombok sinh getter de doc field.
@Getter
// Note: Annotation nay giup Lombok sinh setter de cap nhat field.
@Setter
// Note: Annotation nay giup Lombok tao builder de khoi tao object ro rang hon.
@Builder
// Note: Annotation nay giup Lombok sinh constructor rong cho JPA hoac deserialize du lieu.
@NoArgsConstructor
// Note: Annotation nay giup Lombok sinh constructor nhan day du field.
@AllArgsConstructor
public class ExpertRecommendationEntity {
    // Note: Annotation nay danh dau khoa chinh cua entity.
    @Id
    // Note: Annotation nay cau hinh cach database/JPA sinh gia tri khoa chinh.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "id")
    private Long id;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "job_posting_id", nullable = false)
    private Long jobPostingId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "expert_id", nullable = false)
    private Long expertId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "portfolio_id")
    private Long portfolioId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "match_score", precision = 5, scale = 2)
    private BigDecimal matchScore;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "ai_reason", columnDefinition = "TEXT")
    private String aiReason;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "matched_skills", columnDefinition = "TEXT")
    private String matchedSkills;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "matched_domains", columnDefinition = "TEXT")
    private String matchedDomains;

    // Note: Annotation nay giup Lombok tao builder de khoi tao object ro rang hon.
    @Builder.Default
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "business_selected", nullable = false)
    private Boolean businessSelected = Boolean.FALSE;

    // Note: Annotation nay tu ghi thoi diem tao du lieu khi insert database.
    @CreationTimestamp
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
