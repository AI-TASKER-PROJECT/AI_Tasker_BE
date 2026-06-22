/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/ContractExecutionService.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.dto.core.ContractMilestoneViewResponse;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final ContractMilestoneRepository contractMilestoneRepository;
    private final MilestoneRepository milestoneRepository;
    private final AcceptanceCriteriaRepository criteriaRepository;
    private final MilestoneAcceptanceCriteriaRepository milestoneCriteriaRepository;
    private final DeliverableRepository deliverableRepository;
    private final TransactionRepository transactionRepository;
    private final DisputeRepository disputeRepository;
    private final StaffRepository staffRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final SystemWalletService systemWalletService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
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
                        "Hop dong nhap moi",
                        "Doanh nghiep da tao hop dong nhap de ban xem xet.",
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
                "Hop dong da duoc xac nhan",
                "Ben con lai da ky xac nhan hop dong.");
        if ("PENDING".equals(saved.getStatus())) {
            notifyBothParticipants(saved, accountId, "CONTRACT_PENDING_DEPOSIT", "Hop dong cho ky quy", "Hop dong da du chu ky Contract va NDA, doanh nghiep can thanh toan ky quy de bat dau du an.");
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
                    .build());
        }
    }

    // Note: Hàm `buildCriteriaSnapshot` dựng văn bản tiêu chí nghiệm thu tại thời điểm tạo contract để snapshot không bị thay đổi sau này.
    private String buildCriteriaSnapshot(Integer milestoneId) {
        List<MilestoneAcceptanceCriteriaEntity> links = milestoneCriteriaRepository.findByIdMilestoneId(milestoneId);
        if (links == null || links.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (MilestoneAcceptanceCriteriaEntity link : links) {
            if (link.getId() == null || link.getId().getCriteriaId() == null) continue;
            criteriaRepository.findById(link.getId().getCriteriaId())
                    .filter(AcceptanceCriteriaEntity::getIsActive)
                    .map(AcceptanceCriteriaEntity::getDescription)
                    .ifPresent(description -> {
                        if (sb.length() > 0) sb.append("\n");
                        sb.append(description);
                    });
        }
        return sb.length() > 0 ? sb.toString() : null;
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
                "NDA da duoc ky",
                "Ben con lai da ky NDA cho hop dong.");
        if ("PENDING".equals(saved.getStatus())) {
            notifyBothParticipants(saved, accountId, "CONTRACT_PENDING_DEPOSIT", "Hop dong cho ky quy", "Hop dong da du chu ky Contract va NDA, doanh nghiep can thanh toan ky quy de bat dau du an.");
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
                        "Hop dong bi tu choi",
                        "Chuyen gia da tu choi hop dong nhap. Job duoc chuyen ve buoc review proposal.",
                        saved.getContractId()
                ));
        return saved;
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `terminateContract` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public ContractEntity terminateContract(Integer contractId, String reason) {
        // CHI BUSINESS HOAC ADMIN DUOC CHAM DUT CONTRACT KHI CHUA HOAN TAT.
        accessService.requireRole("BUSINESS", "ADMIN");
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        if ("COMPLETED".equals(contract.getStatus()) || "CANCELLED".equals(contract.getStatus())) {
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
        contract.setStatus("CANCELLED");
        contract.setUpdatedAt(LocalDateTime.now());
        ContractEntity saved = contractRepository.save(contract);
        auditLogService.record(AuditLogService.ACTION_TERMINATE_CONTRACT, "contracts", String.valueOf(contractId), actor.getAccountId());
        return saved;
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
        replaceMilestoneCriteria(saved.getMilestoneId(), input.getCriteriaIds());
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
        MilestoneEntity saved = milestoneRepository.save(existing);
        auditLogService.record(AuditLogService.ACTION_UPDATE_MILESTONE, "milestones", String.valueOf(milestoneId), accessService.currentAccount().getAccountId());
        return attachCriteria(saved);
    }
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Transactional public AcceptanceCriteriaEntity createCriteria(AcceptanceCriteriaEntity input) {
        accessService.requireRole("ADMIN");
        if (input.getCriteriaCode() == null || input.getCriteriaCode().isBlank()) throw new AppException("CRITERIA CODE KHONG DUOC DE TRONG");
        if (input.getDescription() == null || input.getDescription().isBlank()) throw new AppException("CRITERIA DESCRIPTION KHONG DUOC DE TRONG");
        String code = input.getCriteriaCode().trim().replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "").toUpperCase();
        criteriaRepository.findByCriteriaCode(code).ifPresent(existing -> { throw new AppException("CRITERIA CODE DA TON TAI"); });
        input.setCriteriaId(null);
        input.setCriteriaCode(code);
        input.setIsActive(input.getIsActive() == null || input.getIsActive());
        input.setSortOrder(input.getSortOrder() == null ? 0 : input.getSortOrder());
        AcceptanceCriteriaEntity saved = criteriaRepository.save(input);
        auditLogService.record(AuditLogService.ACTION_CREATE_ACCEPTANCE_CRITERIA, "acceptance_criteria", String.valueOf(saved.getCriteriaId()), accessService.currentAccount().getAccountId());
        return saved;
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
        DeliverableEntity saved = deliverableRepository.save(input);
        milestone.setStatus("UNDER_REVIEW");
        milestone.setUpdatedAt(LocalDateTime.now());
        milestoneRepository.save(milestone);
        auditLogService.record(AuditLogService.ACTION_SUBMIT_DELIVERABLE, "milestones", String.valueOf(milestone.getMilestoneId()), accessService.currentAccount().getAccountId());
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
    public MilestoneEntity completeMilestone(Integer milestoneId) {
        accessService.requireRole("BUSINESS");
        accessService.requireApprovedAccount();
        MilestoneEntity milestone = milestoneRepository.findById(milestoneId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE"));
        ContractEntity contract = requireBusinessOwnedContract(
                Optional.ofNullable(milestone.getContractId())
                        .orElseGet(() -> contractRepository.findByJobId(milestone.getJobId())
                                .map(ContractEntity::getContractId)
                                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT CUA MILESTONE")))
        );
        if (!"ACTIVE".equals(contract.getStatus())) throw new AppException("CHI DUOC HOAN TAT MILESTONE KHI CONTRACT ACTIVE");
        if (!"UNDER_REVIEW".equals(milestone.getStatus())) throw new AppException("MILESTONE CHUA O TRANG THAI CHO DUYET");
        milestone.setStatus("COMPLETED");
        milestone.setUpdatedAt(LocalDateTime.now());
        MilestoneEntity saved = milestoneRepository.save(milestone);
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        auditLogService.record(AuditLogService.ACTION_COMPLETE_MILESTONE, "milestones", String.valueOf(milestoneId), actorAccountId);
        tryCompleteContract(contract, actorAccountId);
        return saved;
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
        auditLogService.record(AuditLogService.ACTION_COMPLETE_CONTRACT, "contracts", String.valueOf(saved.getContractId()), actorAccountId);
        notifyBothParticipants(saved, actorAccountId, "CONTRACT_COMPLETED", "Hop dong da hoan tat", "Tat ca milestone cua hop dong da hoan thanh.");
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
        return attachCriteria(milestone).getCriteria();
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
        auditLogService.record(AuditLogService.ACTION_ASSIGN_DISPUTE, "disputes", String.valueOf(disputeId), accessService.currentAccount().getAccountId());
        staffRepository.findById(staffId)
                .ifPresent(staff -> notificationService.notifyDisputeAssigned(
                        staff.getAccountId(),
                        accessService.currentAccount().getAccountId(),
                        disputeId
                ));
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
        auditLogService.record(AuditLogService.ACTION_RESOLVE_DISPUTE, "disputes", String.valueOf(disputeId), accessService.currentAccount().getAccountId());
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
        Integer actorAccountId = accessService.currentAccount().getAccountId();
        for (MilestoneEntity milestone : milestoneRepository.findAll()) {
            if (!"UNDER_REVIEW".equals(milestone.getStatus()) && !"PENDING".equals(milestone.getStatus())) continue;
            List<DeliverableEntity> deliverables = deliverableRepository.findByMilestoneId(milestone.getMilestoneId());
            if (deliverables.isEmpty()) continue;
            LocalDateTime lastSubmission = deliverables.stream()
                    .map(DeliverableEntity::getCreatedAt)
                    .filter(java.util.Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            if (lastSubmission == null) continue;
            if (!lastSubmission.plusDays(slaDays).isAfter(now)) {
                milestone.setStatus("COMPLETED");
                milestone.setUpdatedAt(now);
                MilestoneEntity saved = milestoneRepository.save(milestone);
                updated.add(saved);
                findContractForMilestone(saved).ifPresent(contract -> tryCompleteContract(contract, actorAccountId));
            }
        }
        auditLogService.record(AuditLogService.ACTION_RUN_SLA_AUTO_APPROVE, "system_settings", "default_sla_days", actorAccountId);
        return updated;
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
        List<Integer> criteriaIds = milestoneCriteriaRepository.findByIdMilestoneId(milestone.getMilestoneId()).stream()
                .map(item -> item.getId().getCriteriaId())
                .toList();
        milestone.setCriteriaIds(criteriaIds);
        if (criteriaIds.isEmpty()) {
            milestone.setCriteria(List.of());
            return milestone;
        }
        Map<Integer, AcceptanceCriteriaEntity> criteriaById = criteriaRepository.findAllById(criteriaIds).stream()
                .collect(Collectors.toMap(AcceptanceCriteriaEntity::getCriteriaId, item -> item));
        milestone.setCriteria(criteriaIds.stream()
                .map(criteriaById::get)
                .filter(Objects::nonNull)
                .toList());
        return milestone;
    }

    private void replaceMilestoneCriteria(Integer milestoneId, List<Integer> criteriaIds) {
        milestoneCriteriaRepository.deleteByIdMilestoneId(milestoneId);
        if (criteriaIds == null || criteriaIds.isEmpty()) return;
        for (Integer criteriaId : new LinkedHashSet<>(criteriaIds)) {
            AcceptanceCriteriaEntity criteria = criteriaRepository.findById(criteriaId)
                    .orElseThrow(() -> new NotFoundException("KHONG TIM THAY ACCEPTANCE CRITERIA " + criteriaId));
            if (!Boolean.TRUE.equals(criteria.getIsActive())) {
                throw new AppException("ACCEPTANCE CRITERIA KHONG CON HOAT DONG " + criteriaId);
            }
            milestoneCriteriaRepository.save(MilestoneAcceptanceCriteriaEntity.builder()
                    .id(new MilestoneAcceptanceCriteriaId(milestoneId, criteriaId))
                    .build());
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
