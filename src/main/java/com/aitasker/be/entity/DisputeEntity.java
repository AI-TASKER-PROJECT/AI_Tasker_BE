package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity @Table(name = "disputes")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DisputeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispute_id") private Integer disputeId;
    @Column(name = "contract_id", nullable = false) private Integer contractId;
    @Column(name = "milestone_id") private Integer milestoneId;
    @Column(name = "assigned_staff_id") private Integer assignedStaffId;
    @Column(name = "evidence_report") private String evidenceReport;
    @Column(name = "proposed_action", length = 100) private String proposedAction;
    @Column(name = "admin_approved_by") private Integer adminApprovedBy;
    @Column(name = "status", nullable = false, length = 50) private String status;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
