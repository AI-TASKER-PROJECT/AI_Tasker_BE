package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "milestone_progress_reports")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MilestoneProgressReportEntity {
    public static final String CHECKPOINT_MIDPOINT = "MIDPOINT";
    public static final String CHECKPOINT_PRE_DEADLINE = "PRE_DEADLINE";
    public static final String ACK_PENDING = "PENDING_BUSINESS_ACK";
    public static final String ACKNOWLEDGED = "ACKNOWLEDGED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_report_id")
    private Long progressReportId;
    @Column(name = "contract_id", nullable = false)
    private Integer contractId;
    @Column(name = "milestone_id", nullable = false)
    private Integer milestoneId;
    @Column(name = "submitted_by_account_id", nullable = false)
    private Integer submittedByAccountId;
    @Column(name = "checkpoint_type", length = 20)
    private String checkpointType;
    @Column(name = "content", nullable = false)
    private String content;
    @Column(name = "percent_complete")
    private Integer percentComplete;
    @Column(name = "attachment_url")
    private String attachmentUrl;
    @Column(name = "source_code_url")
    private String sourceCodeUrl;
    @Column(name = "demo_link")
    private String demoLink;
    @Column(name = "submission_notes")
    private String submissionNotes;
    @Column(name = "is_late", nullable = false)
    private Boolean isLate;
    @Column(name = "acknowledgement_state", nullable = false, length = 30)
    private String acknowledgementState;
    @Column(name = "acknowledged_by_account_id")
    private Integer acknowledgedByAccountId;
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
