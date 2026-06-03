package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity @Table(name = "acceptance_criteria")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AcceptanceCriteriaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "criteria_id") private Integer criteriaId;
    @Column(name = "milestone_id", nullable = false) private Integer milestoneId;
    @Column(name = "description", nullable = false) private String description;
    @Column(name = "is_passed", nullable = false) private Boolean isPassed;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
