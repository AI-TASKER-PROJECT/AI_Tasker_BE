package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "milestone_progress_report_requests")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MilestoneProgressReportRequestEntity {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_report_request_id")
    private Long progressReportRequestId;
    @Column(name = "contract_id", nullable = false)
    private Integer contractId;
    @Column(name = "milestone_id", nullable = false)
    private Integer milestoneId;
    @Column(name = "requested_by_account_id", nullable = false)
    private Integer requestedByAccountId;
    @Column(name = "request_number", nullable = false)
    private Integer requestNumber;
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    @Column(name = "progress_report_id")
    private Long progressReportId;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
