/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/ContractExecutionService.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Note: Annotation này cho Spring quản lý class như một service chứa nghiệp vụ.
@Service
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class ContractExecutionService {
    private final AccessService accessService;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final ProposalRepository proposalRepository;
    private final JobRepository jobRepository;
    private final ContractRepository contractRepository;
    private final ContractChangeRequestRepository changeRequestRepository;
    private final MilestoneRepository milestoneRepository;
    private final AcceptanceCriteriaRepository criteriaRepository;
    private final DeliverableRepository deliverableRepository;
    private final TransactionRepository transactionRepository;
    private final DisputeRepository disputeRepository;
    private final StaffRepository staffRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final SystemWalletService systemWalletService;

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `createDraftFromProposal` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public ContractEntity createDraftFromProposal(Integer proposalId, ContractEntity input) {
        accessService.requireRole("BUSINESS");
        accessService.requireApprovedAccount();
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer businessId = businessProfileRepository.findByAccountId(accountId).map(BusinessProfileEntity::getBusinessId).orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
        ProposalEntity proposal = proposalRepository.findById(proposalId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY PROPOSAL"));
        if (!"Accepted".equalsIgnoreCase(proposal.getStatus())) throw new AppException("CHI DUOC TAO CONTRACT TU PROPOSAL DA ACCEPTED");
        JobEntity job = proposalRepository.findById(proposalId)
                .flatMap(p -> Optional.ofNullable(p.getJobId()).flatMap(jobId -> jobRepository.findById(jobId)))
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB CUA PROPOSAL"));
        // DAM BAO DOANH NGHIEP CHI DUOC TAO CONTRACT TU JOB CUA CHINH MINH.
        if (!businessId.equals(job.getBusinessId())) throw new AppException("BAN KHONG CO QUYEN TAO CONTRACT CHO JOB NAY");
        if (input.getTotalBudget() == null || input.getTotalBudget().signum() <= 0) throw new AppException("TOTAL BUDGET PHAI LON HON 0");
        if (input.getTimelineDays() == null || input.getTimelineDays() <= 0) throw new AppException("TIMELINE DAYS PHAI LON HON 0");
        input.setContractId(null);
        input.setJobId(proposal.getJobId());
        input.setExpertId(proposal.getExpertId());
        input.setBusinessId(businessId);
        input.setNdaSigned(Boolean.FALSE);
        input.setStatus("Draft");
        input.setBusinessAcceptedAt(null);
        input.setExpertAcceptedAt(null);
        return contractRepository.save(input);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `requestChange` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public ContractChangeRequestEntity requestChange(ContractChangeRequestEntity input) {
        requireApprovedForBusinessOrExpert();
        Integer accountId = accessService.currentAccount().getAccountId();
        // CHI CHO PHEP GUI YEU CAU SUA DOI KHI CONTRACT DANG O DRAFT/NEGOTIATING.
        ContractEntity contract = contractRepository.findById(input.getContractId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        // CHI CHO PHEP HAI BEN LIEN QUAN CONTRACT GUI YEU CAU SUA DOI.
        Integer businessId = businessProfileRepository.findByAccountId(accountId).map(BusinessProfileEntity::getBusinessId).orElse(null);
        Integer expertId = expertProfileRepository.findByAccountId(accountId).map(ExpertProfileEntity::getExpertId).orElse(null);
        boolean isParticipant = (businessId != null && businessId.equals(contract.getBusinessId()))
                || (expertId != null && expertId.equals(contract.getExpertId()));
        if (!isParticipant) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        if (!List.of("Draft", "Negotiating").contains(contract.getStatus())) {
            throw new AppException("CONTRACT KHONG O TRANG THAI CHO PHEP REQUEST CHANGE");
        }
        if (input.getChangeType() == null || input.getChangeType().isBlank()) throw new AppException("CHANGE TYPE KHONG DUOC DE TRONG");
        if (input.getChangeSummary() == null || input.getChangeSummary().isBlank()) throw new AppException("CHANGE SUMMARY KHONG DUOC DE TRONG");
        input.setRequestId(null);
        input.setRequestedByAccountId(accountId);
        input.setStatus("Pending");
        // MOI CHANGE REQUEST LAM MO LAI DAM PHAN, NEN HAI BEN PHAI ACCEPT LAI.
        contract.setStatus("Negotiating");
        contract.setBusinessAcceptedAt(null);
        contract.setExpertAcceptedAt(null);
        contractRepository.save(contract);
        return changeRequestRepository.save(input);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `activateContract` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public ContractEntity activateContract(Integer contractId) {
        requireApprovedForBusinessOrExpert();
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer businessId = businessProfileRepository.findByAccountId(accountId).map(BusinessProfileEntity::getBusinessId).orElse(null);
        Integer expertId = expertProfileRepository.findByAccountId(accountId).map(ExpertProfileEntity::getExpertId).orElse(null);
        boolean isParticipant = (businessId != null && businessId.equals(contract.getBusinessId()))
                || (expertId != null && expertId.equals(contract.getExpertId()));
        if (!isParticipant) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        if ("Active".equals(contract.getStatus())) return contract;
        if (!List.of("Draft", "Negotiating").contains(contract.getStatus())) throw new AppException("CONTRACT KHONG O TRANG THAI KICH HOAT");
        LocalDateTime now = LocalDateTime.now();
        if (businessId != null && businessId.equals(contract.getBusinessId())) contract.setBusinessAcceptedAt(now);
        if (expertId != null && expertId.equals(contract.getExpertId())) contract.setExpertAcceptedAt(now);
        contract.setStatus(contract.getBusinessAcceptedAt() != null && contract.getExpertAcceptedAt() != null ? "Active" : "Negotiating");
        contract.setUpdatedAt(LocalDateTime.now());
        return contractRepository.save(contract);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `signNda` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public ContractEntity signNda(Integer contractId) {
        // CHI CHO BEN EXPERT KY NDA SAU KHI CONTRACT DA ACTIVE.
        accessService.requireRole("EXPERT");
        accessService.requireApprovedAccount();
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer expertId = expertProfileRepository.findByAccountId(accountId)
                .map(ExpertProfileEntity::getExpertId)
                .orElseThrow(() -> new NotFoundException("CHUA CO EXPERT PROFILE"));
        if (!expertId.equals(contract.getExpertId())) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        if (!"Active".equals(contract.getStatus())) throw new AppException("CHI DUOC KY NDA KHI CONTRACT DA ACTIVE");
        contract.setNdaSigned(Boolean.TRUE);
        contract.setUpdatedAt(LocalDateTime.now());
        return contractRepository.save(contract);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `terminateContract` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public ContractEntity terminateContract(Integer contractId, String reason) {
        // CHI BUSINESS HOAC ADMIN DUOC CHAM DUT CONTRACT KHI CHUA HOAN TAT.
        accessService.requireRole("BUSINESS", "ADMIN");
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        if ("Completed".equals(contract.getStatus()) || "Terminated".equals(contract.getStatus()) || "Cancelled".equals(contract.getStatus())) {
            throw new AppException("CONTRACT KHONG THE CHAM DUT O TRANG THAI HIEN TAI");
        }
        if (reason == null || reason.isBlank()) throw new AppException("LY DO CHAM DUT KHONG DUOC DE TRONG");
        requireApprovedForBusinessOrExpert();
        AccountEntity actor = accessService.currentAccount();
        if ("BUSINESS".equals(actor.getRole().getRoleName())) {
            Integer businessId = businessProfileRepository.findByAccountId(actor.getAccountId())
                    .map(BusinessProfileEntity::getBusinessId)
                    .orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
            if (!businessId.equals(contract.getBusinessId())) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        }
        contract.setStatus("Terminated");
        contract.setUpdatedAt(LocalDateTime.now());
        return contractRepository.save(contract);
    }

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Transactional public MilestoneEntity createMilestone(MilestoneEntity input) {
        accessService.requireRole("BUSINESS");
        accessService.requireApprovedAccount();
        JobEntity job = requireBusinessOwnedJob(input.getJobId());
        if (!List.of("DRAFT", "OPEN").contains(job.getStatus())) throw new AppException("JOB KHONG CHO PHEP TAO MILESTONE");
        if (input.getFundsAllocated() == null || input.getFundsAllocated().signum() < 0) throw new AppException("FUNDS ALLOCATED KHONG HOP LE");
        if (input.getOrderIndex() == null || input.getOrderIndex() <= 0) throw new AppException("ORDER INDEX PHAI LON HON 0");
        if (milestoneRepository.existsByJobIdAndOrderIndex(input.getJobId(), input.getOrderIndex())) {
            throw new AppException("ORDER INDEX DA TON TAI TRONG JOB");
        }
        input.setContractId(null);
        if (input.getStatus() == null) input.setStatus("Pending");
        return milestoneRepository.save(input);
    }
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Transactional public AcceptanceCriteriaEntity createCriteria(AcceptanceCriteriaEntity input) {
        accessService.requireRole("BUSINESS");
        accessService.requireApprovedAccount();
        MilestoneEntity milestone = milestoneRepository.findById(input.getMilestoneId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        requireBusinessOwnedJob(milestone.getJobId());
        if (input.getDescription() == null || input.getDescription().isBlank()) throw new AppException("CRITERIA DESCRIPTION KHONG DUOC DE TRONG");
        if (input.getIsPassed() == null) input.setIsPassed(false);
        return criteriaRepository.save(input);
    }
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Transactional public DeliverableEntity submitDeliverable(DeliverableEntity input) {
        accessService.requireRole("EXPERT");
        accessService.requireApprovedAccount();
        MilestoneEntity milestone = milestoneRepository.findById(input.getMilestoneId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        ContractEntity contract = requireExpertOwnedContractByJob(milestone.getJobId());
        if (!"Active".equals(contract.getStatus())) throw new AppException("CHI DUOC SUBMIT DELIVERABLE KHI CONTRACT ACTIVE");
        if (!Boolean.TRUE.equals(contract.getNdaSigned())) throw new AppException("EXPERT PHAI KY NDA TRUOC KHI BAN GIAO");
        DeliverableEntity saved = deliverableRepository.save(input);
        milestone.setStatus("Under Review");
        milestone.setUpdatedAt(LocalDateTime.now());
        milestoneRepository.save(milestone);
        return saved;
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
        // NEU BAT CAU HINH AUTO ASSIGN THI HE THONG TU GAN STAFF DAU TIEN KHA DUNG.
        if (input.getAssignedStaffId() == null && isAutoAssignStaffEnabled()) {
            staffRepository.findAll().stream().findFirst().ifPresent(staff -> input.setAssignedStaffId(staff.getStaffId()));
        }
        if (input.getStatus() == null) input.setStatus("Open");
        DisputeEntity saved = disputeRepository.save(input);
        systemWalletService.syncWallet();
        return saved;
    }

    // Note: Hàm `listContracts` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<ContractEntity> listContracts() {
        AccountEntity actor = accessService.currentAccount();
        String role = actor.getRole().getRoleName();
        if ("ADMIN".equals(role)) return contractRepository.findAll();
        if ("STAFF".equals(role)) {
            Integer staffId = staffRepository.findByAccountId(actor.getAccountId())
                    .map(StaffEntity::getStaffId)
                    .orElseThrow(() -> new NotFoundException("CHUA CO STAFF PROFILE"));
            return disputeRepository.findByAssignedStaffId(staffId).stream()
                    .map(DisputeEntity::getContractId)
                    .distinct()
                    .map(contractRepository::findById)
                    .flatMap(Optional::stream)
                    .toList();
        }
        if ("BUSINESS".equals(role)) {
            accessService.requireApprovedAccount();
            Integer businessId = businessProfileRepository.findByAccountId(actor.getAccountId()).map(BusinessProfileEntity::getBusinessId).orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
            return contractRepository.findByBusinessId(businessId);
        }
        if ("EXPERT".equals(role)) {
            accessService.requireApprovedAccount();
            Integer expertId = expertProfileRepository.findByAccountId(actor.getAccountId()).map(ExpertProfileEntity::getExpertId).orElseThrow(() -> new NotFoundException("CHUA CO EXPERT PROFILE"));
            return contractRepository.findByExpertId(expertId);
        }
        throw new AppException("ROLE KHONG HOP LE");
    }
    // Note: Hàm `listMilestonesByContract` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<MilestoneEntity> listMilestonesByContract(Integer contractId) {
        ContractEntity contract = requireContractParticipantOrOperator(contractId);
        return milestoneRepository.findByJobIdOrderByOrderIndexAsc(contract.getJobId());
    }
    // Note: Hàm `listMilestonesByJob` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<MilestoneEntity> listMilestonesByJob(Integer jobId) {
        JobEntity job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        if (!"OPEN".equalsIgnoreCase(job.getStatus())) {
            requireJobParticipantOrOwner(jobId);
        }
        return milestoneRepository.findByJobIdOrderByOrderIndexAsc(jobId);
    }
    // Note: Hàm `listCriteriaByMilestone` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<AcceptanceCriteriaEntity> listCriteriaByMilestone(Integer milestoneId) {
        MilestoneEntity milestone = milestoneRepository.findById(milestoneId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        requireJobParticipantOrOwner(milestone.getJobId());
        return criteriaRepository.findByMilestoneId(milestoneId);
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
        requireContractParticipantOrOperator(contractId);
        return disputeRepository.findByContractId(contractId);
    }
    // Note: Hàm `getDispute` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public DisputeEntity getDispute(Integer disputeId) {
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        requireContractParticipantOrOperator(dispute.getContractId());
        return dispute;
    }
    // Note: Hàm `matchingByKeyword` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<ProposalEntity> matchingByKeyword(Integer jobId) {
        JobEntity job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        final String keyword = Optional.ofNullable(job.getAiTag()).filter(v -> !v.isBlank()).orElse("AI").toUpperCase();
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
        return saved;
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `assignDispute` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public DisputeEntity assignDispute(Integer disputeId, Integer staffId) {
        accessService.requireRole("ADMIN");
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        staffRepository.findById(staffId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY STAFF"));
        dispute.setAssignedStaffId(staffId);
        if ("Open".equalsIgnoreCase(dispute.getStatus())) {
            dispute.setStatus("UnderReview");
        }
        DisputeEntity saved = disputeRepository.save(dispute);
        systemWalletService.syncWallet();
        return saved;
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
        dispute.setAdminApprovedBy(accessService.currentAccount().getAccountId());
        DisputeEntity saved = disputeRepository.save(dispute);
        systemWalletService.syncWallet();
        return saved;
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
        for (MilestoneEntity milestone : milestoneRepository.findAll()) {
            if (!"Under Review".equals(milestone.getStatus()) && !"Pending".equals(milestone.getStatus())) continue;
            List<DeliverableEntity> deliverables = deliverableRepository.findByMilestoneId(milestone.getMilestoneId());
            if (deliverables.isEmpty()) continue;
            LocalDateTime lastSubmission = deliverables.stream()
                    .map(DeliverableEntity::getCreatedAt)
                    .filter(java.util.Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            if (lastSubmission == null) continue;
            if (!lastSubmission.plusDays(slaDays).isAfter(now)) {
                milestone.setStatus("Released");
                milestone.setUpdatedAt(now);
                updated.add(milestoneRepository.save(milestone));
            }
        }
        return updated;
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
        return saved;
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `processPaymentWebhook` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public TransactionEntity processPaymentWebhook(Long transactionId, String paymentStatus, String bankTxCode, String receiptImgUrl) {
        accessService.requireRole("ADMIN");
        // VNPay sandbox webhook chi cap nhat transaction; du an khong con bang invoice noi bo.
        if (!List.of("Success", "Failed").contains(paymentStatus)) {
            throw new AppException("PAYMENT STATUS KHONG HOP LE");
        }
        TransactionEntity transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY TRANSACTION"));
        transaction.setStatus(paymentStatus);
        transaction = transactionRepository.save(transaction);
        systemWalletService.syncWallet();
        return transaction;
    }

    // Note: Hàm `requireBusinessOwnedContract` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private ContractEntity requireBusinessOwnedContract(Integer contractId) {
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        Integer businessId = businessProfileRepository.findByAccountId(accessService.currentAccount().getAccountId())
                .map(BusinessProfileEntity::getBusinessId)
                .orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
        if (!businessId.equals(contract.getBusinessId())) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        return contract;
    }

    // Note: Hàm `requireBusinessOwnedJob` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
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
                    .anyMatch(dispute -> staffId.equals(dispute.getAssignedStaffId()));
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
}
