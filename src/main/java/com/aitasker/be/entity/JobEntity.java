package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "jobs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class JobEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id") private Integer jobId;
    @Column(name = "business_id", nullable = false) private Integer businessId;
    @Column(name = "title", nullable = false, length = 255) private String title;
    @Column(name = "raw_requirements", nullable = false) private String rawRequirements;
    @Column(name = "structured_sow") private String structuredSow;
    @Column(name = "ai_tag", length = 50) private String aiTag;
    @Column(name = "budget", nullable = false) private BigDecimal budget;
    @Column(name = "status", nullable = false, length = 50) private String status;
    @Column(name = "planned_duration_value") private Integer plannedDurationValue;
    @Column(name = "planned_duration_unit", length = 20) private String plannedDurationUnit;
    @Column(name = "is_hot") private Boolean isHot;
    @Column(name = "hot_until") private LocalDateTime hotUntil;
    @Column(name = "published_at") private LocalDateTime publishedAt;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
