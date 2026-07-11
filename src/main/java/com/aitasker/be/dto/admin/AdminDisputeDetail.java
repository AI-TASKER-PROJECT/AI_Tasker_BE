package com.aitasker.be.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminDisputeDetail {
    private Integer disputeId;
    private Integer contractId;
    private Integer milestoneId;
    private String status;
    private String initiatedBy;
    private String initiationType;
    private LocalDateTime createdAt;

    private String escalationReason;
    private String evidenceReport;
    private String escalationEvidenceFile;

    private AdminDisputeListItem.AssignedStaffSummary assignedStaff;
    private LocalDateTime escalatedAt;
    private LocalDateTime staffReviewStartedAt;
    private LocalDateTime staffDecidedAt;
    private Integer expertPayoutPercentage;
    private BigDecimal expertPayoutAmount;
    private BigDecimal businessRefundAmount;
    private String staffDecisionNote;
    private String staffReport;
    private LocalDateTime resolvedAt;
    private LocalDateTime settlementExecutedAt;
    private Long settlementWalletTransactionId;
    private String resolutionType;

    private BigDecimal milestoneEscrowAmount;
    private String settlementSourceType;
    private Long settlementSourceId;

    private List<AttachmentEntry> attachments;
    private List<WalletTransactionLedgerEntry> settlementLedger;

    @Data
    @Builder
    public static class AttachmentEntry {
        private String fileName;
        private String fileUrl;
        private String fileType;
        private String note;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class WalletTransactionLedgerEntry {
        private Long transactionId;
        private String transactionType;
        private String direction;
        private String balanceType;
        private BigDecimal amount;
        private BigDecimal balanceBefore;
        private BigDecimal balanceAfter;
        private String description;
        private LocalDateTime createdAt;
    }
}
