package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.ForbiddenException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.dto.admin.*;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDisputeDashboardService {

    private final AccessService accessService;
    private final DisputeRepository disputeRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final CaseAttachmentRepository caseAttachmentRepository;
    private final ContractRepository contractRepository;
    private final ContractMilestoneRepository contractMilestoneRepository;
    private final MilestoneRepository milestoneRepository;
    private final AccountRepository accountRepository;
    private final StaffRepository staffRepository;

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private static final Set<String> VALID_STATUSES = Set.of(
            DisputeEntity.STATUS_PENDING_SELF_RESOLVE,
            DisputeEntity.STATUS_ESCALATION_REQUESTED,
            DisputeEntity.STATUS_STAFF_REVIEWING,
            DisputeEntity.STATUS_STAFF_DECIDED,
            DisputeEntity.STATUS_RESOLVED,
            DisputeEntity.STATUS_CANCELLED
    );

    @Transactional(readOnly = true)
    public AdminDisputeListResponse listDisputes(AdminDisputeFilter filter) {
        accessService.requireRole("ADMIN");

        int page;
        if (filter.getPage() == null) {
            page = 0;
        } else if (filter.getPage() < 0) {
            throw new AppException("PAGE KHONG DUOC AM");
        } else {
            page = filter.getPage();
        }

        int size;
        if (filter.getSize() == null) {
            size = DEFAULT_PAGE_SIZE;
        } else if (filter.getSize() < 1 || filter.getSize() > MAX_PAGE_SIZE) {
            throw new AppException("SIZE PHAI NAM TRONG KHOANG 1 DEN 100");
        } else {
            size = filter.getSize();
        }

        if (filter.getStatus() != null && !VALID_STATUSES.contains(filter.getStatus())) {
            throw new AppException("DISPUTE STATUS KHONG HOP LE");
        }
        if (filter.getFrom() != null && filter.getTo() != null && filter.getFrom().isAfter(filter.getTo())) {
            throw new AppException("FROM KHONG DUOC LON HON TO");
        }
        if (filter.getQ() != null && filter.getQ().length() > 100) {
            throw new AppException("Q KHONG DUOC VUOT QUA 100 KY TU");
        }

        List<DisputeEntity> allDisputes = disputeRepository.findAllByOrderByCreatedAtDesc();

        List<DisputeEntity> filtered = allDisputes.stream()
                .filter(d -> filter.getStatus() == null || filter.getStatus().equals(d.getStatus()))
                .filter(d -> filter.getAssignedStaffId() == null
                        || Objects.equals(filter.getAssignedStaffId(), d.getAssignedStaffId()))
                .filter(d -> filter.getFrom() == null
                        || (d.getCreatedAt() != null && !d.getCreatedAt().isBefore(filter.getFrom())))
                .filter(d -> filter.getTo() == null
                        || (d.getCreatedAt() != null && !d.getCreatedAt().isAfter(filter.getTo())))
                .filter(d -> {
                    if (filter.getQ() == null || filter.getQ().isBlank()) return true;
                    String q = filter.getQ().trim().toLowerCase();
                    return String.valueOf(d.getDisputeId()).contains(q)
                            || String.valueOf(d.getContractId()).contains(q);
                })
                .toList();

        long totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int offset = page * size;

        List<AdminDisputeListItem> content = filtered.stream()
                .skip(offset)
                .limit(size)
                .map(d -> AdminDisputeListItem.from(d, resolveStaffDisplayName(d.getAssignedStaffId())))
                .toList();

        return AdminDisputeListResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminDisputeDetail getDisputeDetail(Integer disputeId) {
        accessService.requireRole("ADMIN");

        DisputeEntity dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));

        AdminDisputeDetail.AdminDisputeDetailBuilder builder = AdminDisputeDetail.builder()
                .disputeId(dispute.getDisputeId())
                .contractId(dispute.getContractId())
                .milestoneId(dispute.getMilestoneId())
                .status(dispute.getStatus())
                .initiatedBy(dispute.getInitiatedBy())
                .initiationType(dispute.getInitiationType())
                .createdAt(dispute.getCreatedAt())
                .escalationReason(dispute.getEscalationReason())
                .evidenceReport(dispute.getEvidenceReport())
                .escalationEvidenceFile(dispute.getEscalationEvidenceFile())
                .escalatedAt(dispute.getEscalationRequestedAt())
                .staffReviewStartedAt(dispute.getStaffReviewStartedAt())
                .staffDecidedAt(dispute.getStaffDecidedAt())
                .staffDecisionNote(dispute.getStaffDecisionNote())
                .staffReport(dispute.getStaffReport())
                .resolvedAt(dispute.getResolvedAt())
                .resolutionType(dispute.getResolutionType());

        if (dispute.getAssignedStaffId() != null) {
            String staffName = resolveStaffDisplayName(dispute.getAssignedStaffId());
            if (staffName != null) {
                builder.assignedStaff(AdminDisputeListItem.AssignedStaffSummary.builder()
                        .staffId(dispute.getAssignedStaffId())
                        .displayName(staffName)
                        .build());
            }
        }

        if (dispute.getStaffDecisionPercentage() != null) {
            builder.expertPayoutPercentage(dispute.getStaffDecisionPercentage())
                    .expertPayoutAmount(dispute.getStaffProposedExpertAmount())
                    .businessRefundAmount(dispute.getBusinessRefundAmount())
                    .settlementExecutedAt(dispute.getSettlementExecutedAt())
                    .settlementWalletTransactionId(dispute.getSettlementWalletTransactionId());
        }

        contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(dispute.getContractId()).stream()
                .filter(cm -> dispute.getMilestoneId() != null && dispute.getMilestoneId().equals(cm.getJobMilestoneId()))
                .findFirst()
                .ifPresent(cm -> {
                    builder.milestoneEscrowAmount(cm.getFinalBudget())
                            .settlementSourceType(cm.getSettlementSourceType())
                            .settlementSourceId(cm.getSettlementSourceId());
                });

        List<AdminDisputeDetail.AttachmentEntry> attachments = caseAttachmentRepository
                .findByOwnerTypeAndOwnerIdOrderByCreatedAtAsc(CaseAttachmentEntity.OWNER_DISPUTE, disputeId.longValue())
                .stream()
                .map(a -> AdminDisputeDetail.AttachmentEntry.builder()
                        .fileName(a.getFileName())
                        .fileUrl(a.getFileUrl())
                        .fileType(a.getFileType())
                        .note(a.getNote())
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();
        builder.attachments(attachments);

        List<AdminDisputeDetail.WalletTransactionLedgerEntry> ledger = walletTransactionRepository
                .findByReferenceTypeAndReferenceIdOrderByCreatedAtAsc("DISPUTE", disputeId.longValue())
                .stream()
                .map(tx -> AdminDisputeDetail.WalletTransactionLedgerEntry.builder()
                        .transactionId(tx.getId())
                        .transactionType(tx.getTransactionType())
                        .direction(tx.getDirection())
                        .balanceType(tx.getBalanceType())
                        .amount(tx.getAmount())
                        .balanceBefore(tx.getBalanceBefore())
                        .balanceAfter(tx.getBalanceAfter())
                        .description(tx.getDescription())
                        .createdAt(tx.getCreatedAt())
                        .build())
                .toList();
        builder.settlementLedger(ledger);

        return builder.build();
    }

    private String resolveStaffDisplayName(Integer staffId) {
        if (staffId == null) return null;
        return staffRepository.findById(staffId)
                .flatMap(staff -> accountRepository.findById(staff.getAccountId()))
                .map(AccountEntity::getFullName)
                .orElse(null);
    }
}
