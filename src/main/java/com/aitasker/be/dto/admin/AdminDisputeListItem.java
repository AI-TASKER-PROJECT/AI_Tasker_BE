package com.aitasker.be.dto.admin;

import com.aitasker.be.entity.DisputeEntity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminDisputeListItem {
    private Integer disputeId;
    private Integer contractId;
    private Integer milestoneId;
    private String status;
    private String initiatedBy;
    private String initiationType;
    private LocalDateTime createdAt;
    private AssignedStaffSummary assignedStaff;
    private LocalDateTime staffDecidedAt;
    private Integer expertPayoutPercentage;
    private BigDecimal expertPayoutAmount;
    private BigDecimal businessRefundAmount;
    private LocalDateTime settlementExecutedAt;
    private Long settlementWalletTransactionId;

    @Data
    @Builder
    public static class AssignedStaffSummary {
        private Integer staffId;
        private String displayName;
    }

    public static AdminDisputeListItem from(DisputeEntity dispute, String staffDisplayName) {
        AdminDisputeListItemBuilder builder = AdminDisputeListItem.builder()
                .disputeId(dispute.getDisputeId())
                .contractId(dispute.getContractId())
                .milestoneId(dispute.getMilestoneId())
                .status(dispute.getStatus())
                .initiatedBy(dispute.getInitiatedBy())
                .initiationType(dispute.getInitiationType())
                .createdAt(dispute.getCreatedAt())
                .staffDecidedAt(dispute.getStaffDecidedAt());

        if (dispute.getAssignedStaffId() != null && staffDisplayName != null) {
            builder.assignedStaff(AssignedStaffSummary.builder()
                    .staffId(dispute.getAssignedStaffId())
                    .displayName(staffDisplayName)
                    .build());
        }

        if (dispute.getStaffDecisionPercentage() != null) {
            builder.expertPayoutPercentage(dispute.getStaffDecisionPercentage())
                    .expertPayoutAmount(dispute.getStaffProposedExpertAmount())
                    .businessRefundAmount(dispute.getBusinessRefundAmount())
                    .settlementExecutedAt(dispute.getSettlementExecutedAt())
                    .settlementWalletTransactionId(dispute.getSettlementWalletTransactionId());
        }

        return builder.build();
    }
}
