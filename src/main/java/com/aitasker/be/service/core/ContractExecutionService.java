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

@Service
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
    private final InvoiceRepository invoiceRepository;
    private final DisputeRepository disputeRepository;
    private final StaffRepository staffRepository;
    private final SystemSettingRepository systemSettingRepository;

    @Transactional
    public ContractEntity createDraftFromProposal(Integer proposalId, ContractEntity input) {
        accessService.requireRole("BUSINESS");
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer businessId = businessProfileRepository.findByAccountId(accountId).map(BusinessProfileEntity::getBusinessId).orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
        ProposalEntity proposal = proposalRepository.findById(proposalId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY PROPOSAL"));
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
        return contractRepository.save(input);
    }

    @Transactional
    public ContractChangeRequestEntity requestChange(ContractChangeRequestEntity input) {
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
        return changeRequestRepository.save(input);
    }

    @Transactional
    public ContractEntity activateContract(Integer contractId) {
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer businessId = businessProfileRepository.findByAccountId(accountId).map(BusinessProfileEntity::getBusinessId).orElse(null);
        Integer expertId = expertProfileRepository.findByAccountId(accountId).map(ExpertProfileEntity::getExpertId).orElse(null);
        boolean isParticipant = (businessId != null && businessId.equals(contract.getBusinessId()))
                || (expertId != null && expertId.equals(contract.getExpertId()));
        if (!isParticipant) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        if (!List.of("Draft", "Negotiating").contains(contract.getStatus())) throw new AppException("CONTRACT KHONG O TRANG THAI KICH HOAT");
        contract.setStatus("Active");
        contract.setUpdatedAt(LocalDateTime.now());
        return contractRepository.save(contract);
    }

    @Transactional
    public ContractEntity signNda(Integer contractId) {
        // CHI CHO BEN EXPERT KY NDA SAU KHI CONTRACT DA ACTIVE.
        accessService.requireRole("EXPERT");
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

    @Transactional
    public ContractEntity terminateContract(Integer contractId, String reason) {
        // CHI BUSINESS HOAC ADMIN DUOC CHAM DUT CONTRACT KHI CHUA HOAN TAT.
        accessService.requireRole("BUSINESS", "ADMIN");
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        if ("Completed".equals(contract.getStatus()) || "Terminated".equals(contract.getStatus()) || "Cancelled".equals(contract.getStatus())) {
            throw new AppException("CONTRACT KHONG THE CHAM DUT O TRANG THAI HIEN TAI");
        }
        if (reason == null || reason.isBlank()) throw new AppException("LY DO CHAM DUT KHONG DUOC DE TRONG");
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

    @Transactional public MilestoneEntity createMilestone(MilestoneEntity input) {
        if (input.getFundsAllocated() == null || input.getFundsAllocated().signum() < 0) throw new AppException("FUNDS ALLOCATED KHONG HOP LE");
        if (input.getOrderIndex() == null || input.getOrderIndex() <= 0) throw new AppException("ORDER INDEX PHAI LON HON 0");
        if (milestoneRepository.existsByContractIdAndOrderIndex(input.getContractId(), input.getOrderIndex())) {
            throw new AppException("ORDER INDEX DA TON TAI TRONG CONTRACT");
        }
        if (input.getStatus() == null) input.setStatus("Pending");
        return milestoneRepository.save(input);
    }
    @Transactional public AcceptanceCriteriaEntity createCriteria(AcceptanceCriteriaEntity input) { if (input.getIsPassed() == null) input.setIsPassed(false); return criteriaRepository.save(input); }
    @Transactional public DeliverableEntity submitDeliverable(DeliverableEntity input) { return deliverableRepository.save(input); }
    @Transactional public TransactionEntity createTransaction(TransactionEntity input) {
        if (input.getAmount() == null || input.getAmount().signum() < 0) throw new AppException("AMOUNT KHONG HOP LE");
        if (!List.of("Deposit", "Payout", "Refund").contains(input.getTransactionType())) throw new AppException("TRANSACTION TYPE KHONG HOP LE");
        if (input.getStatus() == null) input.setStatus("Pending");
        return transactionRepository.save(input);
    }
    @Transactional public InvoiceEntity createInvoice(InvoiceEntity input) {
        transactionRepository.findById(input.getTransactionId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY TRANSACTION"));
        if (invoiceRepository.existsByTransactionId(input.getTransactionId())) {
            throw new AppException("TRANSACTION DA CO INVOICE");
        }
        return invoiceRepository.save(input);
    }
    @Transactional public DisputeEntity createDispute(DisputeEntity input) {
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
        return disputeRepository.save(input);
    }

    public List<ContractEntity> listContracts() { return contractRepository.findAll(); }
    public List<MilestoneEntity> listMilestonesByContract(Integer contractId) { return milestoneRepository.findByContractIdOrderByOrderIndexAsc(contractId); }
    public List<AcceptanceCriteriaEntity> listCriteriaByMilestone(Integer milestoneId) { return criteriaRepository.findByMilestoneId(milestoneId); }
    public List<ProposalEntity> matchingByKeyword(Integer jobId) {
        final String keyword = "AI";
        return proposalRepository.findByJobId(jobId).stream()
                .filter(p -> p.getTechnicalSolution() != null && p.getTechnicalSolution().toUpperCase().contains(keyword))
                .toList();
    }

    @Transactional
    public TransactionEntity updateTransactionStatus(Long transactionId, String status) {
        accessService.requireRole("ADMIN", "STAFF");
        // RANG BUOC TRANG THAI GIAO DICH THEO FLOW ESCROW.
        if (!List.of("Pending", "Success", "Failed").contains(status)) {
            throw new AppException("STATUS TRANSACTION KHONG HOP LE");
        }
        TransactionEntity transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY TRANSACTION"));
        transaction.setStatus(status);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public DisputeEntity assignDispute(Integer disputeId, Integer staffId) {
        accessService.requireRole("ADMIN");
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        staffRepository.findById(staffId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY STAFF"));
        dispute.setAssignedStaffId(staffId);
        if ("Open".equalsIgnoreCase(dispute.getStatus())) {
            dispute.setStatus("UnderReview");
        }
        return disputeRepository.save(dispute);
    }

    @Transactional
    public DisputeEntity resolveDispute(Integer disputeId, String proposedAction) {
        accessService.requireRole("ADMIN");
        // ADMIN CHOT PHUONG AN XU LY TRANH CHAP VA DONG CASE.
        if (proposedAction == null || proposedAction.isBlank()) throw new AppException("PROPOSED ACTION KHONG DUOC DE TRONG");
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        dispute.setProposedAction(proposedAction);
        dispute.setStatus("Resolved");
        dispute.setAdminApprovedBy(accessService.currentAccount().getAccountId());
        return disputeRepository.save(dispute);
    }

    @Transactional
    public List<MilestoneEntity> runSlaAutoApprove() {
        accessService.requireRole("ADMIN", "STAFF");
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

    @Transactional
    public DisputeEntity recordDemoTesting(Integer disputeId, String testResult) {
        accessService.requireRole("STAFF", "ADMIN");
        // STAFF GHI NHAN KET QUA TEST DEMO KY THUAT DE PHUC VU QUY TRINH THAM DINH.
        if (testResult == null || testResult.isBlank()) throw new AppException("KET QUA DEMO TEST KHONG DUOC DE TRONG");
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        dispute.setEvidenceReport(testResult);
        if ("Open".equals(dispute.getStatus())) dispute.setStatus("UnderReview");
        return disputeRepository.save(dispute);
    }

    @Transactional
    public DisputeEntity issueTechnicalReport(Integer disputeId, String reportContent, String proposedAction) {
        accessService.requireRole("STAFF", "ADMIN");
        // STAFF BAN HANH BAO CAO THAM DINH VA DE XUAT HUONG XU LY TRANH CHAP.
        if (reportContent == null || reportContent.isBlank()) throw new AppException("TECHNICAL REPORT KHONG DUOC DE TRONG");
        DisputeEntity dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DISPUTE"));
        dispute.setEvidenceReport(reportContent);
        if (proposedAction != null && !proposedAction.isBlank()) dispute.setProposedAction(proposedAction);
        dispute.setStatus("Escalated");
        return disputeRepository.save(dispute);
    }

    @Transactional
    public TransactionEntity processPaymentWebhook(Long transactionId, String paymentStatus, String bankTxCode, String receiptImgUrl) {
        accessService.requireRole("ADMIN", "STAFF");
        // MO PHONG XU LY WEBHOOK THANH TOAN: CAP NHAT TRANG THAI GIAO DICH VA GHI HOA DON DOI SOAT.
        if (!List.of("Success", "Failed").contains(paymentStatus)) {
            throw new AppException("PAYMENT STATUS KHONG HOP LE");
        }
        TransactionEntity transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY TRANSACTION"));
        transaction.setStatus(paymentStatus);
        transaction = transactionRepository.save(transaction);

        InvoiceEntity invoice = invoiceRepository.findByTransactionId(transactionId)
                .orElseGet(() -> InvoiceEntity.builder().transactionId(transactionId).build());
        invoice.setBankTxCode(bankTxCode);
        invoice.setReceiptImgUrl(receiptImgUrl);
        invoiceRepository.save(invoice);
        return transaction;
    }

    private boolean isAutoAssignStaffEnabled() {
        return systemSettingRepository.findById("auto_assign_staff_enabled")
                .map(SystemSettingEntity::getSettingValue)
                .map(String::trim)
                .map(String::toLowerCase)
                .map(v -> v.equals("true") || v.equals("1"))
                .orElse(false);
    }
}
