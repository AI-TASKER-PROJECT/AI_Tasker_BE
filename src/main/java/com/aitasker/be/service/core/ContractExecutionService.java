/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/ContractExecutionService.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.dto.core.AcceptanceCriteriaRequest;
import com.aitasker.be.dto.core.ContractMilestoneViewResponse;
import com.aitasker.be.dto.core.AcceptDisputeSelfResolveAgreementRequest;
import com.aitasker.be.dto.core.CreateDisputeSelfResolveReplyRequest;
import com.aitasker.be.dto.core.DisputeSelfResolveReplyResponse;
import com.aitasker.be.dto.core.ImmediateTerminationRequest;
import com.aitasker.be.dto.core.ProgressReportFeedbackRequest;
import com.aitasker.be.dto.core.ProgressReportRequest;
import com.aitasker.be.dto.core.StaffAssignmentCandidateResponse;
import com.aitasker.be.dto.core.StaffDisputeFilter;
import com.aitasker.be.dto.core.StaffDisputeListItem;
import com.aitasker.be.dto.core.StaffDisputeListResponse;
import com.aitasker.be.dto.payment.DepositRefundRequest;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import com.aitasker.be.event.DisputeSettlementCompletedEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Note: Annotation này cho Spring quản lý class như một service chứa nghiệp vụ.
@Service
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class ContractExecutionService {
    private final AccessService accessService;
    private final AccountRepository accountRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final ProposalRepository proposalRepository;
    private final JobRepository jobRepository;
    private final ContractRepository contractRepository;
    private final ContractMilestoneRepository contractMilestoneRepository;
    private final MilestoneRepository milestoneRepository;
    private final AcceptanceCriteriaRepository criteriaRepository;
    private final DeliverableRepository deliverableRepository;
    private final TransactionRepository transactionRepository;
    private final DisputeRepository disputeRepository;
    private final DisputeSelfResolveReplyRepository disputeSelfResolveReplyRepository;
    private final TerminationRequestRepository terminationRequestRepository;
    private final CaseAttachmentRepository caseAttachmentRepository;
    private final MilestoneProgressReportRepository milestoneProgressReportRepository;
    private final MilestoneProgressReportRequestRepository milestoneProgressReportRequestRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final StaffRepository staffRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final SystemWalletService systemWalletService;
    private final WalletLedgerService walletLedgerService;
    private final PaymentWalletService paymentWalletService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final JobDomainRepository jobDomainRepository;
    private final JobSkillRepository jobSkillRepository;
    private final StaffDomainRepository staffDomainRepository;
    private final StaffSkillRepository staffSkillRepository;
    private final DomainRepository domainRepository;
    private final SkillRepository skillRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `createDraftFromProposal` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public ContractEntity createDraftFromProposal(Integer proposalId, ContractEntity input) {
        accessService.requireRole("BUSINESS");
        accessService.requireApprovedAccount();
        if (input == null) input = new ContractEntity();
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer businessId = businessProfileRepository.findByAccountId(accountId).map(BusinessProfileEntity::getBusinessId).orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
        ProposalEntity proposal = proposalRepository.findById(proposalId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY PROPOSAL"));
        if (!"Accepted".equalsIgnoreCase(proposal.getStatus())) throw new AppException("CHI DUOC TAO CONTRACT TU PROPOSAL DA ACCEPTED");
        if (contractRepository.existsByProposalId(proposalId)) throw new AppException("PROPOSAL DA DUOC TAO CONTRACT");
        JobEntity job = proposalRepository.findById(proposalId)
                .flatMap(p -> Optional.ofNullable(p.getJobId()).flatMap(jobId -> jobRepository.findById(jobId)))
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB CUA PROPOSAL"));
        // DAM BAO DOANH NGHIEP CHI DUOC TAO CONTRACT TU JOB CUA CHINH MINH.
        if (!businessId.equals(job.getBusinessId())) throw new AppException("BAN KHONG CO QUYEN TAO CONTRACT CHO JOB NAY");
        List<MilestoneEntity> jobMilestones = milestoneRepository.findByJobIdOrderByOrderIndexAsc(job.getJobId());
        if (jobMilestones.isEmpty()) throw new AppException("JOB CHUA CO MILESTONE DE TAO CONTRACT");
        Map<Integer, java.math.BigDecimal> proposedBudgetByMilestone = parseProposalMilestoneBudget(proposal.getProposalMilestone());
        java.math.BigDecimal totalBudget = calculateContractTotalBudget(jobMilestones, proposedBudgetByMilestone);
        Integer timelineDays = input.getTimelineDays() != null && input.getTimelineDays() > 0
                ? input.getTimelineDays()
                : calculateTimelineDays(job);
        input.setContractId(null);
        input.setJobId(proposal.getJobId());
        input.setProposalId(proposalId);
        input.setExpertId(proposal.getExpertId());
        input.setBusinessId(businessId);
        input.setContractTitle(input.getContractTitle() == null || input.getContractTitle().isBlank() ? job.getTitle() : input.getContractTitle().trim());
        input.setTotalBudget(totalBudget);
        input.setTimelineDays(timelineDays);
        input.setStatus("DRAFT");
        input.setBusinessAcceptedAt(null);
        input.setExpertAcceptedAt(null);
        input.setBusinessNdaSignedAt(null);
        input.setExpertNdaSignedAt(null);
        input.setActivatedAt(null);
        ContractEntity saved = contractRepository.save(input);
        createContractMilestones(saved.getContractId(), jobMilestones, proposedBudgetByMilestone);
        saved.setContractMilestones(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(saved.getContractId()));
        auditLogService.record(AuditLogService.ACTION_CREATE_CONTRACT_DRAFT, "contracts", String.valueOf(saved.getContractId()), accountId);
        expertProfileRepository.findById(saved.getExpertId())
                .ifPresent(expert -> notificationService.notifyContractEvent(
                        expert.getAccountId(),
                        accountId,
                        "CONTRACT_CREATED",
                        "Hợp đồng nháp mới",
                        "Doanh nghiệp đã tạo hợp đồng nháp để bạn xem xét.",
                        saved.getContractId()
                ));
        return saved;
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `signContract` ghi nhận một bên đã ký/xác nhận điều khoản hợp đồng.
    public ContractEntity signContract(Integer contractId) {
        requireApprovedForBusinessOrExpert();
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer businessId = businessProfileRepository.findByAccountId(accountId).map(BusinessProfileEntity::getBusinessId).orElse(null);
        Integer expertId = expertProfileRepository.findByAccountId(accountId).map(ExpertProfileEntity::getExpertId).orElse(null);
        boolean isParticipant = (businessId != null && businessId.equals(contract.getBusinessId()))
                || (expertId != null && expertId.equals(contract.getExpertId()));
        if (!isParticipant) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        if (List.of("PENDING", "ACTIVE").contains(contract.getStatus())) return contract;
        if (!"DRAFT".equals(contract.getStatus())) throw new AppException("CONTRACT KHONG O TRANG THAI CHO PHEP KY");
        LocalDateTime now = LocalDateTime.now();
        if (businessId != null && businessId.equals(contract.getBusinessId())) contract.setBusinessAcceptedAt(now);
        if (expertId != null && expertId.equals(contract.getExpertId())) contract.setExpertAcceptedAt(now);
        tryActivateContract(contract, now);
        ContractEntity saved = contractRepository.save(contract);
        saved.setContractMilestones(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(saved.getContractId()));
        auditLogService.record(AuditLogService.ACTION_ACCEPT_CONTRACT, "contracts", String.valueOf(contractId), accountId);
        notifyCounterpartyContractEvent(saved, accountId,
                businessId != null && businessId.equals(saved.getBusinessId()) ? saved.getExpertId() : null,
                expertId != null && expertId.equals(saved.getExpertId()) ? saved.getBusinessId() : null,
                "CONTRACT_ACCEPTED",
                "Hợp đồng đã được xác nhận",
                "Bên còn lại đã ký xác nhận hợp đồng.");
        if ("PENDING".equals(saved.getStatus())) {
            notifyBothParticipants(saved, accountId, "CONTRACT_PENDING_DEPOSIT", "Hợp đồng chờ ký quỹ", "Hợp đồng đã đủ chữ ký Contract và NDA, doanh nghiệp cần thanh toán ký quỹ để bắt đầu dự án.");
        }
        return saved;
    }

    // Note: Hàm `parseProposalMilestoneBudget` đọc JSON ngân sách milestone do expert đề xuất trong proposal.
    private Map<Integer, BigDecimal> parseProposalMilestoneBudget(String proposalMilestone) {
        Map<Integer, BigDecimal> result = new HashMap<>();
        if (proposalMilestone == null || proposalMilestone.isBlank()) return result;
        try {
            JsonNode root = objectMapper.readTree(proposalMilestone);
            if (!root.isArray()) throw new AppException("PROPOSAL MILESTONE KHONG HOP LE");
            for (JsonNode item : root) {
                JsonNode milestoneIdNode = item.get("milestoneId");
                JsonNode proposedBudgetNode = item.get("proposedBudget");
                if (milestoneIdNode == null || !milestoneIdNode.canConvertToInt() || proposedBudgetNode == null || !proposedBudgetNode.isNumber()) {
                    throw new AppException("PROPOSAL MILESTONE KHONG HOP LE");
                }
                result.put(milestoneIdNode.asInt(), proposedBudgetNode.decimalValue());
            }
            return result;
        } catch (Exception ex) {
            if (ex instanceof AppException appException) throw appException;
            throw new AppException("PROPOSAL MILESTONE KHONG PHAI JSON HOP LE");
        }
    }

    // Note: Hàm `calculateContractTotalBudget` tính tổng ngân sách chốt của hợp đồng từ milestone job và đề xuất trong proposal.
    private BigDecimal calculateContractTotalBudget(List<MilestoneEntity> jobMilestones, Map<Integer, BigDecimal> proposedBudgetByMilestone) {
        BigDecimal total = BigDecimal.ZERO;
        for (MilestoneEntity milestone : jobMilestones) {
            total = total.add(proposedBudgetByMilestone.getOrDefault(milestone.getMilestoneId(), milestone.getFundsAllocated()));
        }
        if (total.signum() <= 0) throw new AppException("TOTAL BUDGET PHAI LON HON 0");
        return total;
    }

    // Note: Hàm `calculateTimelineDays` quy đổi thời lượng job sang ngày để lưu vào contract.
    private Integer calculateTimelineDays(JobEntity job) {
        Integer value = job.getPlannedDurationValue();
        if (value == null || value <= 0) return 1;
        String unit = job.getPlannedDurationUnit() == null ? "DAY" : job.getPlannedDurationUnit().trim().toUpperCase();
        return switch (unit) {
            case "WEEK", "WEEKS" -> value * 7;
            case "MONTH", "MONTHS" -> value * 30;
            default -> value;
        };
    }

    // Note: Hàm `createContractMilestones` lưu từng milestone chốt của contract, gồm ngân sách gốc và ngân sách cuối.
    private void createContractMilestones(Integer contractId, List<MilestoneEntity> jobMilestones, Map<Integer, BigDecimal> proposedBudgetByMilestone) {
        contractMilestoneRepository.deleteByContractId(contractId);
        for (MilestoneEntity milestone : jobMilestones) {
            BigDecimal finalBudget = proposedBudgetByMilestone.getOrDefault(milestone.getMilestoneId(), milestone.getFundsAllocated());
            String normalizedUnit = milestone.getDurationUnit() != null ? milestone.getDurationUnit().trim().toUpperCase() : null;
            String criteriaSnapshot = buildCriteriaSnapshot(milestone.getMilestoneId());
            contractMilestoneRepository.save(ContractMilestoneEntity.builder()
                    .contractId(contractId)
                    .jobMilestoneId(milestone.getMilestoneId())
                    .milestoneName(milestone.getMilestoneName())
                    .description(milestone.getDescription())
                    .originalBudget(milestone.getFundsAllocated())
                    .finalBudget(finalBudget)
                    .orderIndex(milestone.getOrderIndex())
                    .duration(milestone.getDuration())
                    .durationUnit(normalizedUnit)
                    .criteriaSnapshot(criteriaSnapshot)
                    .deliverableExpectation(milestone.getDescription())
                    .status("PENDING")
                    .resubmitCount(0)
                    .rejectCount(0)
                    .build());
        }
    }

    // Note: Hàm `buildCriteriaSnapshot` dựng văn bản tiêu chí nghiệm thu tại thời điểm tạo contract để snapshot không bị thay đổi sau này.
    private String buildCriteriaSnapshot(Integer milestoneId) {
        List<AcceptanceCriteriaEntity> criteria = criteriaRepository
                .findByMilestoneIdOrderBySortOrderAscCriteriaIdAsc(milestoneId);
        if (criteria == null || criteria.isEmpty()) return null;
        return criteria.stream()
                .map(AcceptanceCriteriaEntity::getDescription)
                .filter(description -> description != null && !description.isBlank())
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    // Note: Hàm `applyContractMilestoneBudgets` cập nhật ngân sách chốt từ contract_milestones về milestone thật khi contract active.
    private void applyContractMilestoneBudgets(ContractEntity contract) {
        List<ContractMilestoneEntity> contractMilestones = contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contract.getContractId());
        for (ContractMilestoneEntity contractMilestone : contractMilestones) {
            MilestoneEntity milestone = milestoneRepository.findById(contractMilestone.getJobMilestoneId())
                    .orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE CUA CONTRACT"));
            if (!contract.getJobId().equals(milestone.getJobId())) {
                throw new AppException("MILESTONE KHONG THUOC JOB CUA CONTRACT");
            }
            milestone.setContractId(contract.getContractId());
            milestone.setFundsAllocated(contractMilestone.getFinalBudget());
            milestoneRepository.save(milestone);
        }
    }

    private void markContractJobInProgress(ContractEntity contract) {
        JobEntity job = jobRepository.findById(contract.getJobId())
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB CUA CONTRACT"));
        job.setBudget(contract.getTotalBudget());
        job.setStatus("IN_PROGRESS");
        jobRepository.save(job);
    }

    private void closeCompletedContractJob(ContractEntity contract) {
        JobEntity job = jobRepository.findById(contract.getJobId())
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB CUA CONTRACT"));
        job.setStatus("CLOSED");
        jobRepository.save(job);
    }

    // Note: Hàm `tryActivateContract` dua contract sang PENDING khi du chu ky va NDA.
    private void tryActivateContract(ContractEntity contract, LocalDateTime now) {
        boolean readyToActivate = contract.getBusinessAcceptedAt() != null
                && contract.getExpertAcceptedAt() != null
                && contract.getBusinessNdaSignedAt() != null
                && contract.getExpertNdaSignedAt() != null;
        if (readyToActivate) {
            contract.setStatus("PENDING");
        } else {
            contract.setStatus("DRAFT");
        }
        contract.setUpdatedAt(now);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `signNda` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public ContractEntity signNda(Integer contractId) {
        // BUSINESS VA EXPERT DEU PHAI XAC NHAN NDA TRUOC KHI CONTRACT ACTIVE.
        accessService.requireRole("BUSINESS", "EXPERT");
        accessService.requireApprovedAccount();
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer businessId = businessProfileRepository.findByAccountId(accountId).map(BusinessProfileEntity::getBusinessId).orElse(null);
        Integer expertId = expertProfileRepository.findByAccountId(accountId).map(ExpertProfileEntity::getExpertId).orElse(null);
        boolean isParticipant = (businessId != null && businessId.equals(contract.getBusinessId()))
                || (expertId != null && expertId.equals(contract.getExpertId()));
        if (!isParticipant) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        if (List.of("PENDING", "ACTIVE").contains(contract.getStatus())) return contract;
        if (!"DRAFT".equals(contract.getStatus())) throw new AppException("CONTRACT KHONG O TRANG THAI CHO PHEP KY NDA");
        LocalDateTime now = LocalDateTime.now();
        if (businessId != null && businessId.equals(contract.getBusinessId())) contract.setBusinessNdaSignedAt(now);
        if (expertId != null && expertId.equals(contract.getExpertId())) contract.setExpertNdaSignedAt(now);
        tryActivateContract(contract, now);
        ContractEntity saved = contractRepository.save(contract);
        saved.setContractMilestones(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(saved.getContractId()));
        auditLogService.record(AuditLogService.ACTION_SIGN_NDA, "contracts", String.valueOf(contractId), accountId);
        notifyCounterpartyContractEvent(saved, accountId,
                businessId != null && businessId.equals(saved.getBusinessId()) ? saved.getExpertId() : null,
                expertId != null && expertId.equals(saved.getExpertId()) ? saved.getBusinessId() : null,
                "NDA_SIGNED",
                "NDA đã được ký",
                "Bên còn lại đã ký NDA cho hợp đồng.");
        if ("PENDING".equals(saved.getStatus())) {
            notifyBothParticipants(saved, accountId, "CONTRACT_PENDING_DEPOSIT", "Hợp đồng chờ ký quỹ", "Hợp đồng đã đủ chữ ký Contract và NDA, doanh nghiệp cần thanh toán ký quỹ để bắt đầu dự án.");
        }
        return saved;
    }

    @Transactional
    public ContractEntity rejectContract(Integer contractId) {
        accessService.requireRole("EXPERT");
        accessService.requireApprovedAccount();
        ContractEntity contract = requireExpertOwnedContract(contractId);
        if (!List.of("DRAFT", "PENDING").contains(contract.getStatus())) {
            throw new AppException("CONTRACT KHONG O TRANG THAI CHO PHEP TU CHOI");
        }
        contract.setStatus("CANCELLED");
        contract.setUpdatedAt(LocalDateTime.now());
        JobEntity job = jobRepository.findById(contract.getJobId())
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB CUA CONTRACT"));
        job.setStatus("OPEN");
        jobRepository.save(job);
        ContractEntity saved = contractRepository.save(contract);
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record(AuditLogService.ACTION_REJECT_CONTRACT, "contracts", String.valueOf(contractId), actorAccountId);
        businessProfileRepository.findById(saved.getBusinessId())
                .ifPresent(business -> notificationService.notifyContractEvent(
                        business.getAccountId(),
                        actorAccountId,
                        "CONTRACT_REJECTED",
                        "Hợp đồng bị từ chối",
                        "Chuyên gia đã từ chối hợp đồng nháp. Job được chuyển về bước review proposal.",
                        saved.getContractId()
                ));
        return saved;
    }

    @Transactional
    public ContractEntity cancelDraftContract(Integer contractId) {
        accessService.requireRole("BUSINESS");
        accessService.requireApprovedAccount();
        ContractEntity contract = requireBusinessOwnedContract(contractId);
        boolean untouchedDraft = ContractEntity.STATUS_DRAFT.equals(contract.getStatus())
                && contract.getBusinessAcceptedAt() == null
                && contract.getExpertAcceptedAt() == null
                && contract.getBusinessNdaSignedAt() == null
                && contract.getExpertNdaSignedAt() == null;
        if (!untouchedDraft) throw new AppException("CONTRACT_DRAFT_CANCELLATION_NOT_ALLOWED");

        contract.setStatus(ContractEntity.STATUS_CANCELLED);
        contract.setUpdatedAt(LocalDateTime.now());
        JobEntity job = jobRepository.findById(contract.getJobId())
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB CUA CONTRACT"));
        job.setStatus("OPEN");
        jobRepository.save(job);
        ContractEntity saved = contractRepository.save(contract);
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record("CONTRACT_DRAFT_CANCELLED_BY_BUSINESS", "contracts", String.valueOf(contractId), actorAccountId);
        expertProfileRepository.findById(saved.getExpertId()).ifPresent(expert ->
                notificationService.notifyContractEvent(expert.getAccountId(), actorAccountId,
                        "CONTRACT_DRAFT_CANCELLED", "Hợp đồng nháp đã bị hủy",
                        "Doanh nghiệp đã hủy hợp đồng nháp trước khi hai bên ký.", contractId));
        return saved;
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    public ContractEntity requestTermination(Integer contractId, String reason) {
        requestTerminationRequest(contractId, TerminationRequestEntity.builder().requestReason(reason).build());
        return contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
    }

    @Transactional
    public TerminationRequestEntity requestTerminationRequest(Integer contractId, TerminationRequestEntity request) {
        accessService.requireRole("BUSINESS", "EXPERT");
        accessService.requireApprovedAccount();
        String reason = request == null ? null : request.getRequestReason();
        if (reason == null || reason.isBlank()) throw new AppException("LY DO CHAM DUT KHONG DUOC DE TRONG");
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        AccountEntity actor = accessService.currentAccount();
        String role = actor.getRole().getRoleName();
        requireParticipant(contract, actor);
        if (!ContractEntity.STATUS_ACTIVE.equals(contract.getStatus())) {
            throw new AppException("CONTRACT KHONG O TRANG THAI CHO PHEP YEU CAU CHAM DUT");
        }
        if (!activeTerminationRequests(contractId).isEmpty()) {
            throw new AppException("CONTRACT DA CO YEU CAU CHAM DUT DANG HOAT DONG");
        }
        Integer currentMilestoneId = request == null ? null : request.getCurrentMilestoneId();
        if (currentMilestoneId == null) currentMilestoneId = currentExecutionMilestone(contractId).map(ContractMilestoneEntity::getJobMilestoneId).orElse(null);
        LocalDateTime now = LocalDateTime.now();
        contract.setStatus(ContractEntity.STATUS_TERMINATION_PENDING);
        contract.setTerminationReason(reason.trim());
        contract.setUpdatedAt(now);
        contractRepository.save(contract);
        TerminationRequestEntity saved = terminationRequestRepository.save(TerminationRequestEntity.builder()
                .contractId(contractId)
                .currentMilestoneId(currentMilestoneId)
                .requestedByAccountId(actor.getAccountId())
                .requestedByRole(role)
                .requestReason(reason.trim())
                .requestFileUrl(request == null ? null : request.getRequestFileUrl())
                .status("BUSINESS".equals(role)
                        ? TerminationRequestEntity.STATUS_AWAITING_EXPERT_RESPONSE
                        : TerminationRequestEntity.STATUS_REQUESTED)
                .expertResponseDueAt("BUSINESS".equals(role) ? now.plusDays(3) : null)
                .partialEvidenceRequired(false)
                .depositRefundRequired(true)
                .build());
        auditLogService.record("TERMINATION_REQUESTED", "termination_requests", String.valueOf(saved.getTerminationRequestId()), actor.getAccountId());
        saveAttachmentIfPresent(CaseAttachmentEntity.OWNER_TERMINATION_REQUEST, saved.getTerminationRequestId(), request == null ? null : request.getRequestFileUrl(), null, null, reason);
        if ("BUSINESS".equals(role)) {
            expertProfileRepository.findById(contract.getExpertId()).ifPresent(expert ->
                    notificationService.notifyTerminationRequested(
                            expert.getAccountId(), actor.getAccountId(), contractId, saved.getTerminationRequestId()));
        } else {
            notifyAllAdmins(actor.getAccountId(), (receiver, actorId) ->
                    notificationService.notifyTerminationRequested(receiver, actorId, contractId, saved.getTerminationRequestId()));
        }
        return saved;
    }

    @Transactional
    public ContractEntity executeTermination(Integer contractId, String adminNote) {
        accessService.requireRole("ADMIN");
        AccountEntity actor = accessService.currentAccount();
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        if (!List.of(ContractEntity.STATUS_ACTIVE, ContractEntity.STATUS_TERMINATION_PENDING).contains(contract.getStatus())) {
            throw new AppException("CONTRACT KHONG O TRANG THAI CHO PHEP CHAM DUT");
        }
        if (hasActiveDispute(contractId)) {
            throw new AppException("KHONG THE SETTLE TERMINATION KHI CONTRACT CON DISPUTE DANG HOAT DONG");
        }
        Integer businessAccountId = businessProfileRepository.findById(contract.getBusinessId())
                .map(BusinessProfileEntity::getAccountId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
        List<String> refundableStatuses = List.of(
                ContractMilestoneEntity.STATUS_DEPOSITED,
                ContractMilestoneEntity.STATUS_IN_PROGRESS,
                ContractMilestoneEntity.STATUS_UNDER_REVIEW
        );
        List<ContractMilestoneEntity> contractMilestones = contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contractId);
        for (ContractMilestoneEntity milestone : contractMilestones) {
            if (refundableStatuses.contains(milestone.getStatus())) {
                ensureEscrowNotReleased(milestone);
                walletLedgerService.releaseEscrowToAvailable(
                        businessAccountId,
                        milestone.getFinalBudget(),
                        WalletTransactionEntity.TX_ESCROW_REFUND,
                        "MILESTONE",
                        milestone.getJobMilestoneId().longValue(),
                        "Refund escrow on contract termination"
                );
                markEscrowReleased(milestone, "TERMINATION", contractId.longValue());
            }
            if (List.of(ContractMilestoneEntity.STATUS_COMPLETED, ContractMilestoneEntity.STATUS_CANCELLED).contains(milestone.getStatus())) continue;
            milestone.setStatus(ContractMilestoneEntity.STATUS_CANCELLED);
            contractMilestoneRepository.save(milestone);
            milestoneRepository.findById(milestone.getJobMilestoneId()).ifPresent(jobMilestone -> {
                jobMilestone.setStatus(ContractMilestoneEntity.STATUS_CANCELLED);
                jobMilestone.setUpdatedAt(LocalDateTime.now());
                milestoneRepository.save(jobMilestone);
            });
        }
        LocalDateTime now = LocalDateTime.now();
        contract.setStatus(ContractEntity.STATUS_TERMINATED);
        contract.setTerminationNote(adminNote);
        contract.setTerminatedAt(now);
        contract.setUpdatedAt(now);
        ContractEntity saved = contractRepository.save(contract);
        auditLogService.record(AuditLogService.ACTION_TERMINATE_CONTRACT, "contracts", String.valueOf(contractId), actor.getAccountId());
        paymentWalletService.autoRefundParticipantDeposits(contractId, actor.getAccountId());
        return saved;
    }

    // Note: Hàm `terminateContract` giữ tương thích API cũ, chỉ tạo yêu cầu chấm dứt.
    @Transactional
    public ContractEntity terminateContract(Integer contractId, String reason) {
        return requestTermination(contractId, reason);
    }

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Transactional public MilestoneEntity createMilestone(MilestoneEntity input) {
        accessService.requireRole("BUSINESS");
        accessService.requireApprovedAccount();
        JobEntity job = requireBusinessOwnedJob(input.getJobId());
        if (!List.of("DRAFT", "OPEN").contains(job.getStatus())) throw new AppException("JOB KHONG CHO PHEP TAO MILESTONE");
        if (input.getMilestoneName() == null || input.getMilestoneName().isBlank()) throw new AppException("MILESTONE NAME KHONG DUOC DE TRONG");
        if (input.getFundsAllocated() == null || input.getFundsAllocated().signum() < 0) throw new AppException("FUNDS ALLOCATED KHONG HOP LE");
        if (input.getOrderIndex() == null || input.getOrderIndex() <= 0) throw new AppException("ORDER INDEX PHAI LON HON 0");
        if (milestoneRepository.existsByJobIdAndOrderIndex(input.getJobId(), input.getOrderIndex())) {
            throw new AppException("ORDER INDEX DA TON TAI TRONG JOB");
        }
        validateDuration(input.getDuration(), input.getDurationUnit());
        input.setContractId(null);
        if (input.getStatus() == null) input.setStatus("PENDING");
        if (input.getDurationUnit() != null) input.setDurationUnit(input.getDurationUnit().trim().toUpperCase());
        MilestoneEntity saved = milestoneRepository.save(input);
        replaceMilestoneCriteria(saved.getMilestoneId(), input.getAcceptanceCriteria());
        auditLogService.record(AuditLogService.ACTION_CREATE_MILESTONE, "milestones", String.valueOf(saved.getMilestoneId()), accessService.currentAccount().getAccountId());
        return attachCriteria(saved);
    }

    @Transactional
    public MilestoneEntity updateMilestone(Integer milestoneId, MilestoneEntity input) {
        accessService.requireRole("BUSINESS");
        accessService.requireApprovedAccount();
        MilestoneEntity existing = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        if (existing.getContractId() != null) {
            throw new AppException("KHONG THE SUA MILESTONE DA THUOC CONTRACT");
        }
        requireBusinessOwnedJob(existing.getJobId());
        if (input.getMilestoneName() != null && !input.getMilestoneName().isBlank()) {
            existing.setMilestoneName(input.getMilestoneName());
        }
        if (input.getDescription() != null) {
            existing.setDescription(input.getDescription());
        }
        if (input.getFundsAllocated() != null && input.getFundsAllocated().signum() >= 0) {
            existing.setFundsAllocated(input.getFundsAllocated());
        }
        boolean durationProvided = input.getDuration() != null || (input.getDurationUnit() != null && !input.getDurationUnit().isBlank());
        if (durationProvided) {
            validateDuration(input.getDuration(), input.getDurationUnit());
            existing.setDuration(input.getDuration());
            existing.setDurationUnit(input.getDurationUnit().trim().toUpperCase());
        }
        if (input.getAcceptanceCriteria() != null) {
            if (contractRepository.findByJobId(existing.getJobId()).isPresent()) {
                throw new AppException("JOB DA CO CONTRACT, KHONG DUOC SUA TIEU CHI NGHIEM THU");
            }
        }
        MilestoneEntity saved = milestoneRepository.save(existing);
        if (input.getAcceptanceCriteria() != null) {
            replaceMilestoneCriteria(milestoneId, input.getAcceptanceCriteria());
        }
        auditLogService.record(AuditLogService.ACTION_UPDATE_MILESTONE, "milestones", String.valueOf(milestoneId), accessService.currentAccount().getAccountId());
        return attachCriteria(saved);
    }

    @Transactional
    public AcceptanceCriteriaEntity createCriteria(Integer milestoneId, AcceptanceCriteriaRequest request) {
        MilestoneEntity milestone = requireEditableCriteriaMilestone(milestoneId);
        validateCriteriaRequest(request);
        List<AcceptanceCriteriaEntity> existing = criteriaRepository
                .findByMilestoneIdOrderBySortOrderAscCriteriaIdAsc(milestoneId);
        int sortOrder = request.getSortOrder() == null
                ? existing.stream().map(AcceptanceCriteriaEntity::getSortOrder).filter(java.util.Objects::nonNull)
                .max(Integer::compareTo).orElse(0) + 1
                : request.getSortOrder();
        AcceptanceCriteriaEntity saved = criteriaRepository.save(AcceptanceCriteriaEntity.builder()
                .milestoneId(milestone.getMilestoneId())
                .description(request.getDescription().trim())
                .sortOrder(sortOrder)
                .build());
        auditLogService.record(AuditLogService.ACTION_CREATE_ACCEPTANCE_CRITERIA, "acceptance_criteria",
                String.valueOf(saved.getCriteriaId()), accessService.currentAccount().getAccountId());
        return saved;
    }

    @Transactional
    public AcceptanceCriteriaEntity updateCriteria(
            Integer milestoneId,
            Integer criteriaId,
            AcceptanceCriteriaRequest request
    ) {
        requireEditableCriteriaMilestone(milestoneId);
        validateCriteriaRequest(request);
        AcceptanceCriteriaEntity criteria = requireOwnedCriteria(milestoneId, criteriaId);
        criteria.setDescription(request.getDescription().trim());
        if (request.getSortOrder() != null) criteria.setSortOrder(request.getSortOrder());
        AcceptanceCriteriaEntity saved = criteriaRepository.save(criteria);
        auditLogService.record(AuditLogService.ACTION_UPDATE_ACCEPTANCE_CRITERIA, "acceptance_criteria",
                String.valueOf(saved.getCriteriaId()), accessService.currentAccount().getAccountId());
        return saved;
    }

    @Transactional
    public void deleteCriteria(Integer milestoneId, Integer criteriaId) {
        requireEditableCriteriaMilestone(milestoneId);
        AcceptanceCriteriaEntity criteria = requireOwnedCriteria(milestoneId, criteriaId);
        criteriaRepository.delete(criteria);
        auditLogService.record(AuditLogService.ACTION_DELETE_ACCEPTANCE_CRITERIA, "acceptance_criteria",
                String.valueOf(criteriaId), accessService.currentAccount().getAccountId());
    }
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Transactional public DeliverableEntity submitDeliverable(DeliverableEntity input) {
        accessService.requireRole("EXPERT");
        accessService.requireApprovedAccount();
        MilestoneEntity milestone = milestoneRepository.findById(input.getMilestoneId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        ContractEntity contract = requireExpertOwnedContractByJob(milestone.getJobId());
        if (!"ACTIVE".equals(contract.getStatus())) throw new AppException("CHI DUOC SUBMIT DELIVERABLE KHI CONTRACT ACTIVE");
        if (contract.getBusinessNdaSignedAt() == null || contract.getExpertNdaSignedAt() == null) {
            throw new AppException("HAI BEN PHAI KY NDA TRUOC KHI BAN GIAO");
        }
        if (!List.of(ContractMilestoneEntity.STATUS_IN_PROGRESS, ContractMilestoneEntity.STATUS_OVERDUE)
                .contains(milestone.getStatus())) {
            throw new AppException("MILESTONE CHUA SAN SANG DE SUBMIT DELIVERABLE");
        }
        ContractMilestoneEntity contractMilestone = findContractMilestone(contract.getContractId(), milestone.getMilestoneId());
        ensureEscrowNotReleased(contractMilestone);
        List<DeliverableEntity> prior = deliverableRepository
                .findByMilestoneIdOrderBySubmissionRoundDesc(milestone.getMilestoneId());
        int nextRound = prior.stream().map(DeliverableEntity::getSubmissionRound)
                .filter(java.util.Objects::nonNull).max(Integer::compareTo).orElse(0) + 1;
        prior.stream().filter(item -> DeliverableEntity.STATUS_REJECTED.equals(item.getStatus()))
                .findFirst().ifPresent(item -> {
                    item.setStatus(DeliverableEntity.STATUS_SUPERSEDED);
                    deliverableRepository.save(item);
                });
        input.setSubmissionRound(nextRound);
        input.setStatus(DeliverableEntity.STATUS_SUBMITTED);
        DeliverableEntity saved = deliverableRepository.save(input);
        milestone.setStatus(ContractMilestoneEntity.STATUS_UNDER_REVIEW);
        milestone.setUpdatedAt(LocalDateTime.now());
        milestoneRepository.save(milestone);
        contractMilestone.setStatus(ContractMilestoneEntity.STATUS_UNDER_REVIEW);
        contractMilestoneRepository.save(contractMilestone);
        auditLogService.record("DELIVERABLE_SUBMITTED", "milestones", String.valueOf(milestone.getMilestoneId()), accessService.currentAccount().getAccountId());
        businessProfileRepository.findById(contract.getBusinessId())
                .ifPresent(business -> notificationService.notifyDeliverableSubmitted(
                        business.getAccountId(),
                        accessService.currentAccount().getAccountId(),
                        contract.getContractId(),
                        milestone.getMilestoneId(),
                        saved.getDeliverableId(),
                        milestone.getMilestoneName()
                ));
        return saved;
    }

    @Transactional
    public MilestoneEntity depositMilestoneEscrow(Integer contractId, Integer milestoneId) {
        accessService.requireRole("BUSINESS");
        accessService.requireApprovedAccount();
        ContractEntity contract = requireBusinessOwnedContract(contractId);
        if (!"ACTIVE".equals(contract.getStatus())) throw new AppException("CHI DUOC DEPOSIT MILESTONE KHI CONTRACT ACTIVE");
        MilestoneEntity milestone = milestoneRepository.findById(milestoneId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        if (milestone.getContractId() != null && !contractId.equals(milestone.getContractId())) throw new AppException("MILESTONE KHONG THUOC CONTRACT NAY");
        ContractMilestoneEntity contractMilestone = findContractMilestoneForUpdate(contractId, milestoneId);
        if (!ContractMilestoneEntity.STATUS_PENDING.equals(contractMilestone.getStatus())) {
            throw new AppException("MILESTONE KHONG O TRANG THAI PENDING DE DEPOSIT");
        }
        // Spec 9.1: phai hoan tat cac milestone truoc do theo order_index. Null-safe: milestone khong co order_index
        // khong duoc coi la milestone truoc, va neu milestone hien tai chua co order_index thi bo qua kiem tra thu tu.
        Integer currentOrderIndex = contractMilestone.getOrderIndex();
        boolean previousMilestonesIncomplete = currentOrderIndex != null
                && contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contractId).stream()
                .anyMatch(item -> item.getOrderIndex() != null
                        && item.getOrderIndex() < currentOrderIndex
                        && !ContractMilestoneEntity.STATUS_COMPLETED.equals(item.getStatus()));
        if (previousMilestonesIncomplete) {
            throw new AppException("PHAI HOAN TAT CAC MILESTONE TRUOC DO THEO THU TU TRUOC KHI DEPOSIT MILESTONE NAY");
        }
        ensureEscrowNotReleased(contractMilestone);
        Integer businessAccountId = businessProfileRepository.findById(contract.getBusinessId()).map(BusinessProfileEntity::getAccountId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
        walletLedgerService.holdEscrowFromAvailable(
                businessAccountId,
                contractMilestone.getFinalBudget(),
                WalletTransactionEntity.TX_ESCROW_DEPOSIT,
                "MILESTONE",
                milestoneId.longValue(),
                "Deposit milestone escrow",
                walletOperationContext(
                        contractId,
                        milestoneId,
                        walletMetadata("MILESTONE_ESCROW_DEPOSIT", null, null, accessService.currentAccount().getAccountId(),
                                businessAccountId, null, null, contractMilestone.getFinalBudget(), BigDecimal.ZERO),
                        "MILESTONE_ESCROW_DEPOSIT:" + contractId + ":" + milestoneId,
                        null
                ));
        LocalDateTime now = LocalDateTime.now();
        milestone.setStatus(ContractMilestoneEntity.STATUS_IN_PROGRESS);
        milestone.setUpdatedAt(now);
        contractMilestone.setStatus(ContractMilestoneEntity.STATUS_IN_PROGRESS);
        if (contractMilestone.getInProgressStartedAt() == null) {
            contractMilestone.setInProgressStartedAt(now);
        }
        contractMilestoneRepository.save(contractMilestone);
        MilestoneEntity saved = milestoneRepository.save(milestone);
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record("MILESTONE_ESCROW_DEPOSITED", "milestones", String.valueOf(milestoneId), actorAccountId);
        expertProfileRepository.findById(contract.getExpertId())
                .ifPresent(expert -> notificationService.notifyMilestoneEscrowDeposited(expert.getAccountId(), actorAccountId, contract.getContractId(), milestoneId, milestone.getMilestoneName()));
        return saved;
    }

    @Transactional
    public MilestoneEntity startMilestone(Integer milestoneId) {
        accessService.requireRole("EXPERT");
        accessService.requireApprovedAccount();
        MilestoneEntity milestone = milestoneRepository.findById(milestoneId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        ContractEntity contract = requireExpertOwnedContract(Optional.ofNullable(milestone.getContractId()).orElseGet(() -> contractRepository.findByJobId(milestone.getJobId()).map(ContractEntity::getContractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT CUA MILESTONE"))));
        if (!ContractEntity.STATUS_ACTIVE.equals(contract.getStatus())) throw new AppException("CHI DUOC BAT DAU MILESTONE KHI CONTRACT ACTIVE");
        ContractMilestoneEntity contractMilestone = findContractMilestoneForUpdate(contract.getContractId(), milestoneId);
        if (ContractMilestoneEntity.STATUS_IN_PROGRESS.equals(contractMilestone.getStatus())) {
            ensureEscrowNotReleased(contractMilestone);
            if (!ContractMilestoneEntity.STATUS_IN_PROGRESS.equals(milestone.getStatus())) {
                milestone.setStatus(ContractMilestoneEntity.STATUS_IN_PROGRESS);
                milestone.setUpdatedAt(LocalDateTime.now());
                return milestoneRepository.save(milestone);
            }
            return milestone;
        }
        if (!ContractMilestoneEntity.STATUS_DEPOSITED.equals(contractMilestone.getStatus())) {
            throw new AppException("MILESTONE PHAI DUOC DEPOSIT TRUOC KHI BAT DAU");
        }
        ensureEscrowNotReleased(contractMilestone);
        milestone.setStatus(ContractMilestoneEntity.STATUS_IN_PROGRESS);
        milestone.setUpdatedAt(LocalDateTime.now());
        contractMilestone.setStatus(ContractMilestoneEntity.STATUS_IN_PROGRESS);
        contractMilestoneRepository.save(contractMilestone);
        MilestoneEntity saved = milestoneRepository.save(milestone);
        auditLogService.record("MILESTONE_STARTED", "milestones", String.valueOf(milestoneId), accessService.currentAccount().getAccountId());
        return saved;
    }

    @Transactional
    public MilestoneProgressReportEntity submitProgressReport(Integer contractId, Integer milestoneId, String content, Integer percentComplete, String attachmentUrl) {
        ProgressReportRequest request = new ProgressReportRequest();
        request.setContent(content);
        request.setPercentComplete(percentComplete);
        request.setAttachmentUrl(attachmentUrl);
        return submitProgressReport(contractId, milestoneId, request);
    }

    @Transactional
    public MilestoneProgressReportEntity submitProgressReport(
            Integer contractId, Integer milestoneId, ProgressReportRequest request) {
        accessService.requireRole("EXPERT");
        accessService.requireApprovedAccount();
        String content = request == null ? null : request.getContent();
        Integer percentComplete = request == null ? null : request.getPercentComplete();
        String attachmentUrl = request == null ? null : request.getAttachmentUrl();
        if (content == null || content.isBlank()) throw new AppException("NOI DUNG BAO CAO TIEN DO KHONG DUOC RONG");
        ContractEntity contract = requireExpertOwnedContract(contractId);
        MilestoneEntity milestone = milestoneRepository.findById(milestoneId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        ContractMilestoneEntity contractMilestone = findContractMilestone(contractId, milestoneId);
        if (!List.of(ContractMilestoneEntity.STATUS_IN_PROGRESS, ContractMilestoneEntity.STATUS_OVERDUE)
                .contains(contractMilestone.getStatus())) {
            throw new AppException("CHI DUOC NOP BAO CAO TIEN DO KHI MILESTONE DANG THUC HIEN");
        }
        List<MilestoneProgressReportEntity> existingReports = milestoneProgressReportRepository.findByMilestoneIdOrderByCreatedAtAsc(milestoneId);
        requireLatestProgressReportAcknowledged(contractId, milestoneId);
        String checkpointType = nextCheckpointType(contractMilestone, existingReports);
        LocalDateTime checkpointDue = checkpointType == null ? null : checkpointDueDate(contractMilestone, checkpointType);
        LocalDateTime now = LocalDateTime.now();
        Optional<MilestoneProgressReportRequestEntity> openRequest =
                milestoneProgressReportRequestRepository
                        .findFirstByContractIdAndMilestoneIdAndStatusOrderByRequestNumberDesc(
                                contractId, milestoneId, MilestoneProgressReportRequestEntity.STATUS_PENDING);
        boolean isLate = openRequest.map(item -> now.isAfter(item.getDueAt()))
                .orElse(checkpointDue != null && now.isAfter(checkpointDue));
        MilestoneProgressReportEntity saved = milestoneProgressReportRepository.save(MilestoneProgressReportEntity.builder()
                .contractId(contractId)
                .milestoneId(milestoneId)
                .submittedByAccountId(accessService.currentAccount().getAccountId())
                .checkpointType(checkpointType)
                .content(content.trim())
                .percentComplete(percentComplete)
                .attachmentUrl(attachmentUrl)
                .sourceCodeUrl(request.getSourceCodeUrl())
                .demoLink(request.getDemoLink())
                .submissionNotes(request.getSubmissionNotes())
                .isLate(isLate)
                .requiresAdjustment(false)
                .acknowledgementState(MilestoneProgressReportEntity.ACK_PENDING)
                .build());
        openRequest.ifPresent(item -> {
            item.setStatus(MilestoneProgressReportRequestEntity.STATUS_SUBMITTED);
            item.setSubmittedAt(now);
            item.setProgressReportId(saved.getProgressReportId());
            milestoneProgressReportRequestRepository.save(item);
        });
        auditLogService.record("PROGRESS_REPORT_SUBMITTED", "milestone_progress_reports", String.valueOf(saved.getProgressReportId()), accessService.currentAccount().getAccountId());
        businessProfileRepository.findById(contract.getBusinessId())
                .ifPresent(business -> notificationService.notifyProgressReportSubmitted(
                        business.getAccountId(),
                        accessService.currentAccount().getAccountId(),
                        contractId,
                        milestoneId,
                        milestone.getMilestoneName(),
                        isLate
                ));
        return saved;
    }

    public List<MilestoneProgressReportEntity> listProgressReports(Integer contractId, Integer milestoneId) {
        requireContractParticipantOrOperator(contractId);
        findContractMilestone(contractId, milestoneId);
        return milestoneProgressReportRepository.findByMilestoneIdOrderByCreatedAtAsc(milestoneId);
    }

    @Transactional
    public MilestoneProgressReportRequestEntity requestProgressReport(Integer contractId, Integer milestoneId) {
        accessService.requireRole("BUSINESS");
        ContractEntity contract = requireBusinessOwnedContract(contractId);
        if (!ContractEntity.STATUS_ACTIVE.equals(contract.getStatus())) throw new AppException("CONTRACT_INVALID_STATUS");
        ContractMilestoneEntity milestone = findContractMilestoneForUpdate(contractId, milestoneId);
        if (!List.of(ContractMilestoneEntity.STATUS_IN_PROGRESS, ContractMilestoneEntity.STATUS_OVERDUE)
                .contains(milestone.getStatus())) throw new AppException("MILESTONE_NOT_EXECUTABLE");
        requireLatestProgressReportAcknowledged(contractId, milestoneId);
        LocalDateTime now = LocalDateTime.now();
        Optional<MilestoneProgressReportRequestEntity> pending =
                milestoneProgressReportRequestRepository
                        .findFirstByContractIdAndMilestoneIdAndStatusOrderByRequestNumberDesc(
                                contractId, milestoneId, MilestoneProgressReportRequestEntity.STATUS_PENDING);
        pending.ifPresent(item -> {
            if (!item.getDueAt().isBefore(now)) throw new AppException("PROGRESS_REPORT_REQUEST_ALREADY_PENDING");
            item.setStatus(MilestoneProgressReportRequestEntity.STATUS_EXPIRED);
            milestoneProgressReportRequestRepository.save(item);
            auditLogService.record("PROGRESS_REPORT_REQUEST_EXPIRED", "milestone_progress_report_requests",
                    String.valueOf(item.getProgressReportRequestId()), accessService.currentAccount().getAccountId());
        });
        int nextNumber = milestoneProgressReportRequestRepository
                .findFirstByContractIdAndMilestoneIdOrderByRequestNumberDesc(contractId, milestoneId)
                .map(item -> item.getRequestNumber() + 1).orElse(1);
        MilestoneProgressReportRequestEntity saved = milestoneProgressReportRequestRepository.save(
                MilestoneProgressReportRequestEntity.builder()
                        .contractId(contractId).milestoneId(milestoneId)
                        .requestedByAccountId(accessService.currentAccount().getAccountId())
                        .requestNumber(nextNumber).status(MilestoneProgressReportRequestEntity.STATUS_PENDING)
                        .requestedAt(now).dueAt(now.plusHours(nextNumber == 1 ? 24 : 12)).build());
        auditLogService.record("PROGRESS_REPORT_REQUESTED", "milestone_progress_report_requests",
                String.valueOf(saved.getProgressReportRequestId()), accessService.currentAccount().getAccountId());
        expertProfileRepository.findById(contract.getExpertId()).ifPresent(expert ->
                notificationService.notifyContractEvent(expert.getAccountId(), accessService.currentAccount().getAccountId(),
                        "PROGRESS_REPORT_REQUESTED", "Yêu cầu báo cáo tiến độ",
                        "Doanh nghiệp đã yêu cầu báo cáo tiến độ cho milestone.", contractId));
        return saved;
    }

    @Transactional
    public MilestoneProgressReportEntity feedbackProgressReport(
            Integer contractId, Integer milestoneId, Long progressReportId, ProgressReportFeedbackRequest input) {
        accessService.requireRole("BUSINESS");
        ContractEntity contract = requireBusinessOwnedContract(contractId);
        ContractMilestoneEntity milestone = findContractMilestone(contractId, milestoneId);
        if (List.of(ContractMilestoneEntity.STATUS_COMPLETED, ContractMilestoneEntity.STATUS_CANCELLED)
                .contains(milestone.getStatus())) {
            throw new AppException("PROGRESS_REPORT_FEEDBACK_NOT_ALLOWED");
        }
        MilestoneProgressReportEntity report = milestoneProgressReportRepository.findById(progressReportId)
                .filter(item -> contractId.equals(item.getContractId()) && milestoneId.equals(item.getMilestoneId()))
                .orElseThrow(() -> new NotFoundException("PROGRESS_REPORT_REQUEST_NOT_FOUND"));
        if (input == null || input.getFeedback() == null || input.getFeedback().isBlank()) {
            throw new AppException("PROGRESS_REPORT_FEEDBACK_NOT_ALLOWED");
        }
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        report.setBusinessFeedback(input.getFeedback().trim());
        report.setFeedbackCategory(input.getCategory());
        report.setFeedbackSeverity(input.getSeverity());
        try {
            report.setFeedbackDodItems(input.getDodItems() == null ? null : objectMapper.writeValueAsString(input.getDodItems()));
        } catch (Exception ex) {
            throw new AppException("PROGRESS_REPORT_FEEDBACK_NOT_ALLOWED");
        }
        report.setRequiresAdjustment(Boolean.TRUE.equals(input.getRequiresAdjustment()));
        report.setFeedbackByAccountId(actorAccountId);
        report.setFeedbackAt(LocalDateTime.now());
        if (MilestoneProgressReportEntity.ACK_PENDING.equals(report.getAcknowledgementState())) {
            report.setAcknowledgementState(MilestoneProgressReportEntity.ACKNOWLEDGED);
            report.setAcknowledgedByAccountId(actorAccountId);
            report.setAcknowledgedAt(LocalDateTime.now());
        }
        MilestoneProgressReportEntity saved = milestoneProgressReportRepository.save(report);
        auditLogService.record("PROGRESS_REPORT_FEEDBACK_RECORDED", "milestone_progress_reports",
                String.valueOf(progressReportId), actorAccountId);
        expertProfileRepository.findById(contract.getExpertId()).ifPresent(expert ->
                notificationService.notifyProgressReportFeedbackRecorded(
                        expert.getAccountId(), actorAccountId, contractId, milestoneId, progressReportId));
        return saved;
    }

    @Transactional
    public MilestoneProgressReportEntity acknowledgeProgressReport(
            Integer contractId, Integer milestoneId, Long progressReportId) {
        accessService.requireRole("BUSINESS");
        ContractEntity contract = requireBusinessOwnedContract(contractId);
        ContractMilestoneEntity milestone = findContractMilestone(contractId, milestoneId);
        if (List.of(ContractMilestoneEntity.STATUS_COMPLETED, ContractMilestoneEntity.STATUS_CANCELLED)
                .contains(milestone.getStatus())) {
            throw new AppException("PROGRESS_REPORT_ACK_NOT_ALLOWED");
        }
        MilestoneProgressReportEntity report = milestoneProgressReportRepository.findById(progressReportId)
                .filter(item -> contractId.equals(item.getContractId()) && milestoneId.equals(item.getMilestoneId()))
                .orElseThrow(() -> new NotFoundException("PROGRESS_REPORT_REQUEST_NOT_FOUND"));
        if (!MilestoneProgressReportEntity.ACK_PENDING.equals(report.getAcknowledgementState())) {
            throw new AppException("PROGRESS_REPORT_ACK_NOT_ALLOWED");
        }
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        report.setAcknowledgementState(MilestoneProgressReportEntity.ACKNOWLEDGED);
        report.setAcknowledgedByAccountId(actorAccountId);
        report.setAcknowledgedAt(LocalDateTime.now());
        MilestoneProgressReportEntity saved = milestoneProgressReportRepository.save(report);
        auditLogService.record("PROGRESS_REPORT_ACKNOWLEDGED", "milestone_progress_reports",
                String.valueOf(progressReportId), actorAccountId);
        expertProfileRepository.findById(contract.getExpertId()).ifPresent(expert ->
                notificationService.notifyContractEvent(expert.getAccountId(), actorAccountId,
                        "PROGRESS_REPORT_ACKNOWLEDGED", "Báo cáo tiến độ đã được xác nhận",
                        "Doanh nghiệp đã xác nhận báo cáo tiến độ. Bạn có thể gửi báo cáo tiếp theo khi phù hợp.", contractId));
        return saved;
    }

    private void requireLatestProgressReportAcknowledged(Integer contractId, Integer milestoneId) {
        milestoneProgressReportRepository.findFirstByContractIdAndMilestoneIdOrderByCreatedAtDesc(contractId, milestoneId)
                .filter(report -> MilestoneProgressReportEntity.ACK_PENDING.equals(report.getAcknowledgementState()))
                .ifPresent(report -> {
                    throw new AppException("PROGRESS_REPORT_ACK_PENDING");
                });
    }

    @Transactional
    public List<MilestoneEntity> markOverdueMilestones(Integer contractId) {
        accessService.requireRole("ADMIN");
        LocalDateTime now = LocalDateTime.now();
        List<MilestoneEntity> changed = new java.util.ArrayList<>();
        for (ContractMilestoneEntity item : contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contractId)) {
            LocalDateTime due = milestoneDueAt(item);
            if (ContractMilestoneEntity.STATUS_IN_PROGRESS.equals(item.getStatus())
                    && due != null && now.isAfter(due)) {
                item.setStatus(ContractMilestoneEntity.STATUS_OVERDUE);
                contractMilestoneRepository.save(item);
                milestoneRepository.findById(item.getJobMilestoneId()).ifPresent(live -> {
                    live.setStatus(ContractMilestoneEntity.STATUS_OVERDUE);
                    changed.add(milestoneRepository.save(live));
                });
                auditLogService.record("MILESTONE_MARKED_OVERDUE", "milestones",
                        String.valueOf(item.getJobMilestoneId()), accessService.currentAccount().getAccountId());
                contractRepository.findById(contractId).ifPresent(contract ->
                        notifyBothParticipants(contract, accessService.currentAccount().getAccountId(),
                                (receiver, actor) -> notificationService.notifyMilestoneMarkedOverdue(
                                        receiver, actor, contractId, item.getJobMilestoneId())));
            }
        }
        return changed;
    }

    private LocalDateTime milestoneDueAt(ContractMilestoneEntity item) {
        if (item.getInProgressStartedAt() == null || item.getDuration() == null || item.getDuration() <= 0) return null;
        return item.getInProgressStartedAt().plusDays(durationToDays(item.getDuration(), item.getDurationUnit()));
    }

    // Note: Bao cao dau tien nop cho checkpoint MIDPOINT, sau do PRE_DEADLINE; qua 2 moc thi bao cao them khong gan checkpoint (checkpointType = null).
    private String nextCheckpointType(List<MilestoneProgressReportEntity> existingReports) {
        boolean hasMidpoint = existingReports.stream().anyMatch(r -> MilestoneProgressReportEntity.CHECKPOINT_MIDPOINT.equals(r.getCheckpointType()));
        boolean hasPreDeadline = existingReports.stream().anyMatch(r -> MilestoneProgressReportEntity.CHECKPOINT_PRE_DEADLINE.equals(r.getCheckpointType()));
        if (!hasMidpoint) return MilestoneProgressReportEntity.CHECKPOINT_MIDPOINT;
        if (!hasPreDeadline) return MilestoneProgressReportEntity.CHECKPOINT_PRE_DEADLINE;
        return null;
    }

    // Note: Neu milestone chua co in_progress_started_at hoac chua khai bao duration thi khong tinh duoc han, tra ve null (khong ep buoc).
    private LocalDateTime checkpointDueDate(ContractMilestoneEntity contractMilestone, String checkpointType) {
        if (contractMilestone.getInProgressStartedAt() == null || contractMilestone.getDuration() == null || contractMilestone.getDuration() <= 0) {
            return null;
        }
        long totalDays = durationToDays(contractMilestone.getDuration(), contractMilestone.getDurationUnit());
        double fraction = MilestoneProgressReportEntity.CHECKPOINT_MIDPOINT.equals(checkpointType) ? 0.5 : 0.8;
        long offsetSeconds = Math.round(totalDays * 24 * 60 * 60 * fraction);
        return contractMilestone.getInProgressStartedAt().plusSeconds(offsetSeconds);
    }

    private long durationToDays(Integer duration, String durationUnit) {
        String unit = durationUnit == null ? "DAY" : durationUnit.trim().toUpperCase();
        long factor = switch (unit) {
            case "WEEK" -> 7;
            case "MONTH" -> 30;
            default -> 1;
        };
        return duration * factor;
    }

    private String nextCheckpointType(ContractMilestoneEntity contractMilestone, List<MilestoneProgressReportEntity> existingReports) {
        if (contractMilestone.getInProgressStartedAt() == null || contractMilestone.getDuration() == null || contractMilestone.getDuration() <= 0) {
            return null;
        }
        return nextCheckpointType(existingReports);
    }

    @Transactional
    public MilestoneEntity approveMilestone(Integer milestoneId) {
        accessService.requireRole("BUSINESS");
        accessService.requireApprovedAccount();
        MilestoneEntity milestone = milestoneRepository.findById(milestoneId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        ContractEntity contract = requireBusinessOwnedContract(Optional.ofNullable(milestone.getContractId()).orElseGet(() -> contractRepository.findByJobId(milestone.getJobId()).map(ContractEntity::getContractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT CUA MILESTONE"))));
        if (!ContractEntity.STATUS_ACTIVE.equals(contract.getStatus())) throw new AppException("CHI DUOC DUYET MILESTONE KHI CONTRACT ACTIVE");
        if (!ContractMilestoneEntity.STATUS_UNDER_REVIEW.equals(milestone.getStatus())) throw new AppException("MILESTONE CHUA O TRANG THAI CHO DUYET");
        ContractMilestoneEntity contractMilestone = findContractMilestoneForUpdate(contract.getContractId(), milestoneId);
        ensureEscrowNotReleased(contractMilestone);
        Integer businessAccountId = businessProfileRepository.findById(contract.getBusinessId()).map(BusinessProfileEntity::getAccountId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
        Integer expertAccountId = expertProfileRepository.findById(contract.getExpertId()).map(ExpertProfileEntity::getAccountId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY EXPERT PROFILE"));
        String metadata = walletMetadata("BUSINESS_APPROVAL", null, null, accessService.currentAccount().getAccountId(), businessAccountId, expertAccountId, BigDecimal.valueOf(100), contractMilestone.getFinalBudget(), BigDecimal.ZERO);
        walletLedgerService.debitEscrow(
                businessAccountId,
                contractMilestone.getFinalBudget(),
                WalletTransactionEntity.TX_ESCROW_RELEASE,
                "MILESTONE",
                milestoneId.longValue(),
                "Release approved milestone escrow",
                walletOperationContext(contract.getContractId(), milestoneId, metadata,
                        "MILESTONE_ESCROW_RELEASE:" + contract.getContractId() + ":" + milestoneId + ":BUSINESS_APPROVAL", null));
        walletLedgerService.creditAvailable(
                expertAccountId,
                contractMilestone.getFinalBudget(),
                WalletTransactionEntity.TX_ESCROW_RELEASE,
                "MILESTONE",
                milestoneId.longValue(),
                "Milestone approved payout",
                walletOperationContext(contract.getContractId(), milestoneId, metadata,
                        "MILESTONE_ESCROW_RELEASE:" + contract.getContractId() + ":" + milestoneId + ":BUSINESS_APPROVAL",
                        "EXPERT_AVAILABLE_CREDIT"));
        markEscrowReleased(contractMilestone, "BUSINESS_APPROVAL", milestoneId.longValue());
        milestone.setEscrowReleasedAt(contractMilestone.getEscrowReleasedAt());
        milestone.setSettlementSourceType(contractMilestone.getSettlementSourceType());
        milestone.setSettlementSourceId(contractMilestone.getSettlementSourceId());
        milestone.setStatus(ContractMilestoneEntity.STATUS_COMPLETED);
        contractMilestone.setStatus(ContractMilestoneEntity.STATUS_COMPLETED);
        contractMilestoneRepository.save(contractMilestone);
        resolveActiveDisputeByBusinessApproval(milestoneId);
        MilestoneEntity saved = milestoneRepository.save(milestone);
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record("MILESTONE_APPROVED", "milestones", String.valueOf(milestoneId), actorAccountId);
        notificationService.notifyMilestoneApproved(expertAccountId, actorAccountId, contract.getContractId(), milestoneId, milestone.getMilestoneName());
        tryCompleteContract(contract, actorAccountId);
        return saved;
    }

    @Transactional
    public MilestoneEntity rejectMilestone(Integer milestoneId, String reason) {
        accessService.requireRole("BUSINESS");
        accessService.requireApprovedAccount();
        MilestoneEntity milestone = milestoneRepository.findById(milestoneId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        ContractEntity contract = requireBusinessOwnedContract(Optional.ofNullable(milestone.getContractId()).orElseGet(() -> contractRepository.findByJobId(milestone.getJobId()).map(ContractEntity::getContractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT CUA MILESTONE"))));
        if (!ContractEntity.STATUS_ACTIVE.equals(contract.getStatus())) throw new AppException("CHI DUOC TU CHOI MILESTONE KHI CONTRACT ACTIVE");
        if (!ContractMilestoneEntity.STATUS_UNDER_REVIEW.equals(milestone.getStatus())) {
            throw new AppException("MILESTONE CHUA O TRANG THAI CHO TU CHOI");
        }
        if (reason == null || reason.isBlank()) throw new AppException("REJECTION_FEEDBACK_REQUIRED");
        if (!activeDisputesForMilestone(milestoneId).isEmpty()) throw new AppException("DISPUTE_ALREADY_ACTIVE");
        ContractMilestoneEntity contractMilestone = findContractMilestone(contract.getContractId(), milestoneId);
        DeliverableEntity current = deliverableRepository.findByMilestoneIdOrderBySubmissionRoundDesc(milestoneId)
                .stream().findFirst().orElseThrow(() -> new NotFoundException("KHONG TIM THAY DELIVERABLE"));
        current.setStatus(DeliverableEntity.STATUS_REJECTED);
        current.setRejectionFeedback(reason.trim());
        current.setRejectedAt(LocalDateTime.now());
        deliverableRepository.save(current);
        int rejectCount = (contractMilestone.getRejectCount() == null ? 0 : contractMilestone.getRejectCount()) + 1;
        contractMilestone.setRejectCount(rejectCount);
        contractMilestone.setResubmitCount(rejectCount);
        contractMilestone.setLastRejectionFeedback(reason.trim());
        contractMilestone.setStatus(ContractMilestoneEntity.STATUS_IN_PROGRESS);
        milestone.setRejectCount(rejectCount);
        milestone.setLastRejectionFeedback(reason.trim());
        milestone.setStatus(ContractMilestoneEntity.STATUS_IN_PROGRESS);
        milestone.setUpdatedAt(LocalDateTime.now());
        contractMilestoneRepository.save(contractMilestone);
        MilestoneEntity saved = milestoneRepository.save(milestone);
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record("MILESTONE_REJECTED", "milestones", String.valueOf(milestoneId), actorAccountId);
        expertProfileRepository.findById(contract.getExpertId())
                .ifPresent(expert -> notificationService.notifyMilestoneRejected(expert.getAccountId(), actorAccountId, contract.getContractId(), milestoneId, milestone.getMilestoneName(), reason));
        return saved;
    }

    @Transactional
    public DisputeEntity initiateDispute(Integer contractId, Integer milestoneId, String initiatedBy, String initiationType) {
        return initiateDispute(contractId, milestoneId, initiatedBy, initiationType, null);
    }

    @Transactional
    public DisputeEntity initiateDispute(Integer contractId, Integer milestoneId, String initiatedBy, String initiationType, String reason) {
        requireApprovedForBusinessOrExpert();
        ContractEntity contract = requireContractParticipantOrOperator(contractId);
        if (!ContractEntity.STATUS_ACTIVE.equals(contract.getStatus())) {
            throw new AppException("CHI DUOC KHOI TAO DISPUTE KHI CONTRACT ACTIVE");
        }
        MilestoneEntity milestone = milestoneRepository.findById(milestoneId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        if (!List.of(ContractMilestoneEntity.STATUS_IN_PROGRESS, ContractMilestoneEntity.STATUS_OVERDUE,
                ContractMilestoneEntity.STATUS_UNDER_REVIEW).contains(milestone.getStatus())) {
            throw new AppException("MILESTONE KHONG O TRANG THAI CHO PHEP KHOI TAO DISPUTE");
        }
        if (!activeTerminationRequests(contractId).isEmpty()) throw new AppException("TERMINATION_REQUEST_ALREADY_ACTIVE");
        if (!activeDisputesForMilestone(milestoneId).isEmpty()) {
            throw new AppException("MILESTONE DA CO DISPUTE DANG HOAT DONG");
        }
        String actorRole = accessService.currentAccount().getRole().getRoleName();
        if (initiatedBy != null && !initiatedBy.isBlank() && !actorRole.equals(normalizeInitiator(initiatedBy))) {
            throw new AppException("INITIATED_BY PHAI KHOP VOI VAI TRO NGUOI DUNG HIEN TAI");
        }
        ContractMilestoneEntity contractMilestone = findContractMilestone(contractId, milestoneId);
        String previousStatus = milestone.getStatus();
        milestone.setStatus(ContractMilestoneEntity.STATUS_DISPUTED);
        contractMilestone.setStatus(ContractMilestoneEntity.STATUS_DISPUTED);
        milestoneRepository.save(milestone);
        contractMilestoneRepository.save(contractMilestone);
        DisputeEntity dispute = DisputeEntity.builder()
                .contractId(contract.getContractId())
                .milestoneId(milestoneId)
                .initiatedBy(normalizeInitiator(actorRole))
                .initiatedByAccountId(accessService.currentAccount().getAccountId())
                .initiationType(normalizeInitiationType(initiationType))
                .evidenceReport(reason == null || reason.isBlank() ? null : reason.trim())
                .previousMilestoneStatus(previousStatus)
                .status(DisputeEntity.STATUS_PENDING_SELF_RESOLVE)
                .build();
        DisputeEntity saved = disputeRepository.save(dispute);
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record("DISPUTE_CREATED", "disputes", String.valueOf(saved.getDisputeId()), actorAccountId);
        notifyContractParticipantsExcept(contract, actorAccountId,
                (receiver, actor) -> notificationService.notifyDisputeInitiated(receiver, actor, contract.getContractId(), milestoneId, saved.getDisputeId()));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<DisputeSelfResolveReplyResponse> listSelfResolveReplies(Integer disputeId) {
        DisputeEntity dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        requireSelfResolveReadAccess(dispute);
        return disputeSelfResolveReplyRepository.findByDisputeIdOrderByCreatedAtAscReplyIdAsc(disputeId).stream()
                .map(this::toSelfResolveReplyResponse)
                .toList();
    }

    @Transactional
    public DisputeSelfResolveReplyResponse createSelfResolveReply(
            Integer disputeId, CreateDisputeSelfResolveReplyRequest request) {
        ContractEntity contract = requireSelfResolveParticipant(disputeId);
        DisputeEntity dispute = disputeRepository.findByIdForUpdate(disputeId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        requirePendingSelfResolve(dispute);
        if (request == null) throw new AppException("SELF_RESOLVE_REPLY_REQUEST_REQUIRED");

        String replyType = normalizeSelfResolveReplyType(request.getReplyType());
        String proposedAction = normalizeSelfResolveProposedAction(request.getProposedAction(), replyType);
        String message = requireSelfResolveMessage(request.getMessage());
        if (request.getProposedDueAt() != null && request.getProposedDueAt().isBefore(LocalDateTime.now())) {
            throw new AppException("SELF_RESOLVE_PROPOSED_DUE_AT_MUST_NOT_BE_PAST");
        }

        AccountEntity actor = accessService.currentAccount();
        DisputeSelfResolveReplyEntity saved = disputeSelfResolveReplyRepository.save(
                DisputeSelfResolveReplyEntity.builder()
                        .disputeId(disputeId)
                        .actorAccountId(actor.getAccountId())
                        .actorRole(actor.getRole().getRoleName())
                        .replyType(replyType)
                        .proposedAction(proposedAction)
                        .message(message)
                        .proposedDueAt(request.getProposedDueAt())
                        .build());
        auditLogService.record("DISPUTE_SELF_RESOLVE_REPLY_CREATED", "dispute_self_resolve_replies",
                String.valueOf(saved.getReplyId()), actor.getAccountId());
        notifyContractParticipantsExcept(contract, actor.getAccountId(),
                (receiver, sender) -> notificationService.notifyDisputeSelfResolveReplyCreated(
                        receiver, sender, contract.getContractId(), disputeId));
        return toSelfResolveReplyResponse(saved);
    }

    @Transactional
    public DisputeEntity acceptSelfResolveAgreement(
            Integer disputeId, AcceptDisputeSelfResolveAgreementRequest request) {
        ContractEntity contract = requireSelfResolveParticipant(disputeId);
        DisputeEntity dispute = disputeRepository.findByIdForUpdate(disputeId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        requirePendingSelfResolve(dispute);
        if (request == null || request.getAcceptedReplyId() == null) {
            throw new AppException("SELF_RESOLVE_ACCEPTED_REPLY_REQUIRED");
        }
        AccountEntity actor = accessService.currentAccount();
        DisputeSelfResolveReplyEntity acceptedReply = disputeSelfResolveReplyRepository
                .findByReplyIdAndDisputeId(request.getAcceptedReplyId(), disputeId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY SELF RESOLVE REPLY"));
        if (actor.getAccountId().equals(acceptedReply.getActorAccountId())) {
            throw new AppException("KHONG THE CHAP NHAN PHAN HOI CUA CHINH MINH");
        }
        String finalAction = normalizeSupportedSelfResolveFinalAction(request.getFinalAction());
        if (!finalAction.equals(acceptedReply.getProposedAction())) {
            throw new AppException("SELF_RESOLVE_FINAL_ACTION_MUST_MATCH_ACCEPTED_REPLY");
        }
        if (!"CONTINUE_REVISION".equals(finalAction)) {
            accessService.requireRole("BUSINESS");
        }
        String message = requireSelfResolveMessage(request.getMessage());
        DisputeSelfResolveReplyEntity acceptance = disputeSelfResolveReplyRepository.save(
                DisputeSelfResolveReplyEntity.builder()
                        .disputeId(disputeId)
                        .actorAccountId(actor.getAccountId())
                        .actorRole(actor.getRole().getRoleName())
                        .replyType("ACCEPT_PROPOSAL")
                        .proposedAction(finalAction)
                        .message(message)
                        .acceptedReplyId(acceptedReply.getReplyId())
                        .build());

        MilestoneEntity milestone = milestoneRepository.findById(dispute.getMilestoneId())
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        ContractMilestoneEntity contractMilestone = findContractMilestoneForUpdate(contract.getContractId(), milestone.getMilestoneId());
        if ("CONTINUE_REVISION".equals(finalAction)) {
            milestone.setStatus(ContractMilestoneEntity.STATUS_IN_PROGRESS);
            contractMilestone.setStatus(ContractMilestoneEntity.STATUS_IN_PROGRESS);
            if (contractMilestone.getInProgressStartedAt() == null) {
                contractMilestone.setInProgressStartedAt(LocalDateTime.now());
            }
            dispute.setResolutionType(DisputeEntity.RESOLUTION_SELF_RESOLVE_AGREEMENT_CONTINUE_REVISION);
        } else {
            applySelfResolveApproval(contract, dispute, milestone, contractMilestone, finalAction, actor.getAccountId());
        }
        dispute.setStatus(DisputeEntity.STATUS_RESOLVED);
        dispute.setResolvedAt(LocalDateTime.now());
        contractMilestoneRepository.save(contractMilestone);
        milestoneRepository.save(milestone);
        DisputeEntity saved = disputeRepository.save(dispute);
        auditLogService.record("DISPUTE_SELF_RESOLVE_AGREEMENT_ACCEPTED", "disputes",
                String.valueOf(disputeId), actor.getAccountId());
        notifyBothParticipants(contract, actor.getAccountId(),
                (receiver, sender) -> notificationService.notifyDisputeSelfResolveAgreementAccepted(
                        receiver, sender, contract.getContractId(), disputeId, finalAction));
        if (!"CONTINUE_REVISION".equals(finalAction)) {
            tryCompleteContract(contract, actor.getAccountId());
        }
        return saved;
    }

    private void applySelfResolveApproval(ContractEntity contract, DisputeEntity dispute, MilestoneEntity milestone,
                                          ContractMilestoneEntity contractMilestone, String finalAction, Integer actorAccountId) {
        if (!ContractEntity.STATUS_ACTIVE.equals(contract.getStatus())) {
            throw new AppException("CHI DUOC CHAP NHAN THOA HIEP KHI CONTRACT ACTIVE");
        }
        ensureEscrowNotReleased(contractMilestone);
        Integer businessAccountId = businessProfileRepository.findById(contract.getBusinessId())
                .map(BusinessProfileEntity::getAccountId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
        Integer expertAccountId = expertProfileRepository.findById(contract.getExpertId())
                .map(ExpertProfileEntity::getAccountId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY EXPERT PROFILE"));
        String operationKey = "MILESTONE_ESCROW_RELEASE:" + contract.getContractId() + ":" + milestone.getMilestoneId()
                + ":SELF_RESOLVE_AGREEMENT";
        String metadata = walletMetadata("SELF_RESOLVE_AGREEMENT", dispute.getDisputeId(), null, actorAccountId,
                businessAccountId, expertAccountId, BigDecimal.valueOf(100), contractMilestone.getFinalBudget(), BigDecimal.ZERO);
        walletLedgerService.debitEscrow(businessAccountId, contractMilestone.getFinalBudget(),
                WalletTransactionEntity.TX_ESCROW_RELEASE, "MILESTONE", milestone.getMilestoneId().longValue(),
                "Release self-resolve agreement escrow", walletOperationContext(contract.getContractId(), milestone.getMilestoneId(), metadata, operationKey, null));
        walletLedgerService.creditAvailable(expertAccountId, contractMilestone.getFinalBudget(),
                WalletTransactionEntity.TX_ESCROW_RELEASE, "MILESTONE", milestone.getMilestoneId().longValue(),
                "Self-resolve agreement payout", walletOperationContext(contract.getContractId(), milestone.getMilestoneId(), metadata, operationKey, "EXPERT_AVAILABLE_CREDIT"));
        markEscrowReleased(contractMilestone, "SELF_RESOLVE_AGREEMENT", dispute.getDisputeId().longValue());
        milestone.setEscrowReleasedAt(contractMilestone.getEscrowReleasedAt());
        milestone.setSettlementSourceType(contractMilestone.getSettlementSourceType());
        milestone.setSettlementSourceId(contractMilestone.getSettlementSourceId());
        milestone.setStatus(ContractMilestoneEntity.STATUS_COMPLETED);
        contractMilestone.setStatus(ContractMilestoneEntity.STATUS_COMPLETED);
        dispute.setResolutionType("ACCEPT_DELIVERABLE".equals(finalAction)
                ? DisputeEntity.RESOLUTION_SELF_RESOLVE_AGREEMENT_ACCEPT_DELIVERABLE
                : DisputeEntity.RESOLUTION_SELF_RESOLVE_AGREEMENT_CONTINUE_NEXT_MILESTONE);
    }

    private ContractEntity requireSelfResolveParticipant(Integer disputeId) {
        requireApprovedForBusinessOrExpert();
        AccountEntity actor = accessService.currentAccount();
        if (!List.of("BUSINESS", "EXPERT").contains(actor.getRole().getRoleName())) {
            throw new AppException("CHI BUSINESS HOAC EXPERT DUOC THUONG LUONG TRANH CHAP");
        }
        DisputeEntity dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        return requireContractParticipantOrOperator(dispute.getContractId());
    }

    private void requireSelfResolveReadAccess(DisputeEntity dispute) {
        AccountEntity actor = accessService.currentAccount();
        String role = actor.getRole().getRoleName();
        if ("ADMIN".equals(role)) return;
        if ("STAFF".equals(role)) {
            if (DisputeEntity.STATUS_PENDING_SELF_RESOLVE.equals(dispute.getStatus())) {
                throw new AppException("STAFF CHI DUOC XEM THUONG LUONG SAU KHI ESCALATE");
            }
            requireAssignedStaff(dispute);
            return;
        }
        if (!List.of("BUSINESS", "EXPERT").contains(role)) {
            throw new AppException("BAN KHONG CO QUYEN XEM PHAN HOI THUONG LUONG");
        }
        requireApprovedForBusinessOrExpert();
        requireContractParticipantOrOperator(dispute.getContractId());
    }

    private void requirePendingSelfResolve(DisputeEntity dispute) {
        if (!DisputeEntity.STATUS_PENDING_SELF_RESOLVE.equals(dispute.getStatus())) {
            throw new AppException("DISPUTE KHONG O GIAI DOAN TU THUONG LUONG");
        }
    }

    private String normalizeSelfResolveReplyType(String value) {
        if (value == null || value.isBlank()) throw new AppException("SELF_RESOLVE_REPLY_TYPE_REQUIRED");
        String normalized = value.trim().toUpperCase();
        if (!List.of("ACCEPT_REQUEST", "COUNTER_PROPOSAL", "REQUEST_ADJUSTMENT").contains(normalized)) {
            throw new AppException("SELF_RESOLVE_REPLY_TYPE_KHONG_HOP_LE");
        }
        return normalized;
    }

    private String normalizeSelfResolveProposedAction(String value, String replyType) {
        boolean required = List.of("ACCEPT_REQUEST", "COUNTER_PROPOSAL", "ACCEPT_PROPOSAL").contains(replyType);
        if (value == null || value.isBlank()) {
            if (required) throw new AppException("SELF_RESOLVE_PROPOSED_ACTION_REQUIRED");
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if (!List.of("CONTINUE_REVISION", "ACCEPT_DELIVERABLE", "CONTINUE_NEXT_MILESTONE", "PARTIAL_REFUND", "OTHER").contains(normalized)) {
            throw new AppException("SELF_RESOLVE_PROPOSED_ACTION_KHONG_HOP_LE");
        }
        return normalized;
    }

    private String normalizeSupportedSelfResolveFinalAction(String value) {
        String normalized = normalizeSelfResolveProposedAction(value, "ACCEPT_PROPOSAL");
        if (!List.of("CONTINUE_REVISION", "ACCEPT_DELIVERABLE", "CONTINUE_NEXT_MILESTONE").contains(normalized)) {
            throw new AppException("SELF_RESOLVE_FINAL_ACTION_CHUA_DUOC_HO_TRO");
        }
        return normalized;
    }

    private String requireSelfResolveMessage(String value) {
        if (value == null || value.isBlank()) throw new AppException("SELF_RESOLVE_MESSAGE_REQUIRED");
        return value.trim();
    }

    private DisputeSelfResolveReplyResponse toSelfResolveReplyResponse(DisputeSelfResolveReplyEntity reply) {
        String displayName = accountRepository.findById(reply.getActorAccountId())
                .map(AccountEntity::getFullName)
                .filter(name -> !name.isBlank())
                .orElse("BUSINESS".equals(reply.getActorRole()) ? "Doanh nghiệp" : "Chuyên gia");
        return DisputeSelfResolveReplyResponse.builder()
                .replyId(reply.getReplyId())
                .disputeId(reply.getDisputeId())
                .actorRole(reply.getActorRole())
                .actorDisplayName(displayName)
                .replyType(reply.getReplyType())
                .proposedAction(reply.getProposedAction())
                .message(reply.getMessage())
                .proposedDueAt(reply.getProposedDueAt())
                .createdAt(reply.getCreatedAt())
                .build();
    }

    // Note: Luong legacy /complete truoc day danh dau COMPLETED ma KHONG release escrow -> Expert khong duoc thanh toan.
    // Theo spec 9.4, hoan tat milestone phai di qua approveMilestone (release 100% escrow) de dam bao dong tien dung.
    @Transactional
    public MilestoneEntity completeMilestone(Integer milestoneId) {
        return approveMilestone(milestoneId);
    }

    private void tryCompleteContract(ContractEntity contract, Integer actorAccountId) {
        if (!"ACTIVE".equals(contract.getStatus())) return;
        List<MilestoneEntity> milestones = milestoneRepository.findByContractIdOrderByOrderIndexAsc(contract.getContractId());
        if (milestones.isEmpty()) return;
        boolean allCompleted = milestones.stream().allMatch(milestone -> "COMPLETED".equals(milestone.getStatus()));
        if (!allCompleted) return;
        contract.setStatus("COMPLETED");
        contract.setUpdatedAt(LocalDateTime.now());
        ContractEntity saved = contractRepository.save(contract);
        closeCompletedContractJob(saved);
        auditLogService.record("CONTRACT_COMPLETED", "contracts", String.valueOf(saved.getContractId()), actorAccountId);
        notifyBothParticipants(saved, actorAccountId, "CONTRACT_COMPLETED", "Hợp đồng đã hoàn tất", "Tất cả milestone của hợp đồng đã hoàn thành.");
        paymentWalletService.autoRefundParticipantDeposits(saved.getContractId(), actorAccountId);
    }

    private void notifyCounterpartyContractEvent(ContractEntity contract, Integer actorAccountId, Integer expertId, Integer businessId, String type, String title, String message) {
        if (expertId != null) {
            expertProfileRepository.findById(expertId)
                    .ifPresent(expert -> notificationService.notifyContractEvent(expert.getAccountId(), actorAccountId, type, title, message, contract.getContractId()));
        }
        if (businessId != null) {
            businessProfileRepository.findById(businessId)
                    .ifPresent(business -> notificationService.notifyContractEvent(business.getAccountId(), actorAccountId, type, title, message, contract.getContractId()));
        }
    }

    private void notifyBothParticipants(ContractEntity contract, Integer actorAccountId, String type, String title, String message) {
        businessProfileRepository.findById(contract.getBusinessId())
                .ifPresent(business -> notificationService.notifyContractEvent(business.getAccountId(), actorAccountId, type, title, message, contract.getContractId()));
        expertProfileRepository.findById(contract.getExpertId())
                .ifPresent(expert -> notificationService.notifyContractEvent(expert.getAccountId(), actorAccountId, type, title, message, contract.getContractId()));
    }

    private void notifyBothParticipants(ContractEntity contract, Integer actorAccountId, java.util.function.BiConsumer<Integer, Integer> notifier) {
        businessProfileRepository.findById(contract.getBusinessId())
                .ifPresent(business -> notifier.accept(business.getAccountId(), actorAccountId));
        expertProfileRepository.findById(contract.getExpertId())
                .ifPresent(expert -> notifier.accept(expert.getAccountId(), actorAccountId));
    }

    // Note: Goi notifier cho tung ben tham gia hop dong (Business + Expert), bo qua chinh nguoi thao tac.
    private void notifyContractParticipantsExcept(ContractEntity contract, Integer actorAccountId, java.util.function.BiConsumer<Integer, Integer> notifier) {
        Integer businessAccountId = businessProfileRepository.findById(contract.getBusinessId()).map(BusinessProfileEntity::getAccountId).orElse(null);
        Integer expertAccountId = expertProfileRepository.findById(contract.getExpertId()).map(ExpertProfileEntity::getAccountId).orElse(null);
        for (Integer accountId : List.of(businessAccountId == null ? -1 : businessAccountId, expertAccountId == null ? -1 : expertAccountId)) {
            if (accountId > 0 && !accountId.equals(actorAccountId)) {
                notifier.accept(accountId, actorAccountId);
            }
        }
    }

    // Note: Goi notifier cho tat ca tai khoan ADMIN (dung cho escalation / termination request).
    private void notifyAllAdmins(Integer actorAccountId, java.util.function.BiConsumer<Integer, Integer> notifier) {
        accountRepository.findAllByRoleRoleNameOrderByAccountIdAsc("ADMIN")
                .forEach(admin -> notifier.accept(admin.getAccountId(), actorAccountId));
    }
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Transactional public TransactionEntity createTransaction(TransactionEntity input) {
        MilestoneEntity milestone = milestoneRepository.findById(input.getMilestoneId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        ContractEntity contract = contractRepository.findByJobId(milestone.getJobId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT CUA JOB"));
        AccountEntity actor = accessService.currentAccount();
        String role = actor.getRole().getRoleName();
        if ("BUSINESS".equals(role)) {
            accessService.requireApprovedAccount();
            requireBusinessOwnedContract(contract.getContractId());
            if (!"Deposit".equals(input.getTransactionType())) throw new AppException("BUSINESS CHI DUOC TAO GIAO DICH DEPOSIT");
        } else {
            accessService.requireRole("ADMIN");
        }
        if (input.getAmount() == null || input.getAmount().signum() < 0) throw new AppException("AMOUNT KHONG HOP LE");
        if (!List.of("Deposit", "Payout", "Refund").contains(input.getTransactionType())) throw new AppException("TRANSACTION TYPE KHONG HOP LE");
        if (input.getStatus() == null) input.setStatus("Pending");
        TransactionEntity saved = transactionRepository.save(input);
        systemWalletService.syncWallet();
        auditLogService.record(AuditLogService.ACTION_CREATE_TRANSACTION, "transactions", String.valueOf(saved.getTransactionId()), actor.getAccountId());
        return saved;
    }
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Transactional public DisputeEntity createDispute(DisputeEntity input) {
        requireApprovedForBusinessOrExpert();
        ContractEntity contract = contractRepository.findById(input.getContractId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer businessId = businessProfileRepository.findByAccountId(accountId).map(BusinessProfileEntity::getBusinessId).orElse(null);
        Integer expertId = expertProfileRepository.findByAccountId(accountId).map(ExpertProfileEntity::getExpertId).orElse(null);
        boolean isParticipant = (businessId != null && businessId.equals(contract.getBusinessId()))
                || (expertId != null && expertId.equals(contract.getExpertId()));
        if (!isParticipant) throw new AppException("BAN KHONG CO QUYEN TAO DISPUTE CHO CONTRACT NAY");
        // Spec 10.3 / invariant 5: moi milestone chi duoc co mot dispute dang hoat dong.
        if (input.getMilestoneId() != null && !activeDisputesForMilestone(input.getMilestoneId()).isEmpty()) {
            throw new AppException("MILESTONE DA CO DISPUTE DANG HOAT DONG");
        }
        // NEU BAT CAU HINH AUTO ASSIGN THI HE THONG TU GAN STAFF DAU TIEN KHA DUNG.
        if (input.getAssignedStaffId() == null && isAutoAssignStaffEnabled()) {
            staffRepository.findAll().stream().findFirst().ifPresent(staff -> input.setAssignedStaffId(staff.getStaffId()));
        }
        if (input.getStatus() == null) input.setStatus("Open");
        DisputeEntity saved = disputeRepository.save(input);
        systemWalletService.syncWallet();
        auditLogService.record(AuditLogService.ACTION_CREATE_DISPUTE, "disputes", String.valueOf(saved.getDisputeId()), accountId);
        if (saved.getAssignedStaffId() != null) {
            staffRepository.findById(saved.getAssignedStaffId())
                    .ifPresent(staff -> notificationService.notifyDisputeCreated(
                            staff.getAccountId(),
                            accountId,
                            saved.getDisputeId()
                    ));
        }
        return saved;
    }

    // Note: Hàm `listContracts` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<ContractEntity> listContracts() {
        AccountEntity actor = accessService.currentAccount();
        String role = actor.getRole().getRoleName();
        if ("ADMIN".equals(role)) return attachContractMilestones(contractRepository.findAll());
        if ("STAFF".equals(role)) {
            Integer staffId = staffRepository.findByAccountId(actor.getAccountId())
                    .map(StaffEntity::getStaffId)
                    .orElseThrow(() -> new NotFoundException("CHUA CO STAFF PROFILE"));
            return attachContractMilestones(disputeRepository.findByAssignedStaffId(staffId).stream()
                    .map(DisputeEntity::getContractId)
                    .distinct()
                    .map(contractRepository::findById)
                    .flatMap(Optional::stream)
                    .toList());
        }
        if ("BUSINESS".equals(role)) {
            accessService.requireApprovedAccount();
            Integer businessId = businessProfileRepository.findByAccountId(actor.getAccountId()).map(BusinessProfileEntity::getBusinessId).orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
            return attachContractMilestones(contractRepository.findByBusinessId(businessId));
        }
        if ("EXPERT".equals(role)) {
            accessService.requireApprovedAccount();
            Integer expertId = expertProfileRepository.findByAccountId(actor.getAccountId()).map(ExpertProfileEntity::getExpertId).orElseThrow(() -> new NotFoundException("CHUA CO EXPERT PROFILE"));
            return attachContractMilestones(contractRepository.findByExpertId(expertId));
        }
        throw new AppException("ROLE KHONG HOP LE");
    }

    // Note: Hàm `getContract` trả chi tiết contract kèm milestone đã chốt để hai bên xem trước khi ký.
    public ContractEntity getContract(Integer contractId) {
        ContractEntity contract = requireContractParticipantOrOperator(contractId);
        contract.setContractMilestones(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contractId));
        return contract;
    }

    // Note: Hàm `listMilestonesByContract` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<ContractMilestoneViewResponse> listMilestonesByContract(Integer contractId) {
        ContractEntity contract = requireContractParticipantOrOperator(contractId);
        List<ContractMilestoneEntity> contractMilestones = contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contract.getContractId());
        return contractMilestones.stream().map(cm -> {
            String liveStatus = milestoneRepository.findById(cm.getJobMilestoneId())
                    .map(MilestoneEntity::getStatus)
                    .orElse(cm.getStatus());
            Optional<MilestoneProgressReportRequestEntity> latestRequest =
                    milestoneProgressReportRequestRepository
                            .findFirstByContractIdAndMilestoneIdOrderByRequestNumberDesc(
                                    contractId, cm.getJobMilestoneId());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dueAt = milestoneDueAt(cm);
            return ContractMilestoneViewResponse.builder()
                    .contractMilestoneId(cm.getContractMilestoneId())
                    .contractId(cm.getContractId())
                    .jobMilestoneId(cm.getJobMilestoneId())
                    .milestoneName(cm.getMilestoneName())
                    .description(cm.getDescription())
                    .originalBudget(cm.getOriginalBudget())
                    .finalBudget(cm.getFinalBudget())
                    .orderIndex(cm.getOrderIndex())
                    .status(liveStatus)
                    .duration(cm.getDuration())
                    .durationUnit(cm.getDurationUnit())
                    .criteriaSnapshot(cm.getCriteriaSnapshot())
                    .deliverableExpectation(cm.getDeliverableExpectation())
                    .dueAt(dueAt)
                    .overdue(dueAt != null && now.isAfter(dueAt)
                            && !List.of(ContractMilestoneEntity.STATUS_COMPLETED, ContractMilestoneEntity.STATUS_CANCELLED)
                            .contains(liveStatus))
                    .progressReportRequestCount(latestRequest.map(MilestoneProgressReportRequestEntity::getRequestNumber).orElse(0))
                    .progressReportRequestedAt(latestRequest.map(MilestoneProgressReportRequestEntity::getRequestedAt).orElse(null))
                    .progressReportDueAt(latestRequest.map(MilestoneProgressReportRequestEntity::getDueAt).orElse(null))
                    .progressReportSubmittedAt(latestRequest.map(MilestoneProgressReportRequestEntity::getSubmittedAt).orElse(null))
                    .progressReportRequestPending(latestRequest.map(item -> MilestoneProgressReportRequestEntity.STATUS_PENDING.equals(item.getStatus())
                            && !item.getDueAt().isBefore(now)).orElse(false))
                    .progressReportRequestOverdue(latestRequest.map(item -> MilestoneProgressReportRequestEntity.STATUS_PENDING.equals(item.getStatus())
                            && item.getDueAt().isBefore(now)).orElse(false))
                    .rejectCount(cm.getRejectCount() == null ? 0 : cm.getRejectCount())
                    .lastRejectionFeedback(cm.getLastRejectionFeedback())
                    .createdAt(cm.getCreatedAt())
                    .updatedAt(cm.getUpdatedAt())
                    .build();
        }).toList();
    }

    // Note: Hàm `attachContractMilestones` gắn milestone chốt vào contract để API trả đủ dữ liệu hợp đồng.
    private List<ContractEntity> attachContractMilestones(List<ContractEntity> contracts) {
        contracts.forEach(contract -> contract.setContractMilestones(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contract.getContractId())));
        return contracts;
    }
    // Note: Hàm `listMilestonesByJob` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<MilestoneEntity> listMilestonesByJob(Integer jobId) {
        JobEntity job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        // Job OPEN cho chuyên gia xem milestone trước khi gửi proposal; job chưa public vẫn phải kiểm tra quyền sở hữu/tham gia.
        if (!"OPEN".equalsIgnoreCase(job.getStatus())) {
            requireJobParticipantOrOwner(jobId);
        }
        return attachCriteria(milestoneRepository.findByJobIdOrderByOrderIndexAsc(jobId));
    }
    // Note: Hàm `listCriteriaByMilestone` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<AcceptanceCriteriaEntity> listCriteriaByMilestone(Integer milestoneId) {
        MilestoneEntity milestone = milestoneRepository.findById(milestoneId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        requireJobParticipantOrOwner(milestone.getJobId());
        return criteriaRepository.findByMilestoneIdOrderBySortOrderAscCriteriaIdAsc(milestoneId);
    }
    // Note: Hàm `listDeliverablesByMilestone` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<DeliverableEntity> listDeliverablesByMilestone(Integer milestoneId) {
        MilestoneEntity milestone = milestoneRepository.findById(milestoneId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        requireJobParticipantOrOwner(milestone.getJobId());
        return deliverableRepository.findByMilestoneId(milestoneId);
    }
    // Note: Hàm `listTransactionsByMilestone` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<TransactionEntity> listTransactionsByMilestone(Integer milestoneId) {
        MilestoneEntity milestone = milestoneRepository.findById(milestoneId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        requireJobParticipantOrOwner(milestone.getJobId());
        return transactionRepository.findByMilestoneId(milestoneId);
    }
    // Note: Hàm `listDisputesByContract` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<DisputeEntity> listDisputesByContract(Integer contractId) {
        AccountEntity actor = accessService.currentAccount();
        requireContractParticipantOrOperator(contractId);
        if ("STAFF".equals(actor.getRole().getRoleName())) {
            Integer staffId = getCurrentStaffId(actor);
            return disputeRepository.findByContractId(contractId).stream()
                    .filter(d -> staffId.equals(d.getAssignedStaffId()))
                    .map(this::withStaffName)
                    .toList();
        }
        return disputeRepository.findByContractId(contractId).stream()
                .map(this::withStaffName)
                .toList();
    }
    // Note: Hàm `getDispute` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public DisputeEntity getDispute(Integer disputeId) {
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        AccountEntity actor = accessService.currentAccount();
        if ("STAFF".equals(actor.getRole().getRoleName())) {
            requireAssignedStaff(dispute);
        } else {
            requireContractParticipantOrOperator(dispute.getContractId());
        }
        return withStaffName(dispute);
    }
    // Note: Hàm `matchingByKeyword` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<ProposalEntity> matchingByKeyword(Integer jobId) {
        JobEntity job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        final String keyword = List.of(job.getTitle(), job.getStructuredSow(), job.getRawRequirements()).stream()
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse("AI")
                .toUpperCase();
        return proposalRepository.findByJobId(jobId).stream()
                .filter(p -> p.getTechnicalSolution() != null && p.getTechnicalSolution().toUpperCase().contains(keyword))
                .toList();
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `updateTransactionStatus` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public TransactionEntity updateTransactionStatus(Long transactionId, String status) {
        accessService.requireRole("ADMIN");
        // RANG BUOC TRANG THAI GIAO DICH THEO FLOW ESCROW.
        if (!List.of("Pending", "Success", "Failed").contains(status)) {
            throw new AppException("STATUS TRANSACTION KHONG HOP LE");
        }
        TransactionEntity transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY TRANSACTION"));
        transaction.setStatus(status);
        TransactionEntity saved = transactionRepository.save(transaction);
        systemWalletService.syncWallet();
        auditLogService.record(AuditLogService.ACTION_UPDATE_TRANSACTION_STATUS, "transactions", String.valueOf(transactionId), accessService.currentAccount().getAccountId());
        return saved;
    }

    private DisputeEntity withStaffName(DisputeEntity dispute) {
        if (dispute == null || dispute.getAssignedStaffId() == null) {
            return dispute;
        }
        Optional<StaffEntity> staff = staffRepository.findById(dispute.getAssignedStaffId());
        if (staff.isEmpty()) {
            return dispute;
        }
        Optional<AccountEntity> account = accountRepository.findById(staff.get().getAccountId());
        if (account.isEmpty()) {
            return dispute;
        }
        String staffName = account.get().getFullName();
        if (staffName != null && !staffName.isBlank()) {
            dispute.setStaffName(staffName);
        }
        return dispute;
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    public DisputeEntity routeDispute(Integer disputeId, Integer staffId) {
        accessService.requireRole("STAFF");
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        if (!DisputeEntity.STATUS_ESCALATION_REQUESTED.equals(dispute.getStatus())) {
            throw new AppException("DISPUTE_NOT_READY_FOR_STAFF_ROUTING");
        }
        List<Integer> jobDomainIds = resolveJobDomainIds(dispute.getContractId());
        if (jobDomainIds.isEmpty()) {
            throw new AppException("JOB KHONG CO DOMAIN, KHONG THE ROUTE STAFF");
        }
        if (staffId != null) {
            List<Integer> staffDomainIds = staffDomainRepository.findByIdStaffId(staffId).stream()
                    .map(sd -> sd.getId().getDomainId()).toList();
            boolean eligible = staffDomainIds.stream().anyMatch(jobDomainIds::contains);
            if (!eligible) {
                throw new AppException("STAFF KHONG CO DOMAIN TUONG UNG VOI JOB");
            }
        }
        Integer routedStaffId = staffId == null ? selectStaffForDispute(dispute) : staffId;
        return routeDisputeToStaff(dispute, routedStaffId, accessService.currentAccount().getAccountId());
    }

    private DisputeEntity routeDisputeToStaff(DisputeEntity dispute, Integer staffId, Integer actorAccountId) {
        staffRepository.findById(staffId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY STAFF"));
        dispute.setAssignedStaffId(staffId);
        dispute.setStatus(DisputeEntity.STATUS_STAFF_REVIEWING);
        LocalDateTime now = LocalDateTime.now();
        dispute.setStaffReviewStartedAt(now);
        dispute.setEvidenceCollectionDueAt(now.plusHours(48));
        dispute.setStaffAccessScope("READ_EXECUTE");
        dispute.setStaffAccessExpiresAt(now.plusHours(48).plusDays(3));
        dispute.setStaffSlaDueAt(now.plusHours(48).plusDays(3));
        DisputeEntity saved = disputeRepository.save(dispute);
        systemWalletService.syncWallet();
        auditLogService.record("DISPUTE_STAFF_ROUTED", "disputes", String.valueOf(dispute.getDisputeId()), actorAccountId);
        staffRepository.findById(staffId)
                .ifPresent(staff -> notificationService.notifyDisputeAssigned(
                        staff.getAccountId(),
                        actorAccountId,
                        dispute.getDisputeId()
                ));
        contractRepository.findById(saved.getContractId()).ifPresent(contract ->
                notifyContractParticipantsExcept(contract, actorAccountId,
                        (receiver, actor) -> notificationService.notifyDisputeUnderStaffReview(
                                receiver, actor, contract.getContractId(), dispute.getDisputeId())));
        return withStaffName(saved);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `resolveDispute` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public DisputeEntity resolveDispute(Integer disputeId, String proposedAction) {
        accessService.requireRole("ADMIN");
        // ADMIN CHOT PHUONG AN XU LY TRANH CHAP VA DONG CASE.
        if (proposedAction == null || proposedAction.isBlank()) throw new AppException("PROPOSED ACTION KHONG DUOC DE TRONG");
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        dispute.setProposedAction(proposedAction);
        dispute.setStatus("Resolved");
        DisputeEntity saved = disputeRepository.save(dispute);
        systemWalletService.syncWallet();
        auditLogService.record(AuditLogService.ACTION_RESOLVE_DISPUTE, "disputes", String.valueOf(disputeId), accessService.currentAccount().getAccountId());
        return withStaffName(saved);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `runSlaAutoApprove` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<MilestoneEntity> runSlaAutoApprove() {
        accessService.requireRole("ADMIN");
        // MO PHONG JOB SLA: TU DONG RELEASE MILESTONE NEU QUA SO NGAY CAU HINH SAU KHI CO DELIVERABLE.
        int slaDays = systemSettingRepository.findById("default_sla_days")
                .map(SystemSettingEntity::getSettingValue)
                .map(v -> {
                    try { return Integer.parseInt(v); } catch (Exception e) { return 7; }
                }).orElse(7);
        LocalDateTime now = LocalDateTime.now();
        List<MilestoneEntity> updated = new java.util.ArrayList<>();
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        for (MilestoneEntity milestone : milestoneRepository.findAll()) {
            if (!ContractMilestoneEntity.STATUS_UNDER_REVIEW.equals(milestone.getStatus())) continue;
            List<DeliverableEntity> deliverables = deliverableRepository.findByMilestoneId(milestone.getMilestoneId());
            if (deliverables.isEmpty()) continue;
            LocalDateTime lastSubmission = deliverables.stream()
                    .map(DeliverableEntity::getCreatedAt)
                    .filter(java.util.Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            if (lastSubmission == null) continue;
            if (!lastSubmission.plusDays(slaDays).isAfter(now)) {
                findContractForMilestone(milestone).ifPresent(contract -> {
                    if (!hasActiveDispute(contract.getContractId())
                            && activeTerminationRequests(contract.getContractId()).isEmpty()) {
                        updated.add(finalizeSlaApproval(milestone, contract, actorAccountId));
                    }
                });
            }
        }
        auditLogService.record(AuditLogService.ACTION_RUN_SLA_AUTO_APPROVE, "system_settings", "default_sla_days", actorAccountId);
        return updated;
    }

    @Transactional
    public List<DisputeEntity> escalateOverdueStaffDisputes() {
        accessService.requireRole("ADMIN");
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        LocalDateTime now = LocalDateTime.now();
        List<DisputeEntity> changed = new java.util.ArrayList<>();
        for (DisputeEntity dispute : disputeRepository.findAll()) {
            if (DisputeEntity.STATUS_STAFF_REVIEWING.equals(dispute.getStatus())
                    && dispute.getStaffSlaDueAt() != null
                    && !dispute.getStaffSlaDueAt().isAfter(now)
                    && dispute.getStaffSlaEscalatedAt() == null) {
                dispute.setStaffSlaEscalatedAt(now);
                DisputeEntity saved = disputeRepository.save(dispute);
                auditLogService.record("DISPUTE_STAFF_SLA_ESCALATED", "disputes",
                        String.valueOf(dispute.getDisputeId()), actorAccountId);
                notifyAllAdmins(actorAccountId, (receiver, actor) ->
                        notificationService.notifyDisputeStaffSlaEscalated(
                                receiver, actor, dispute.getContractId(), dispute.getDisputeId()));
                changed.add(saved);
            }
        }
        return changed;
    }

    private MilestoneEntity finalizeSlaApproval(
            MilestoneEntity milestone, ContractEntity contract, Integer actorAccountId) {
        ContractMilestoneEntity contractMilestone =
                findContractMilestoneForUpdate(contract.getContractId(), milestone.getMilestoneId());
        ensureEscrowNotReleased(contractMilestone);
        Integer businessAccountId = businessProfileRepository.findById(contract.getBusinessId())
                .map(BusinessProfileEntity::getAccountId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
        Integer expertAccountId = expertProfileRepository.findById(contract.getExpertId())
                .map(ExpertProfileEntity::getAccountId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY EXPERT PROFILE"));
        String metadata = walletMetadata("REVIEW_SLA_AUTO_APPROVAL", null, null, actorAccountId,
                businessAccountId, expertAccountId, BigDecimal.valueOf(100),
                contractMilestone.getFinalBudget(), BigDecimal.ZERO);
        walletLedgerService.debitEscrow(
                businessAccountId,
                contractMilestone.getFinalBudget(),
                WalletTransactionEntity.TX_ESCROW_RELEASE, "MILESTONE",
                milestone.getMilestoneId().longValue(), "Review SLA auto-approval escrow release",
                walletOperationContext(contract.getContractId(), milestone.getMilestoneId(), metadata,
                        "MILESTONE_ESCROW_RELEASE:" + contract.getContractId() + ":" + milestone.getMilestoneId() + ":REVIEW_SLA_AUTO_APPROVAL", null));
        walletLedgerService.creditAvailable(
                expertAccountId, contractMilestone.getFinalBudget(),
                WalletTransactionEntity.TX_ESCROW_RELEASE, "MILESTONE",
                milestone.getMilestoneId().longValue(), "Review SLA auto-approval payout",
                walletOperationContext(contract.getContractId(), milestone.getMilestoneId(), metadata,
                        "MILESTONE_ESCROW_RELEASE:" + contract.getContractId() + ":" + milestone.getMilestoneId() + ":REVIEW_SLA_AUTO_APPROVAL",
                        "EXPERT_AVAILABLE_CREDIT"));
        markEscrowReleased(contractMilestone, "REVIEW_SLA_AUTO_APPROVAL", milestone.getMilestoneId().longValue());
        contractMilestone.setStatus(ContractMilestoneEntity.STATUS_COMPLETED);
        contractMilestoneRepository.save(contractMilestone);
        milestone.setStatus(ContractMilestoneEntity.STATUS_COMPLETED);
        milestone.setEscrowReleasedAt(contractMilestone.getEscrowReleasedAt());
        milestone.setSettlementSourceType(contractMilestone.getSettlementSourceType());
        milestone.setSettlementSourceId(contractMilestone.getSettlementSourceId());
        milestone.setUpdatedAt(LocalDateTime.now());
        MilestoneEntity saved = milestoneRepository.save(milestone);
        auditLogService.record("MILESTONE_REVIEW_SLA_AUTO_APPROVED", "milestones",
                String.valueOf(milestone.getMilestoneId()), actorAccountId);
        tryCompleteContract(contract, actorAccountId);
        return saved;
    }

    private Optional<ContractEntity> findContractForMilestone(MilestoneEntity milestone) {
        if (milestone.getContractId() != null) {
            return contractRepository.findById(milestone.getContractId());
        }
        if (milestone.getJobId() != null) {
            return contractRepository.findByJobId(milestone.getJobId());
        }
        return Optional.empty();
    }

    private List<String> activeDisputeStatuses() {
        return List.of(
                DisputeEntity.STATUS_PENDING_SELF_RESOLVE,
                DisputeEntity.STATUS_ESCALATION_REQUESTED,
                DisputeEntity.STATUS_STAFF_REVIEWING,
                DisputeEntity.STATUS_STAFF_DECIDED
        );
    }

    private List<DisputeEntity> activeDisputesForMilestone(Integer milestoneId) {
        return disputeRepository.findByMilestoneIdAndStatusIn(milestoneId, activeDisputeStatuses());
    }

    private boolean hasActiveDispute(Integer contractId) {
        return disputeRepository.findByContractId(contractId).stream()
                .anyMatch(dispute -> activeDisputeStatuses().contains(dispute.getStatus()));
    }

    private void ensureEscrowNotReleased(ContractMilestoneEntity milestone) {
        if (milestone.getEscrowReleasedAt() != null) {
            throw new AppException("MILESTONE_ESCROW_DA_DUOC_RELEASE");
        }
    }

    private void markEscrowReleased(ContractMilestoneEntity milestone, String sourceType, Long sourceId) {
        milestone.setEscrowReleasedAt(LocalDateTime.now());
        milestone.setSettlementSourceType(sourceType);
        milestone.setSettlementSourceId(sourceId);
    }

    private String normalizeInitiator(String initiatedBy) {
        if (initiatedBy == null || initiatedBy.isBlank()) {
            String role = accessService.currentAccount().getRole().getRoleName();
            if ("BUSINESS".equals(role) || "EXPERT".equals(role)) return role;
            return "OTHER";
        }
        String normalized = initiatedBy.trim().toUpperCase();
        if (!List.of("BUSINESS", "EXPERT", "OTHER").contains(normalized)) {
            throw new AppException("INITIATED_BY KHONG HOP LE");
        }
        return normalized;
    }

    // Note: Chuan hoa loai khoi tao tranh chap theo spec 7.5. Mac dinh OTHER neu khong truyen.
    private String normalizeInitiationType(String initiationType) {
        if (initiationType == null || initiationType.isBlank()) {
            return DisputeEntity.INITIATION_OTHER;
        }
        String normalized = initiationType.trim().toUpperCase();
        if (!List.of(
                DisputeEntity.INITIATION_BUSINESS_REJECTED_DELIVERABLE,
                DisputeEntity.INITIATION_EXPERT_SCOPE_CONCERN,
                DisputeEntity.INITIATION_EXPERT_NO_REVIEW_RESPONSE,
                DisputeEntity.INITIATION_EXPERT_BAD_FAITH_REJECTION,
                DisputeEntity.INITIATION_OTHER).contains(normalized)) {
            throw new AppException("INITIATION_TYPE KHONG HOP LE");
        }
        return normalized;
    }

    private void resolveActiveDisputeByBusinessApproval(Integer milestoneId) {
        for (DisputeEntity dispute : activeDisputesForMilestone(milestoneId)) {
            if (!DisputeEntity.STATUS_PENDING_SELF_RESOLVE.equals(dispute.getStatus())) continue;
            dispute.setStatus(DisputeEntity.STATUS_RESOLVED);
            dispute.setResolutionType(DisputeEntity.RESOLUTION_BUSINESS_APPROVED_AFTER_SELF_RESOLVE);
            dispute.setResolvedAt(LocalDateTime.now());
            disputeRepository.save(dispute);
        }
    }

    private ContractMilestoneEntity findContractMilestone(Integer contractId, Integer milestoneId) {
        return contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contractId).stream()
                .filter(item -> milestoneId.equals(item.getJobMilestoneId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT MILESTONE"));
    }

    public List<StaffAssignmentCandidateResponse> listStaffCandidates(Integer disputeId) {
        accessService.requireRole("STAFF");
        DisputeEntity dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        return rankedStaffCandidates(dispute);
    }

    private List<StaffAssignmentCandidateResponse> rankedStaffCandidates(DisputeEntity dispute) {
        List<Integer> jobDomainIds = resolveJobDomainIds(dispute.getContractId());
        List<Integer> jobSkillIds = resolveJobSkillIds(dispute.getContractId());
        List<String> jobDomainNames = domainRepository.findAllById(jobDomainIds).stream()
                .map(DomainEntity::getDomainName).toList();
        List<String> jobSkillNames = skillRepository.findAllById(jobSkillIds).stream()
                .map(SkillEntity::getSkillName).toList();
        List<StaffAssignmentCandidateResponse> candidates = new java.util.ArrayList<>();
        for (StaffEntity staff : staffRepository.findAll()) {
            List<Integer> staffDomainIds = staffDomainRepository.findByIdStaffId(staff.getStaffId()).stream()
                    .map(sd -> sd.getId().getDomainId()).toList();
            List<Integer> staffSkillIds = staffSkillRepository.findByIdStaffId(staff.getStaffId()).stream()
                    .map(ss -> ss.getId().getSkillId()).toList();
            List<Integer> matchedDomainIds = staffDomainIds.stream().filter(jobDomainIds::contains).toList();
            if (matchedDomainIds.isEmpty()) continue;
            List<Integer> matchedSkillIds = staffSkillIds.stream().filter(jobSkillIds::contains).toList();
            List<String> matchedDomainNames = domainRepository.findAllById(matchedDomainIds).stream()
                    .map(DomainEntity::getDomainName).toList();
            List<String> matchedSkillNames = skillRepository.findAllById(matchedSkillIds).stream()
                    .map(SkillEntity::getSkillName).toList();
            int workload = (int) disputeRepository.findByAssignedStaffId(staff.getStaffId()).stream()
                    .filter(item -> activeDisputeStatuses().contains(item.getStatus())).count();
            String displayName = accountRepository.findById(staff.getAccountId())
                    .map(AccountEntity::getFullName).orElse("Staff " + staff.getStaffId());
            candidates.add(StaffAssignmentCandidateResponse.builder()
                    .staffId(staff.getStaffId()).displayName(displayName)
                    .specializationMatch(staff.getSpecialization())
                    .technologyMatchSummary(staff.getSpecialization())
                    .availability(workload == 0 ? "IDLE" : "BUSY")
                    .activeDisputeWorkloadCount(workload)
                    .conflictEligible(true)
                    .matchedDomains(matchedDomainNames)
                    .matchedSkills(matchedSkillNames)
                    .build());
        }
        candidates.sort(java.util.Comparator
                .<StaffAssignmentCandidateResponse>comparingInt(c -> c.getMatchedDomains().size()).reversed()
                .thenComparing(java.util.Comparator.comparingInt((StaffAssignmentCandidateResponse c) -> c.getMatchedSkills().size()).reversed())
                .thenComparingInt(StaffAssignmentCandidateResponse::getActiveDisputeWorkloadCount)
                .thenComparingInt(StaffAssignmentCandidateResponse::getStaffId));
        return candidates;
    }

    private Integer selectStaffForDispute(DisputeEntity dispute) {
        return rankedStaffCandidates(dispute).stream().findFirst()
                .map(StaffAssignmentCandidateResponse::getStaffId)
                .orElseThrow(() -> new AppException("NO_MATCHING_STAFF_FOR_JOB_DOMAIN"));
    }

    private List<Integer> resolveJobDomainIds(Integer contractId) {
        return contractRepository.findById(contractId)
                .map(contract -> jobDomainRepository.findByIdJobId(contract.getJobId()).stream()
                        .map(jd -> jd.getId().getDomainId()).toList())
                .orElse(List.of());
    }

    private List<Integer> resolveJobSkillIds(Integer contractId) {
        return contractRepository.findById(contractId)
                .map(contract -> jobSkillRepository.findByIdJobId(contract.getJobId()).stream()
                        .map(js -> js.getId().getSkillId()).toList())
                .orElse(List.of());
    }

    private ContractMilestoneEntity findContractMilestoneForUpdate(Integer contractId, Integer milestoneId) {
        return contractMilestoneRepository.findByContractIdAndJobMilestoneIdForUpdate(contractId, milestoneId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT MILESTONE"));
    }

    @Transactional
    public DisputeEntity escalateDispute(Integer disputeId, String reason, String evidenceFile) {
        requireApprovedForBusinessOrExpert();
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        if (!DisputeEntity.STATUS_PENDING_SELF_RESOLVE.equals(dispute.getStatus())) {
            throw new AppException("CHI DUOC ESCALATE KHI DISPUTE O TRANG THAI PENDING_SELF_RESOLVE");
        }
        requireContractParticipantOrOperator(dispute.getContractId());
        if (reason == null || reason.isBlank()) {
            throw new AppException("DISPUTE_ESCALATION_REASON_REQUIRED");
        }
        dispute.setEscalationReason(reason.trim());
        dispute.setEscalationEvidenceFile(evidenceFile == null || evidenceFile.isBlank() ? null : evidenceFile.trim());
        dispute.setEscalationRequestedByAccountId(accessService.currentAccount().getAccountId());
        dispute.setEscalationRequestedAt(LocalDateTime.now());
        dispute.setStatus(DisputeEntity.STATUS_ESCALATION_REQUESTED);
        disputeRepository.save(dispute);
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record("DISPUTE_ESCALATION_REQUESTED", "disputes", String.valueOf(disputeId), actorAccountId);
        Integer staffId = selectStaffForDispute(dispute);
        staffRepository.findById(staffId).ifPresent(staff ->
                notificationService.notifyDisputeEscalationRequested(staff.getAccountId(), actorAccountId, disputeId));
        return routeDisputeToStaff(dispute, staffId, actorAccountId);
    }

    @Transactional
    public DisputeEntity staffDecide(Integer disputeId, Integer expertPercent, String note, String staffReport) {
        accessService.requireRole("STAFF");
        if (expertPercent == null || expertPercent < 0 || expertPercent > 100) {
            throw new AppException("EXPERT PERCENT PHAI TU 0 DEN 100");
        }
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        requireAssignedStaff(dispute);
        if (!DisputeEntity.STATUS_STAFF_REVIEWING.equals(dispute.getStatus())) {
            throw new AppException("CHI DUOC RA QUYET DINH KHI DISPUTE O TRANG THAI STAFF_REVIEWING");
        }
        if (dispute.getMilestoneId() == null) throw new AppException("DISPUTE CHUA GAN MILESTONE");
        // Spec 10.9: tinh truoc so tien de xuat cho Expert va so hoan lai cho Business tu escrow milestone.
        ContractEntity contract = contractRepository.findById(dispute.getContractId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        ContractMilestoneEntity contractMilestone = findContractMilestoneForUpdate(contract.getContractId(), dispute.getMilestoneId());
        BigDecimal escrowAmount = contractMilestone.getFinalBudget();
        BigDecimal expertPayout = escrowAmount.multiply(BigDecimal.valueOf(expertPercent)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal businessRefund = escrowAmount.subtract(expertPayout);
        dispute.setStaffDecisionPercentage(expertPercent);
        dispute.setStaffDecisionNote(note);
        dispute.setStaffReport(staffReport == null || staffReport.isBlank() ? null : staffReport.trim());
        dispute.setStaffProposedExpertAmount(expertPayout);
        dispute.setBusinessRefundAmount(businessRefund);
        dispute.setStaffDecidedAt(LocalDateTime.now());
        dispute.setStatus(DisputeEntity.STATUS_STAFF_DECIDED);
        dispute.setAdminApprovedBy(accessService.currentAccount().getAccountId());
        DisputeEntity saved = disputeRepository.save(dispute);
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record("DISPUTE_STAFF_DECIDED", "disputes", String.valueOf(disputeId), actorAccountId);
        notifyContractParticipantsExcept(contract, actorAccountId,
                (receiver, actor) -> notificationService.notifyDisputeStaffDecided(receiver, actor, contract.getContractId(), disputeId));
        return executeDisputeSettlementInternal(saved, actorAccountId);
    }

    @Transactional
    public TerminationRequestEntity acceptBusinessTermination(Long terminationRequestId) {
        accessService.requireRole("EXPERT");
        TerminationRequestEntity request = requireTerminationRequest(terminationRequestId);
        ContractEntity contract = requireExpertOwnedContract(request.getContractId());
        if (!TerminationRequestEntity.STATUS_AWAITING_EXPERT_RESPONSE.equals(request.getStatus())) {
            throw new AppException("TERMINATION_RESPONSE_NOT_ALLOWED");
        }
        return completeAcceptedBusinessTermination(request, contract, "TERMINATION_ACCEPTED_BY_EXPERT");
    }

    @Transactional
    public TerminationRequestEntity disputeBusinessTermination(Long terminationRequestId, String reason) {
        accessService.requireRole("EXPERT");
        TerminationRequestEntity request = requireTerminationRequest(terminationRequestId);
        ContractEntity contract = requireExpertOwnedContract(request.getContractId());
        if (!TerminationRequestEntity.STATUS_AWAITING_EXPERT_RESPONSE.equals(request.getStatus())) {
            throw new AppException("TERMINATION_RESPONSE_NOT_ALLOWED");
        }
        request.setStatus(TerminationRequestEntity.STATUS_REQUESTED);
        request.setExpertRespondedAt(LocalDateTime.now());
        if (reason != null && !reason.isBlank()) request.setStaffDecisionReason(reason.trim());
        TerminationRequestEntity saved = terminationRequestRepository.save(request);
        auditLogService.record("TERMINATION_DISPUTED_BY_EXPERT", "termination_requests",
                String.valueOf(terminationRequestId), accessService.currentAccount().getAccountId());
        notifyAllAdmins(accessService.currentAccount().getAccountId(),
                (receiver, actor) -> notificationService.notifyTerminationRequested(
                        receiver, actor, contract.getContractId(), terminationRequestId));
        return saved;
    }

    @Transactional
    public List<TerminationRequestEntity> expireAwaitingExpertTerminationResponses() {
        accessService.requireRole("ADMIN");
        LocalDateTime now = LocalDateTime.now();
        List<TerminationRequestEntity> changed = new java.util.ArrayList<>();
        for (TerminationRequestEntity request : terminationRequestRepository.findAll()) {
            if (TerminationRequestEntity.STATUS_AWAITING_EXPERT_RESPONSE.equals(request.getStatus())
                    && request.getExpertResponseDueAt() != null && !request.getExpertResponseDueAt().isAfter(now)) {
                ContractEntity contract = contractRepository.findById(request.getContractId())
                        .orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
                changed.add(completeAcceptedBusinessTermination(request, contract, "TERMINATION_RESPONSE_EXPIRED"));
            }
        }
        return changed;
    }

    private TerminationRequestEntity completeAcceptedBusinessTermination(
            TerminationRequestEntity request, ContractEntity contract, String auditAction) {
        if (hasActiveDispute(contract.getContractId())) {
            throw new AppException("ACTIVE_DISPUTE_BLOCKS_TERMINATION_SETTLEMENT");
        }
        Integer businessAccountId = businessProfileRepository.findById(contract.getBusinessId())
                .map(BusinessProfileEntity::getAccountId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
        for (ContractMilestoneEntity item : contractMilestoneRepository
                .findByContractIdOrderByOrderIndexAsc(contract.getContractId())) {
            if (ContractMilestoneEntity.STATUS_COMPLETED.equals(item.getStatus())) continue;
            if (List.of(ContractMilestoneEntity.STATUS_DEPOSITED, ContractMilestoneEntity.STATUS_IN_PROGRESS,
                    ContractMilestoneEntity.STATUS_OVERDUE).contains(item.getStatus())
                    && item.getEscrowReleasedAt() == null) {
                walletLedgerService.releaseEscrowToAvailable(
                        businessAccountId, item.getFinalBudget(), WalletTransactionEntity.TX_ESCROW_REFUND,
                        "TERMINATION_REQUEST", request.getTerminationRequestId(), "Accepted termination escrow refund",
                        walletOperationContext(
                                contract.getContractId(),
                                item.getJobMilestoneId(),
                                walletMetadata("TERMINATION", null, request.getTerminationRequestId(),
                                        accessService.currentAccount().getAccountId(), businessAccountId, null,
                                        BigDecimal.ZERO, BigDecimal.ZERO, item.getFinalBudget()),
                                "MILESTONE_ESCROW_REFUND:" + contract.getContractId() + ":" + item.getJobMilestoneId() + ":TERMINATION",
                                null));
                markEscrowReleased(item, "TERMINATION", request.getTerminationRequestId());
            }
            item.setStatus(ContractMilestoneEntity.STATUS_CANCELLED);
            contractMilestoneRepository.save(item);
            syncLiveMilestoneFromContractMilestone(item);
        }
        request.setExpertRespondedAt(LocalDateTime.now());
        request.setStatus(TerminationRequestEntity.STATUS_AWAITING_DEPOSIT_REFUND);
        contract.setStatus(ContractEntity.STATUS_TERMINATED);
        contract.setTerminatedAt(LocalDateTime.now());
        contractRepository.save(contract);
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record(auditAction, "termination_requests",
                String.valueOf(request.getTerminationRequestId()), actorAccountId);
        paymentWalletService.autoRefundParticipantDeposits(contract.getContractId(), actorAccountId);
        request.setDepositRefundedAt(LocalDateTime.now());
        request.setStatus(TerminationRequestEntity.STATUS_COMPLETED);
        TerminationRequestEntity saved = terminationRequestRepository.save(request);
        notifyBothParticipants(contract, actorAccountId,
                (receiver, actor) -> notificationService.notifyTerminationAcceptedOrExpired(
                        receiver, actor, contract.getContractId(), saved.getTerminationRequestId(), auditAction));
        return saved;
    }

    @Transactional
    public ContractEntity immediateTerminate(Integer contractId, ImmediateTerminationRequest input) {
        requireApprovedForBusinessOrExpert();
        if (input == null || !Boolean.TRUE.equals(input.getConfirmedPenalty())
                || input.getReason() == null || input.getReason().isBlank()) {
            throw new AppException("IMMEDIATE_TERMINATION_NOT_ALLOWED");
        }
        AccountEntity actor = accessService.currentAccount();
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        requireParticipant(contract, actor);
        if (!ContractEntity.STATUS_ACTIVE.equals(contract.getStatus())
                || !activeTerminationRequests(contractId).isEmpty()
                || hasActiveDispute(contractId)
                || contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contractId).stream()
                .anyMatch(item -> List.of(ContractMilestoneEntity.STATUS_UNDER_REVIEW,
                        ContractMilestoneEntity.STATUS_DISPUTED).contains(item.getStatus()))) {
            throw new AppException("IMMEDIATE_TERMINATION_NOT_ALLOWED");
        }
        contract.setTerminationReason(input.getReason().trim());
        ContractEntity saved = paymentWalletService.immediateTerminateContract(
                contract, actor, actor.getRole().getRoleName());
        auditLogService.record("CONTRACT_IMMEDIATE_TERMINATED", "contracts",
                String.valueOf(contractId), actor.getAccountId());
        notifyBothParticipants(saved, actor.getAccountId(), "CONTRACT_IMMEDIATE_TERMINATED",
                "Hợp đồng đã chấm dứt ngay", "Hợp đồng đã được chấm dứt với khoản bồi thường 10%.");
        return saved;
    }

    @Transactional
    public DisputeEntity executeDisputeSettlement(Integer disputeId) {
        accessService.requireRole("ADMIN");
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        return executeDisputeSettlementInternal(dispute, accessService.currentAccount().getAccountId());
    }

    private DisputeEntity executeDisputeSettlementInternal(DisputeEntity dispute, Integer actorAccountId) {
        Integer disputeId = dispute.getDisputeId();
        if (!DisputeEntity.STATUS_STAFF_DECIDED.equals(dispute.getStatus())) {
            throw new AppException("DISPUTE_NOT_STAFF_DECIDED");
        }
        if (dispute.getStaffDecisionPercentage() == null) throw new AppException("DISPUTE CHUA CO TY LE SETTLEMENT");
        ContractEntity contract = contractRepository.findById(dispute.getContractId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        if (dispute.getMilestoneId() == null) throw new AppException("DISPUTE CHUA GAN MILESTONE");
        ContractMilestoneEntity contractMilestone = findContractMilestone(contract.getContractId(), dispute.getMilestoneId());
        if (!ContractMilestoneEntity.STATUS_DISPUTED.equals(contractMilestone.getStatus())) {
            throw new AppException("MILESTONE KHONG O TRANG THAI DISPUTED DE SETTLEMENT");
        }
        ensureEscrowNotReleased(contractMilestone);
        BigDecimal escrowAmount = contractMilestone.getFinalBudget();
        BigDecimal expertPayout = escrowAmount.multiply(BigDecimal.valueOf(dispute.getStaffDecisionPercentage())).divide(BigDecimal.valueOf(100));
        BigDecimal businessRefund = escrowAmount.subtract(expertPayout);
        Integer businessAccountId = businessProfileRepository.findById(contract.getBusinessId()).map(BusinessProfileEntity::getAccountId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
        Integer expertAccountId = expertProfileRepository.findById(contract.getExpertId()).map(ExpertProfileEntity::getAccountId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY EXPERT PROFILE"));
        String metadata = walletMetadata("DISPUTE", disputeId, null, actorAccountId, businessAccountId, expertAccountId, BigDecimal.valueOf(dispute.getStaffDecisionPercentage()), expertPayout, businessRefund);
        WalletTransactionEntity settlementDebit = walletLedgerService.debitEscrow(
                businessAccountId, escrowAmount, WalletTransactionEntity.TX_ESCROW_SETTLEMENT_PAYOUT,
                "DISPUTE", disputeId.longValue(), "Dispute settlement debit",
                walletOperationContext(contract.getContractId(), dispute.getMilestoneId(), metadata,
                        "DISPUTE_SETTLEMENT:" + disputeId, null));
        if (expertPayout.signum() > 0) {
            walletLedgerService.creditAvailable(
                    expertAccountId, expertPayout, WalletTransactionEntity.TX_ESCROW_SETTLEMENT_PAYOUT,
                    "DISPUTE", disputeId.longValue(), "Dispute expert payout",
                    walletOperationContext(contract.getContractId(), dispute.getMilestoneId(), metadata,
                            "DISPUTE_SETTLEMENT:" + disputeId, "EXPERT_AVAILABLE_CREDIT"));
        }
        if (businessRefund.signum() > 0) {
            walletLedgerService.creditAvailable(
                    businessAccountId, businessRefund, WalletTransactionEntity.TX_ESCROW_SETTLEMENT_REFUND,
                    "DISPUTE", disputeId.longValue(), "Dispute business refund",
                    walletOperationContext(contract.getContractId(), dispute.getMilestoneId(), metadata,
                            "DISPUTE_SETTLEMENT:" + disputeId, "BUSINESS_AVAILABLE_CREDIT"));
        }
        markEscrowReleased(contractMilestone, "DISPUTE", disputeId.longValue());
        milestoneRepository.findById(dispute.getMilestoneId()).ifPresent(milestone -> {
            milestone.setStatus(ContractMilestoneEntity.STATUS_COMPLETED);
            milestone.setEscrowReleasedAt(contractMilestone.getEscrowReleasedAt());
            milestone.setSettlementSourceType(contractMilestone.getSettlementSourceType());
            milestone.setSettlementSourceId(contractMilestone.getSettlementSourceId());
            milestone.setResolvedByDisputeId(disputeId);
            milestone.setUpdatedAt(LocalDateTime.now());
            milestoneRepository.save(milestone);
        });
        contractMilestone.setStatus(ContractMilestoneEntity.STATUS_COMPLETED);
        contractMilestoneRepository.save(contractMilestone);
        dispute.setStatus(DisputeEntity.STATUS_RESOLVED);
        dispute.setResolutionType(DisputeEntity.RESOLUTION_STAFF_DECISION_SETTLEMENT);
        dispute.setResolvedAt(LocalDateTime.now());
        dispute.setSettlementExecutedAt(LocalDateTime.now());
        dispute.setSettlementWalletTransactionId(settlementDebit == null ? null : settlementDebit.getId());
        DisputeEntity saved = disputeRepository.save(dispute);
        auditLogService.record("DISPUTE_SETTLEMENT_EXECUTED", "disputes", String.valueOf(disputeId), actorAccountId);
        notifyContractParticipantsExcept(contract, actorAccountId,
                (receiver, actor) -> notificationService.notifyDisputeResolved(receiver, actor, contract.getContractId(), disputeId));
        List<Integer> adminAccountIds = accountRepository.findAllByRoleRoleNameOrderByAccountIdAsc("ADMIN")
                .stream()
                .map(AccountEntity::getAccountId)
                .toList();
        applicationEventPublisher.publishEvent(new DisputeSettlementCompletedEvent(
                actorAccountId,
                adminAccountIds,
                disputeId,
                contract.getContractId(),
                dispute.getMilestoneId(),
                dispute.getStaffDecisionPercentage(),
                expertPayout,
                businessRefund,
                settlementDebit == null ? null : settlementDebit.getId()));
        tryCompleteContract(contract, actorAccountId);
        return saved;
    }

    @Transactional
    public DisputeEntity cancelDispute(Integer disputeId, String reason) {
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        AccountEntity actor = accessService.currentAccount();
        String role = actor.getRole().getRoleName();
        requireApprovedForBusinessOrExpert();
        ContractEntity contract = requireContractParticipantOrOperator(dispute.getContractId());
        boolean requester = role.equals(dispute.getInitiatedBy());
        if (!requester || !isParticipant(contract, actor)) {
            throw new AppException("ONLY_DISPUTE_INITIATOR_CAN_WITHDRAW");
        }
        if (!List.of(DisputeEntity.STATUS_PENDING_SELF_RESOLVE, DisputeEntity.STATUS_ESCALATION_REQUESTED).contains(dispute.getStatus())
                || dispute.getAssignedStaffId() != null) {
            throw new AppException("DISPUTE_WITHDRAWAL_NOT_ALLOWED_AFTER_STAFF_ROUTING");
        }
        if (!activeDisputeStatuses().contains(dispute.getStatus())) {
            throw new AppException("DISPUTE KHONG O TRANG THAI CO THE HUY");
        }
        if (dispute.getMilestoneId() != null) {
            String restoredStatus = dispute.getPreviousMilestoneStatus() == null ? ContractMilestoneEntity.STATUS_IN_PROGRESS : dispute.getPreviousMilestoneStatus();
            milestoneRepository.findById(dispute.getMilestoneId()).ifPresent(milestone -> {
                milestone.setStatus(restoredStatus);
                milestone.setUpdatedAt(LocalDateTime.now());
                milestoneRepository.save(milestone);
            });
            contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(dispute.getContractId()).stream()
                    .filter(item -> dispute.getMilestoneId().equals(item.getJobMilestoneId()))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setStatus(restoredStatus);
                        contractMilestoneRepository.save(item);
                    });
        }
        dispute.setStatus(DisputeEntity.STATUS_CANCELLED);
        dispute.setResolutionType(DisputeEntity.RESOLUTION_CANCELLED_BY_INITIATOR);
        dispute.setCancellationReason(reason == null || reason.isBlank() ? null : reason.trim());
        dispute.setCancelledByAccountId(actor.getAccountId());
        dispute.setCancelledAt(LocalDateTime.now());
        DisputeEntity saved = disputeRepository.save(dispute);
        auditLogService.record("DISPUTE_CANCELLED", "disputes", String.valueOf(disputeId), actor.getAccountId());
        contractRepository.findById(dispute.getContractId()).ifPresent(disputeContract ->
                notifyContractParticipantsExcept(disputeContract, actor.getAccountId(),
                        (receiver, actorId) -> notificationService.notifyDisputeCancelled(
                                receiver, actorId, disputeContract.getContractId(), disputeId)));
        return saved;
    }

    @Transactional
    public TerminationRequestEntity assignTerminationStaff(Long terminationRequestId, Integer staffId) {
        accessService.requireRole("ADMIN");
        TerminationRequestEntity request = requireTerminationRequest(terminationRequestId);
        if (!TerminationRequestEntity.STATUS_REQUESTED.equals(request.getStatus())) {
            throw new AppException("YEU CAU CHAM DUT KHONG O TRANG THAI REQUESTED");
        }
        staffRepository.findById(staffId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY STAFF"));
        request.setAssignedStaffId(staffId);
        request.setStatus(TerminationRequestEntity.STATUS_STAFF_REVIEWING);
        request.setStaffReviewStartedAt(LocalDateTime.now());
        TerminationRequestEntity saved = terminationRequestRepository.save(request);
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record("TERMINATION_STAFF_ASSIGNED", "termination_requests", String.valueOf(terminationRequestId), actorAccountId);
        staffRepository.findById(staffId).ifPresent(staff ->
                notificationService.notifyTerminationStaffAssigned(staff.getAccountId(), actorAccountId, terminationRequestId));
        return saved;
    }

    @Transactional
    public TerminationRequestEntity rejectTermination(Long terminationRequestId, String reason) {
        accessService.requireRole("STAFF");
        TerminationRequestEntity request = requireTerminationRequest(terminationRequestId);
        requireAssignedTerminationStaff(request);
        if (!TerminationRequestEntity.STATUS_STAFF_REVIEWING.equals(request.getStatus())) {
            throw new AppException("YEU CAU CHAM DUT KHONG O TRANG THAI STAFF_REVIEWING");
        }
        ContractEntity contract = contractRepository.findById(request.getContractId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        request.setStatus(TerminationRequestEntity.STATUS_STAFF_REJECTED);
        request.setStaffDecisionReason(reason);
        request.setStaffDecidedAt(LocalDateTime.now());
        contract.setStatus(ContractEntity.STATUS_ACTIVE);
        contract.setUpdatedAt(LocalDateTime.now());
        contractRepository.save(contract);
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record("TERMINATION_STAFF_REJECTED", "termination_requests", String.valueOf(terminationRequestId), actorAccountId);
        notifyContractParticipantsExcept(contract, actorAccountId,
                (receiver, actor) -> notificationService.notifyTerminationReviewOutcome(receiver, actor, contract.getContractId(), terminationRequestId, false));
        return terminationRequestRepository.save(request);
    }

    @Transactional
    public TerminationRequestEntity approveTermination(Long terminationRequestId, TerminationRequestEntity input) {
        accessService.requireRole("STAFF");
        TerminationRequestEntity request = requireTerminationRequest(terminationRequestId);
        requireAssignedTerminationStaff(request);
        if (!TerminationRequestEntity.STATUS_STAFF_REVIEWING.equals(request.getStatus())) {
            throw new AppException("YEU CAU CHAM DUT KHONG O TRANG THAI STAFF_REVIEWING");
        }
        BigDecimal percent = input == null || input.getExpertPayoutPercentage() == null ? BigDecimal.ZERO : input.getExpertPayoutPercentage();
        if (percent.compareTo(BigDecimal.ZERO) < 0 || percent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new AppException("TY LE THANH TOAN CHO EXPERT PHAI TU 0 DEN 100");
        }
        request.setExpertPayoutPercentage(percent);
        request.setStaffDecisionReason(input == null ? null : input.getStaffDecisionReason());
        request.setStaffReport(input == null ? null : input.getStaffReport());
        request.setPartialEvidenceRequired(input != null && Boolean.TRUE.equals(input.getPartialEvidenceRequired()));
        request.setStaffDecidedAt(LocalDateTime.now());
        request.setStatus(terminationNeedsSettlement(request) ? TerminationRequestEntity.STATUS_AWAITING_SETTLEMENT_EXECUTION : TerminationRequestEntity.STATUS_AWAITING_DEPOSIT_REFUND);
        ContractEntity contract = contractRepository.findById(request.getContractId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        contract.setStatus(ContractEntity.STATUS_TERMINATION_PENDING);
        contract.setTerminationNote(request.getStaffDecisionReason());
        contract.setUpdatedAt(LocalDateTime.now());
        contractRepository.save(contract);
        if (request.getStaffReport() != null && !request.getStaffReport().isBlank()) {
            saveAttachmentIfPresent(CaseAttachmentEntity.OWNER_STAFF_REPORT, terminationRequestId, input == null ? null : input.getRequestFileUrl(), null, null, request.getStaffReport());
        }
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record("TERMINATION_STAFF_APPROVED", "termination_requests", String.valueOf(terminationRequestId), actorAccountId);
        notifyContractParticipantsExcept(contract, actorAccountId,
                (receiver, actor) -> notificationService.notifyTerminationReviewOutcome(receiver, actor, contract.getContractId(), terminationRequestId, true));
        return terminationRequestRepository.save(request);
    }

    @Transactional
    public TerminationRequestEntity submitPartialEvidence(Long terminationRequestId, TerminationRequestEntity input) {
        accessService.requireRole("EXPERT");
        accessService.requireApprovedAccount();
        TerminationRequestEntity request = requireTerminationRequest(terminationRequestId);
        ContractEntity contract = requireExpertOwnedContract(request.getContractId());
        if (!isParticipant(contract, accessService.currentAccount())) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        if (!List.of(TerminationRequestEntity.STATUS_STAFF_REVIEWING, TerminationRequestEntity.STATUS_AWAITING_SETTLEMENT_EXECUTION).contains(request.getStatus())) {
            throw new AppException("YEU CAU CHAM DUT KHONG NHAN PARTIAL EVIDENCE O TRANG THAI HIEN TAI");
        }
        if (input == null || input.getPartialEvidenceUrl() == null || input.getPartialEvidenceUrl().isBlank()) {
            throw new AppException("PARTIAL EVIDENCE URL KHONG DUOC DE TRONG");
        }
        request.setPartialEvidenceUrl(input.getPartialEvidenceUrl());
        request.setPartialEvidenceNote(input.getPartialEvidenceNote());
        request.setPartialEvidenceSubmittedAt(LocalDateTime.now());
        TerminationRequestEntity saved = terminationRequestRepository.save(request);
        saveAttachmentIfPresent(CaseAttachmentEntity.OWNER_PARTIAL_EVIDENCE, terminationRequestId, input.getPartialEvidenceUrl(), null, null, input.getPartialEvidenceNote());
        auditLogService.record("TERMINATION_PARTIAL_EVIDENCE_SUBMITTED", "termination_requests", String.valueOf(terminationRequestId), accessService.currentAccount().getAccountId());
        return saved;
    }

    @Transactional
    public TerminationRequestEntity executeTerminationSettlement(Long terminationRequestId) {
        accessService.requireRole("ADMIN");
        TerminationRequestEntity request = requireTerminationRequest(terminationRequestId);
        if (!List.of(TerminationRequestEntity.STATUS_STAFF_APPROVED, TerminationRequestEntity.STATUS_AWAITING_SETTLEMENT_EXECUTION).contains(request.getStatus())) {
            throw new AppException("YEU CAU CHAM DUT CHUA SAN SANG SETTLEMENT");
        }
        if (Boolean.TRUE.equals(request.getPartialEvidenceRequired())
                && (request.getPartialEvidenceSubmittedAt() == null || request.getPartialEvidenceUrl() == null || request.getPartialEvidenceUrl().isBlank())) {
            throw new AppException("CAN NOP PARTIAL EVIDENCE TRUOC KHI SETTLEMENT");
        }
        ContractEntity contract = contractRepository.findById(request.getContractId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        if (hasActiveDispute(contract.getContractId())) {
            throw new AppException("KHONG THE SETTLE TERMINATION KHI CONTRACT CON DISPUTE DANG HOAT DONG");
        }
        Integer businessAccountId = businessProfileRepository.findById(contract.getBusinessId()).map(BusinessProfileEntity::getAccountId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
        Integer expertAccountId = expertProfileRepository.findById(contract.getExpertId()).map(ExpertProfileEntity::getAccountId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY EXPERT PROFILE"));
        BigDecimal expertPercent = request.getExpertPayoutPercentage() == null ? BigDecimal.ZERO : request.getExpertPayoutPercentage();
        BigDecimal expertPayout = BigDecimal.ZERO;
        BigDecimal businessRefund = BigDecimal.ZERO;
        ContractMilestoneEntity current = request.getCurrentMilestoneId() == null ? null : findContractMilestoneForUpdate(contract.getContractId(), request.getCurrentMilestoneId());
        if (current != null && List.of(ContractMilestoneEntity.STATUS_DEPOSITED, ContractMilestoneEntity.STATUS_IN_PROGRESS, ContractMilestoneEntity.STATUS_UNDER_REVIEW, ContractMilestoneEntity.STATUS_DISPUTED).contains(current.getStatus())) {
            ensureEscrowNotReleased(current);
            BigDecimal escrowAmount = current.getFinalBudget();
            expertPayout = escrowAmount.multiply(expertPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            businessRefund = escrowAmount.subtract(expertPayout);
            String metadata = walletMetadata("TERMINATION_REQUEST", null, terminationRequestId, accessService.currentAccount().getAccountId(), businessAccountId, expertAccountId, expertPercent, expertPayout, businessRefund);
            WalletTransactionEntity settlementDebit = walletLedgerService.debitEscrow(
                    businessAccountId, escrowAmount, WalletTransactionEntity.TX_ESCROW_SETTLEMENT_PAYOUT,
                    "TERMINATION_REQUEST", terminationRequestId, "Termination settlement debit",
                    walletOperationContext(contract.getContractId(), current.getJobMilestoneId(), metadata,
                            "TERMINATION_SETTLEMENT:" + terminationRequestId + ":" + current.getJobMilestoneId(), null));
            if (expertPayout.signum() > 0) {
                walletLedgerService.creditAvailable(
                        expertAccountId, expertPayout, WalletTransactionEntity.TX_ESCROW_SETTLEMENT_PAYOUT,
                        "TERMINATION_REQUEST", terminationRequestId, "Termination expert payout",
                        walletOperationContext(contract.getContractId(), current.getJobMilestoneId(), metadata,
                                "TERMINATION_SETTLEMENT:" + terminationRequestId + ":" + current.getJobMilestoneId(),
                                "EXPERT_AVAILABLE_CREDIT"));
            }
            if (businessRefund.signum() > 0) {
                walletLedgerService.creditAvailable(
                        businessAccountId, businessRefund, WalletTransactionEntity.TX_ESCROW_SETTLEMENT_REFUND,
                        "TERMINATION_REQUEST", terminationRequestId, "Termination business refund",
                        walletOperationContext(contract.getContractId(), current.getJobMilestoneId(), metadata,
                                "TERMINATION_SETTLEMENT:" + terminationRequestId + ":" + current.getJobMilestoneId(),
                                "BUSINESS_AVAILABLE_CREDIT"));
            }
            markEscrowReleased(current, "TERMINATION_REQUEST", terminationRequestId);
            current.setStatus(expertPayout.signum() > 0 ? ContractMilestoneEntity.STATUS_COMPLETED : ContractMilestoneEntity.STATUS_CANCELLED);
            contractMilestoneRepository.save(current);
            request.setSettlementWalletTransactionId(settlementDebit == null ? null : settlementDebit.getId());
            syncLiveMilestoneFromContractMilestone(current);
            // Spec 11.7 step 13: ghi nguon settlement termination len live milestone.
            final Long resolvedTerminationId = terminationRequestId;
            milestoneRepository.findById(current.getJobMilestoneId()).ifPresent(liveMilestone -> {
                liveMilestone.setResolvedByTerminationRequestId(resolvedTerminationId);
                milestoneRepository.save(liveMilestone);
            });
        }
        cancelRemainingMilestones(contract.getContractId(), request.getCurrentMilestoneId());
        contract.setStatus(ContractEntity.STATUS_TERMINATED);
        contract.setTerminatedAt(LocalDateTime.now());
        contract.setUpdatedAt(LocalDateTime.now());
        contractRepository.save(contract);
        request.setExpertPayoutAmount(expertPayout);
        request.setBusinessRefundAmount(businessRefund);
        request.setSettlementExecutedAt(LocalDateTime.now());
        request.setStatus(TerminationRequestEntity.STATUS_AWAITING_DEPOSIT_REFUND);
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record("TERMINATION_SETTLEMENT_EXECUTED", "termination_requests", String.valueOf(terminationRequestId), actorAccountId);
        paymentWalletService.autoRefundParticipantDeposits(contract.getContractId(), actorAccountId);
        request.setDepositRefundedAt(LocalDateTime.now());
        request.setStatus(TerminationRequestEntity.STATUS_COMPLETED);
        notifyContractParticipantsExcept(contract, actorAccountId,
                (receiver, actor) -> notificationService.notifyTerminationSettlementExecuted(receiver, actor, contract.getContractId(), terminationRequestId));
        return terminationRequestRepository.save(request);
    }

    @Transactional
    public TerminationRequestEntity withdrawTerminationRequest(Long terminationRequestId, String reason) {
        TerminationRequestEntity request = requireTerminationRequest(terminationRequestId);
        AccountEntity actor = accessService.currentAccount();
        boolean admin = "ADMIN".equals(actor.getRole().getRoleName());
        if (!admin && !actor.getAccountId().equals(request.getRequestedByAccountId())) {
            throw new AppException("CHI NGUOI TAO YEU CAU HOAC ADMIN MOI DUOC HUY");
        }
        if (!List.of(TerminationRequestEntity.STATUS_REQUESTED, TerminationRequestEntity.STATUS_STAFF_REVIEWING).contains(request.getStatus())) {
            throw new AppException("YEU CAU CHAM DUT DA QUA BUOC CO THE HUY");
        }
        ContractEntity contract = contractRepository.findById(request.getContractId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        request.setStatus(TerminationRequestEntity.STATUS_CANCELLED);
        request.setCancelledAt(LocalDateTime.now());
        request.setCancelledByAccountId(actor.getAccountId());
        request.setCancellationReason(reason);
        contract.setStatus(ContractEntity.STATUS_ACTIVE);
        contract.setUpdatedAt(LocalDateTime.now());
        contractRepository.save(contract);
        auditLogService.record("TERMINATION_CANCELLED", "termination_requests", String.valueOf(terminationRequestId), actor.getAccountId());
        notifyContractParticipantsExcept(contract, actor.getAccountId(),
                (receiver, actorId) -> notificationService.notifyTerminationCancelled(receiver, actorId, contract.getContractId(), terminationRequestId));
        return terminationRequestRepository.save(request);
    }

    @Transactional
    public TerminationRequestEntity refundDepositAfterTermination(Long terminationRequestId, DepositRefundRequest refundRequest) {
        accessService.requireRole("ADMIN");
        TerminationRequestEntity request = requireTerminationRequest(terminationRequestId);
        if (!TerminationRequestEntity.STATUS_AWAITING_DEPOSIT_REFUND.equals(request.getStatus())) {
            throw new AppException("YEU CAU CHAM DUT CHUA SAN SANG HOAN KY QUY");
        }
        ContractDepositEntity deposit = paymentWalletService.refundContractDeposit(request.getContractId(), refundRequest);
        request.setDepositRefundedAt(LocalDateTime.now());
        request.setDepositRefundTransactionId(deposit.getRefundTransactionId());
        request.setStatus(TerminationRequestEntity.STATUS_COMPLETED);
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record("TERMINATION_DEPOSIT_REFUNDED", "termination_requests", String.valueOf(terminationRequestId), actorAccountId);
        contractRepository.findById(request.getContractId()).ifPresent(contract ->
                notifyContractParticipantsExcept(contract, actorAccountId,
                        (receiver, actor) -> notificationService.notifyTerminationDepositRefunded(receiver, actor, contract.getContractId(), terminationRequestId)));
        return terminationRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<TerminationRequestEntity> listTerminationRequestsByContract(Integer contractId) {
        requireContractParticipantOrOperator(contractId);
        return terminationRequestRepository.findByContractIdOrderByCreatedAtDesc(contractId);
    }

    @Transactional(readOnly = true)
    public TerminationRequestEntity getTerminationRequest(Long terminationRequestId) {
        TerminationRequestEntity request = requireTerminationRequest(terminationRequestId);
        requireContractParticipantOrOperator(request.getContractId());
        return request;
    }

    @Transactional
    public CaseAttachmentEntity createCaseAttachment(CaseAttachmentEntity input) {
        if (input == null || input.getOwnerType() == null || input.getOwnerId() == null || input.getFileUrl() == null || input.getFileUrl().isBlank()) {
            throw new AppException("ATTACHMENT KHONG HOP LE");
        }
        requireAttachmentAccess(input.getOwnerType(), input.getOwnerId());
        input.setAttachmentId(null);
        input.setOwnerType(input.getOwnerType().trim().toUpperCase());
        input.setUploadedByAccountId(accessService.currentAccount().getAccountId());
        CaseAttachmentEntity saved = caseAttachmentRepository.save(input);
        auditLogService.record("CASE_ATTACHMENT_CREATED", "case_attachments", String.valueOf(saved.getAttachmentId()), input.getUploadedByAccountId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CaseAttachmentEntity> listCaseAttachments(String ownerType, Long ownerId) {
        requireAttachmentAccess(ownerType, ownerId);
        return caseAttachmentRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtAsc(ownerType.trim().toUpperCase(), ownerId);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `recordDemoTesting` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public DisputeEntity recordDemoTesting(Integer disputeId, String testResult) {
        accessService.requireRole("STAFF");
        // STAFF chi ghi nhan demo testing cho dispute duoc admin phan cong.
        if (testResult == null || testResult.isBlank()) throw new AppException("KET QUA DEMO TEST KHONG DUOC DE TRONG");
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        requireAssignedStaff(dispute);
        dispute.setEvidenceReport(testResult);
        if ("Open".equals(dispute.getStatus())) dispute.setStatus("UnderReview");
        DisputeEntity saved = disputeRepository.save(dispute);
        systemWalletService.syncWallet();
        auditLogService.record(AuditLogService.ACTION_RECORD_DEMO_TESTING, "disputes", String.valueOf(disputeId), accessService.currentAccount().getAccountId());
        return saved;
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `issueTechnicalReport` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public DisputeEntity issueTechnicalReport(Integer disputeId, String reportContent, String proposedAction) {
        accessService.requireRole("STAFF");
        // STAFF de xuat huong xu ly, ADMIN la nguoi chot o resolveDispute.
        if (reportContent == null || reportContent.isBlank()) throw new AppException("TECHNICAL REPORT KHONG DUOC DE TRONG");
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        requireAssignedStaff(dispute);
        dispute.setEvidenceReport(reportContent);
        if (proposedAction != null && !proposedAction.isBlank()) dispute.setProposedAction(proposedAction);
        dispute.setStatus("Escalated");
        DisputeEntity saved = disputeRepository.save(dispute);
        systemWalletService.syncWallet();
        auditLogService.record(AuditLogService.ACTION_ISSUE_TECHNICAL_REPORT, "disputes", String.valueOf(disputeId), accessService.currentAccount().getAccountId());
        return saved;
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `processPaymentWebhook` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public TransactionEntity processPaymentWebhook(Long transactionId, String paymentStatus, String bankTxCode, String receiptImgUrl) {
        accessService.requireRole("ADMIN");
        // Payment webhook chi cap nhat transaction; du an khong con bang invoice noi bo.
        if (!List.of("Success", "Failed").contains(paymentStatus)) {
            throw new AppException("PAYMENT STATUS KHONG HOP LE");
        }
        TransactionEntity transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY TRANSACTION"));
        transaction.setStatus(paymentStatus);
        transaction = transactionRepository.save(transaction);
        systemWalletService.syncWallet();
        auditLogService.record(AuditLogService.ACTION_PROCESS_PAYMENT_WEBHOOK, "transactions", String.valueOf(transactionId), accessService.currentAccount().getAccountId());
        return transaction;
    }

    // Note: Hàm `requireBusinessOwnedContract` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private List<MilestoneEntity> attachCriteria(List<MilestoneEntity> milestones) {
        milestones.forEach(this::attachCriteria);
        return milestones;
    }

    private MilestoneEntity attachCriteria(MilestoneEntity milestone) {
        if (milestone == null || milestone.getMilestoneId() == null) return milestone;
        List<AcceptanceCriteriaEntity> criteria = criteriaRepository
                .findByMilestoneIdOrderBySortOrderAscCriteriaIdAsc(milestone.getMilestoneId());
        milestone.setCriteria(criteria);
        milestone.setAcceptanceCriteria(criteria.stream()
                .map(AcceptanceCriteriaEntity::getDescription)
                .toList());
        return milestone;
    }

    private void replaceMilestoneCriteria(Integer milestoneId, List<String> descriptions) {
        criteriaRepository.deleteByMilestoneId(milestoneId);
        if (descriptions == null || descriptions.isEmpty()) return;
        java.util.LinkedHashSet<String> unique = new java.util.LinkedHashSet<>();
        int sortOrder = 1;
        for (String description : descriptions) {
            if (description == null || description.isBlank()) continue;
            String normalized = description.trim();
            if (!unique.add(normalized.toLowerCase(java.util.Locale.ROOT))) continue;
            criteriaRepository.save(AcceptanceCriteriaEntity.builder()
                    .milestoneId(milestoneId)
                    .description(normalized)
                    .sortOrder(sortOrder++)
                    .build());
        }
    }

    private MilestoneEntity requireEditableCriteriaMilestone(Integer milestoneId) {
        accessService.requireRole("BUSINESS");
        accessService.requireApprovedAccount();
        MilestoneEntity milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        JobEntity job = requireBusinessOwnedJob(milestone.getJobId());
        if (!List.of("DRAFT", "OPEN").contains(job.getStatus())) {
            throw new AppException("JOB KHONG CHO PHEP SUA TIEU CHI NGHIEM THU");
        }
        if (contractRepository.findByJobId(job.getJobId()).isPresent()) {
            throw new AppException("JOB DA CO CONTRACT, KHONG DUOC SUA TIEU CHI NGHIEM THU");
        }
        return milestone;
    }

    private AcceptanceCriteriaEntity requireOwnedCriteria(Integer milestoneId, Integer criteriaId) {
        AcceptanceCriteriaEntity criteria = criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY ACCEPTANCE CRITERIA"));
        if (!milestoneId.equals(criteria.getMilestoneId())) {
            throw new AppException("TIEU CHI NGHIEM THU KHONG THUOC MILESTONE");
        }
        return criteria;
    }

    private void validateCriteriaRequest(AcceptanceCriteriaRequest request) {
        if (request == null || request.getDescription() == null || request.getDescription().isBlank()) {
            throw new AppException("CRITERIA DESCRIPTION KHONG DUOC DE TRONG");
        }
        if (request.getSortOrder() != null && request.getSortOrder() <= 0) {
            throw new AppException("CRITERIA SORT ORDER PHAI LON HON 0");
        }
    }

    private ContractEntity requireBusinessOwnedContract(Integer contractId) {
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        Integer businessId = businessProfileRepository.findByAccountId(accessService.currentAccount().getAccountId())
                .map(BusinessProfileEntity::getBusinessId)
                .orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
        if (!businessId.equals(contract.getBusinessId())) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        return contract;
    }

    // Note: Hàm `requireBusinessOwnedJob` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private List<TerminationRequestEntity> activeTerminationRequests(Integer contractId) {
        return terminationRequestRepository.findByContractIdAndStatusIn(contractId, List.of(
                TerminationRequestEntity.STATUS_AWAITING_EXPERT_RESPONSE,
                TerminationRequestEntity.STATUS_REQUESTED,
                TerminationRequestEntity.STATUS_STAFF_REVIEWING,
                TerminationRequestEntity.STATUS_STAFF_APPROVED,
                TerminationRequestEntity.STATUS_AWAITING_SETTLEMENT_EXECUTION,
                TerminationRequestEntity.STATUS_AWAITING_DEPOSIT_REFUND
        ));
    }

    private Optional<ContractMilestoneEntity> currentExecutionMilestone(Integer contractId) {
        return contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contractId).stream()
                .filter(item -> !List.of(ContractMilestoneEntity.STATUS_COMPLETED, ContractMilestoneEntity.STATUS_CANCELLED).contains(item.getStatus()))
                .findFirst();
    }

    private void requireParticipant(ContractEntity contract, AccountEntity actor) {
        if (!isParticipant(contract, actor)) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
    }

    private boolean isParticipant(ContractEntity contract, AccountEntity actor) {
        Integer businessId = businessProfileRepository.findByAccountId(actor.getAccountId()).map(BusinessProfileEntity::getBusinessId).orElse(null);
        Integer expertId = expertProfileRepository.findByAccountId(actor.getAccountId()).map(ExpertProfileEntity::getExpertId).orElse(null);
        return (businessId != null && businessId.equals(contract.getBusinessId()))
                || (expertId != null && expertId.equals(contract.getExpertId()));
    }

    private TerminationRequestEntity requireTerminationRequest(Long terminationRequestId) {
        return terminationRequestRepository.findById(terminationRequestId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY YEU CAU CHAM DUT"));
    }

    private void requireAssignedTerminationStaff(TerminationRequestEntity request) {
        Integer staffId = getCurrentStaffId(accessService.currentAccount());
        if (request.getAssignedStaffId() == null || !staffId.equals(request.getAssignedStaffId())) {
            throw new AppException("STAFF CHI DUOC XU LY YEU CAU CHAM DUT DUOC GAN");
        }
    }

    private boolean terminationNeedsSettlement(TerminationRequestEntity request) {
        if (request.getCurrentMilestoneId() == null) return false;
        return contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(request.getContractId()).stream()
                .filter(item -> request.getCurrentMilestoneId().equals(item.getJobMilestoneId()))
                .findFirst()
                .filter(item -> item.getEscrowReleasedAt() == null)
                .filter(item -> List.of(ContractMilestoneEntity.STATUS_DEPOSITED, ContractMilestoneEntity.STATUS_IN_PROGRESS, ContractMilestoneEntity.STATUS_UNDER_REVIEW, ContractMilestoneEntity.STATUS_DISPUTED).contains(item.getStatus()))
                .isPresent();
    }

    private void cancelRemainingMilestones(Integer contractId, Integer currentMilestoneId) {
        for (ContractMilestoneEntity item : contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contractId)) {
            if (List.of(ContractMilestoneEntity.STATUS_COMPLETED, ContractMilestoneEntity.STATUS_CANCELLED).contains(item.getStatus())) continue;
            if (currentMilestoneId != null && currentMilestoneId.equals(item.getJobMilestoneId()) && item.getEscrowReleasedAt() != null) continue;
            item.setStatus(ContractMilestoneEntity.STATUS_CANCELLED);
            contractMilestoneRepository.save(item);
            syncLiveMilestoneFromContractMilestone(item);
        }
    }

    private void syncLiveMilestoneFromContractMilestone(ContractMilestoneEntity item) {
        milestoneRepository.findById(item.getJobMilestoneId()).ifPresent(milestone -> {
            milestone.setStatus(item.getStatus());
            milestone.setEscrowReleasedAt(item.getEscrowReleasedAt());
            milestone.setSettlementSourceType(item.getSettlementSourceType());
            milestone.setSettlementSourceId(item.getSettlementSourceId());
            milestone.setUpdatedAt(LocalDateTime.now());
            milestoneRepository.save(milestone);
        });
    }

    private void saveAttachmentIfPresent(String ownerType, Long ownerId, String fileUrl, String fileName, String fileType, String note) {
        if (fileUrl == null || fileUrl.isBlank()) return;
        caseAttachmentRepository.save(CaseAttachmentEntity.builder()
                .ownerType(ownerType)
                .ownerId(ownerId)
                .uploadedByAccountId(accessService.currentAccount().getAccountId())
                .fileUrl(fileUrl.trim())
                .fileName(fileName)
                .fileType(fileType)
                .note(note)
                .build());
    }

    private void requireAttachmentAccess(String ownerType, Long ownerId) {
        if (ownerType == null || ownerId == null) throw new AppException("ATTACHMENT OWNER KHONG HOP LE");
        String normalized = ownerType.trim().toUpperCase();
        if (CaseAttachmentEntity.OWNER_DISPUTE.equals(normalized)) {
            DisputeEntity dispute = disputeRepository.findById(ownerId.intValue()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
            AccountEntity actor = accessService.currentAccount();
            if ("STAFF".equals(actor.getRole().getRoleName())) {
                requireAssignedStaff(dispute);
            } else {
                requireContractParticipantOrOperator(dispute.getContractId());
            }
            return;
        }
        if (List.of(CaseAttachmentEntity.OWNER_TERMINATION_REQUEST, CaseAttachmentEntity.OWNER_PARTIAL_EVIDENCE, CaseAttachmentEntity.OWNER_STAFF_REPORT).contains(normalized)) {
            TerminationRequestEntity request = requireTerminationRequest(ownerId);
            requireContractParticipantOrOperator(request.getContractId());
            return;
        }
        if (CaseAttachmentEntity.OWNER_DELIVERABLE_REJECTION.equals(normalized)) {
            requireApprovedForBusinessOrExpert();
            return;
        }
        throw new AppException("ATTACHMENT OWNER TYPE KHONG HOP LE");
    }

    private WalletLedgerService.WalletOperationContext walletOperationContext(
            Integer contractId,
            Integer milestoneId,
            String metadata,
            String operationKey,
            String operationLeg
    ) {
        WalletLedgerService.WalletOperationContext.Builder builder = WalletLedgerService.WalletOperationContext.builder()
                .contractId(contractId)
                .milestoneId(milestoneId)
                .metadata(metadata)
                .operationKey(operationKey);
        if (operationLeg != null) {
            builder.operationLeg(operationLeg);
        }
        return builder.build();
    }

    private String walletMetadata(String sourceType, Integer disputeId, Long terminationRequestId, Integer actorAccountId,
                                  Integer businessAccountId, Integer expertAccountId, BigDecimal expertPercent,
                                  BigDecimal expertPayout, BigDecimal businessRefund) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sourceType", sourceType);
            metadata.put("disputeId", disputeId);
            metadata.put("terminationRequestId", terminationRequestId);
            metadata.put("actorAccountId", actorAccountId);
            metadata.put("businessAccountId", businessAccountId);
            metadata.put("expertAccountId", expertAccountId);
            metadata.put("expertPayoutPercentage", expertPercent);
            metadata.put("expertPayoutAmount", expertPayout);
            metadata.put("businessRefundAmount", businessRefund);
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            return "{}";
        }
    }

    private JobEntity requireBusinessOwnedJob(Integer jobId) {
        JobEntity job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        Integer businessId = businessProfileRepository.findByAccountId(accessService.currentAccount().getAccountId())
                .map(BusinessProfileEntity::getBusinessId)
                .orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
        if (!businessId.equals(job.getBusinessId())) throw new AppException("BAN KHONG THUOC JOB NAY");
        return job;
    }

    // Note: Hàm `requireExpertOwnedContract` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private ContractEntity requireExpertOwnedContract(Integer contractId) {
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        Integer expertId = expertProfileRepository.findByAccountId(accessService.currentAccount().getAccountId())
                .map(ExpertProfileEntity::getExpertId)
                .orElseThrow(() -> new NotFoundException("CHUA CO EXPERT PROFILE"));
        if (!expertId.equals(contract.getExpertId())) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        return contract;
    }

    // Note: Hàm `requireExpertOwnedContractByJob` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private ContractEntity requireExpertOwnedContractByJob(Integer jobId) {
        ContractEntity contract = contractRepository.findByJobId(jobId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT CUA JOB"));
        Integer expertId = expertProfileRepository.findByAccountId(accessService.currentAccount().getAccountId())
                .map(ExpertProfileEntity::getExpertId)
                .orElseThrow(() -> new NotFoundException("CHUA CO EXPERT PROFILE"));
        if (!expertId.equals(contract.getExpertId())) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        return contract;
    }

    // Note: Hàm `requireContractParticipantOrOperator` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private ContractEntity requireContractParticipantOrOperator(Integer contractId) {
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        AccountEntity actor = accessService.currentAccount();
        String role = actor.getRole().getRoleName();
        if ("ADMIN".equals(role)) return contract;
        if ("STAFF".equals(role)) {
            Integer staffId = getCurrentStaffId(actor);
            boolean assigned = disputeRepository.findByContractId(contractId).stream()
                    .anyMatch(dispute -> staffId.equals(dispute.getAssignedStaffId()))
                    || terminationRequestRepository.findByContractIdOrderByCreatedAtDesc(contractId).stream()
                    .anyMatch(request -> staffId.equals(request.getAssignedStaffId()));
            if (!assigned) throw new AppException("STAFF CHI DUOC XEM CONTRACT CO DISPUTE DUOC GAN");
            return contract;
        }
        Integer businessId = businessProfileRepository.findByAccountId(actor.getAccountId()).map(BusinessProfileEntity::getBusinessId).orElse(null);
        Integer expertId = expertProfileRepository.findByAccountId(actor.getAccountId()).map(ExpertProfileEntity::getExpertId).orElse(null);
        boolean participant = (businessId != null && businessId.equals(contract.getBusinessId()))
                || (expertId != null && expertId.equals(contract.getExpertId()));
        if (!participant) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        return contract;
    }

    // Note: Hàm `requireJobParticipantOrOwner` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private void requireJobParticipantOrOwner(Integer jobId) {
        AccountEntity actor = accessService.currentAccount();
        String role = actor.getRole().getRoleName();
        if ("ADMIN".equals(role)) return;
        Optional<ContractEntity> contract = contractRepository.findByJobId(jobId);
        if ("STAFF".equals(role)) {
            if (contract.isEmpty()) throw new AppException("STAFF CHI DUOC XEM JOB CO DISPUTE DUOC GAN");
            requireContractParticipantOrOperator(contract.get().getContractId());
            return;
        }
        Integer businessId = businessProfileRepository.findByAccountId(actor.getAccountId()).map(BusinessProfileEntity::getBusinessId).orElse(null);
        if (businessId != null) {
            JobEntity job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
            if (businessId.equals(job.getBusinessId())) return;
        }
        Integer expertId = expertProfileRepository.findByAccountId(actor.getAccountId()).map(ExpertProfileEntity::getExpertId).orElse(null);
        if (expertId != null && contract.map(c -> expertId.equals(c.getExpertId())).orElse(false)) return;
        throw new AppException("BAN KHONG THUOC JOB NAY");
    }

    // Note: Hàm `requireAssignedStaff` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private void requireAssignedStaff(DisputeEntity dispute) {
        Integer staffId = getCurrentStaffId(accessService.currentAccount());
        if (dispute.getAssignedStaffId() == null || !staffId.equals(dispute.getAssignedStaffId())) {
            throw new AppException("STAFF CHI DUOC XU LY DISPUTE DUOC GAN");
        }
    }

    // Note: Hàm `getCurrentStaffId` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private Integer getCurrentStaffId(AccountEntity actor) {
        return staffRepository.findByAccountId(actor.getAccountId())
                .map(StaffEntity::getStaffId)
                .orElseThrow(() -> new NotFoundException("CHUA CO STAFF PROFILE"));
    }

    // Note: Hàm `requireApprovedForBusinessOrExpert` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private void requireApprovedForBusinessOrExpert() {
        String role = accessService.currentAccount().getRole().getRoleName();
        if ("BUSINESS".equals(role) || "EXPERT".equals(role)) {
            accessService.requireApprovedAccount();
        }
    }

    // Note: Hàm `isAutoAssignStaffEnabled` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private boolean isAutoAssignStaffEnabled() {
        return systemSettingRepository.findById("auto_assign_staff_enabled")
                .map(SystemSettingEntity::getSettingValue)
                .map(String::trim)
                .map(String::toLowerCase)
                .map(v -> v.equals("true") || v.equals("1"))
                .orElse(false);
    }

    private void validateDuration(Integer duration, String durationUnit) {
        boolean hasDuration = duration != null;
        boolean hasUnit = durationUnit != null && !durationUnit.isBlank();
        if (hasDuration != hasUnit) {
            throw new AppException("DURATION VA DURATION UNIT PHAI CUNG CO HOAC CUNG KHONG CO");
        }
        if (hasDuration && duration <= 0) {
            throw new AppException("DURATION PHAI LON HON 0");
        }
        if (hasUnit) {
            String unit = durationUnit.trim().toUpperCase();
            if (!List.of("DAY", "WEEK", "MONTH").contains(unit)) {
                throw new AppException("DURATION UNIT KHONG HOP LE. CHAP NHAN: DAY, WEEK, MONTH");
            }
        }
    }
}
