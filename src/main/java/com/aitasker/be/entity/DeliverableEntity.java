package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity @Table(name = "deliverables")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliverableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deliverable_id") private Integer deliverableId;
    @Column(name = "milestone_id", nullable = false) private Integer milestoneId;
    @Column(name = "source_code_url", length = 255) private String sourceCodeUrl;
    @Column(name = "demo_link", length = 255) private String demoLink;
    @Column(name = "submission_notes") private String submissionNotes;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
