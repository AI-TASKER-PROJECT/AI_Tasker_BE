package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "termination_requests")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TerminationRequestEntity {
    public static final String STATUS_REQUESTED = "REQUESTED";
    public static final String STATUS_STAFF_REVIEWING = "STAFF_REVIEWING";
    public static final String STATUS_STAFF_APPROVED = "STAFF_APPROVED";
    public static final String STATUS_STAFF_REJECTED = "STAFF_REJECTED";
    public static final String STATUS_AWAITING_SETTLEMENT_EXECUTION = "AWAITING_SETTLEMENT_EXECUTION";
    public static final String STATUS_AWAITING_DEPOSIT_REFUND = "AWAITING_DEPOSIT_REFUND";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "termination_request_id")
    private Long terminationRequestId;

    @Column(name = "contract_id", nullable = false)
    private Integer contractId;
    @Column(name = "current_milestone_id")
    private Integer currentMilestoneId;
    @Column(name = "requested_by_account_id", nullable = false)
    private Integer requestedByAccountId;
    @Column(name = "requested_by_role", nullable = false, length = 20)
    private String requestedByRole;
    @Column(name = "request_reason", nullable = false)
    private String requestReason;
    @Column(name = "request_file_url")
    private String requestFileUrl;
    @Column(name = "assigned_staff_id")
    private Integer assignedStaffId;
    @Column(name = "status", nullable = false, length = 50)
    private String status;
    @Column(name = "staff_review_started_at")
    private LocalDateTime staffReviewStartedAt;
    @Column(name = "staff_decided_at")
    private LocalDateTime staffDecidedAt;
    @Column(name = "staff_decision_reason")
    private String staffDecisionReason;
    @Column(name = "staff_report")
    private String staffReport;
    @Column(name = "expert_payout_percentage", precision = 5, scale = 2)
    private BigDecimal expertPayoutPercentage;
    @Column(name = "expert_payout_amount", precision = 19, scale = 2)
    private BigDecimal expertPayoutAmount;
    @Column(name = "business_refund_amount", precision = 19, scale = 2)
    private BigDecimal businessRefundAmount;
    @Column(name = "partial_evidence_required", nullable = false)
    private Boolean partialEvidenceRequired;
    @Column(name = "partial_evidence_submitted_at")
    private LocalDateTime partialEvidenceSubmittedAt;
    @Column(name = "partial_evidence_url")
    private String partialEvidenceUrl;
    @Column(name = "partial_evidence_note")
    private String partialEvidenceNote;
    @Column(name = "settlement_executed_at")
    private LocalDateTime settlementExecutedAt;
    @Column(name = "settlement_wallet_transaction_id")
    private Long settlementWalletTransactionId;
    @Column(name = "deposit_refund_required", nullable = false)
    private Boolean depositRefundRequired;
    @Column(name = "deposit_refunded_at")
    private LocalDateTime depositRefundedAt;
    @Column(name = "deposit_refund_transaction_id")
    private Long depositRefundTransactionId;
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    @Column(name = "cancelled_by_account_id")
    private Integer cancelledByAccountId;
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
