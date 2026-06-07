package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "skills")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SkillEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id") private Integer skillId;
    @Column(name = "skill_code", nullable = false, unique = true, length = 50) private String skillCode;
    @Column(name = "skill_name", nullable = false, length = 255) private String skillName;
    @Column(name = "description") private String description;
    @Column(name = "is_active", nullable = false) private Boolean isActive;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
