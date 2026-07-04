/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/DisputeEntity.java
 * Đây là file gì: File entity ánh xạ bảng database sang object Java, thể hiện cấu trúc dữ liệu được lưu trữ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Entity @Table(name = "disputes")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DisputeEntity {
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    public static final String STATUS_PENDING_SELF_RESOLVE = "PENDING_SELF_RESOLVE";
    public static final String STATUS_ESCALATION_REQUESTED = "ESCALATION_REQUESTED";
    public static final String STATUS_STAFF_REVIEWING = "STAFF_REVIEWING";
    public static final String STATUS_STAFF_DECIDED = "STAFF_DECIDED";
    public static final String STATUS_INTERVENTION_REJECTED = "INTERVENTION_REJECTED";
    public static final String STATUS_RESOLVED = "RESOLVED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String RESOLUTION_BUSINESS_APPROVED_AFTER_SELF_RESOLVE = "BUSINESS_APPROVED_AFTER_SELF_RESOLVE";
    public static final String RESOLUTION_STAFF_DECISION_SETTLEMENT = "STAFF_DECISION_SETTLEMENT";
    public static final String RESOLUTION_CANCELLED_BY_INITIATOR = "CANCELLED_BY_INITIATOR";
    public static final String RESOLUTION_CANCELLED_BY_ADMIN = "CANCELLED_BY_ADMIN";
    // Note: Loai khoi tao tranh chap (spec 7.5). BUSINESS_REJECTED_DELIVERABLE cho luong Business tu choi; cac EXPERT_* cho Expert khoi tao.
    public static final String INITIATION_BUSINESS_REJECTED_DELIVERABLE = "BUSINESS_REJECTED_DELIVERABLE";
    public static final String INITIATION_EXPERT_SCOPE_CONCERN = "EXPERT_SCOPE_CONCERN";
    public static final String INITIATION_EXPERT_NO_REVIEW_RESPONSE = "EXPERT_NO_REVIEW_RESPONSE";
    public static final String INITIATION_EXPERT_BAD_FAITH_REJECTION = "EXPERT_BAD_FAITH_REJECTION";
    public static final String INITIATION_OTHER = "OTHER";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "dispute_id") private Integer disputeId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "contract_id", nullable = false) private Integer contractId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "milestone_id") private Integer milestoneId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "assigned_staff_id") private Integer assignedStaffId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "evidence_report") private String evidenceReport;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "proposed_action", length = 100) private String proposedAction;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "admin_approved_by") private Integer adminApprovedBy;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "status", nullable = false, length = 50) private String status;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Column(name = "initiated_by", length = 20) private String initiatedBy;
    @Column(name = "escalation_reason") private String escalationReason;
    @Column(name = "escalation_evidence_file", length = 255) private String escalationEvidenceFile;
    @Column(name = "staff_decision_percentage") private Integer staffDecisionPercentage;
    @Column(name = "staff_decision_note") private String staffDecisionNote;
    @Column(name = "previous_milestone_status", length = 50) private String previousMilestoneStatus;
    @Column(name = "resolution_type", length = 50) private String resolutionType;
    @Column(name = "resolved_at") private LocalDateTime resolvedAt;
    @Column(name = "cancelled_at") private LocalDateTime cancelledAt;
    // Note: Cac cot audit/settlement bo sung theo spec 13.3.4 (migration V50).
    @Column(name = "initiated_by_account_id") private Integer initiatedByAccountId;
    @Column(name = "initiation_type", length = 80) private String initiationType;
    @Column(name = "escalation_requested_by_account_id") private Integer escalationRequestedByAccountId;
    @Column(name = "escalation_requested_at") private LocalDateTime escalationRequestedAt;
    @Column(name = "staff_review_started_at") private LocalDateTime staffReviewStartedAt;
    @Column(name = "staff_decided_at") private LocalDateTime staffDecidedAt;
    @Column(name = "intervention_rejected_at") private LocalDateTime interventionRejectedAt;
    @Column(name = "intervention_rejection_reason") private String interventionRejectionReason;
    @Column(name = "staff_report") private String staffReport;
    @Column(name = "staff_proposed_expert_amount", precision = 19, scale = 2) private BigDecimal staffProposedExpertAmount;
    @Column(name = "business_refund_amount", precision = 19, scale = 2) private BigDecimal businessRefundAmount;
    @Column(name = "settlement_executed_at") private LocalDateTime settlementExecutedAt;
    @Column(name = "settlement_wallet_transaction_id") private Long settlementWalletTransactionId;
    @Column(name = "cancelled_by_account_id") private Integer cancelledByAccountId;
    @Column(name = "cancellation_reason") private String cancellationReason;

    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
