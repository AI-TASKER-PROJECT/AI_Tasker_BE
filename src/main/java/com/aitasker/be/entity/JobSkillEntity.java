package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_skills")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class JobSkillEntity {
    @EmbeddedId private JobSkillId id;
    @Column(name = "required_level", length = 50) private String requiredLevel;
    @Column(name = "is_mandatory", nullable = false) private Boolean isMandatory;
    @Column(name = "min_years_experience") private Integer minYearsExperience;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
}
