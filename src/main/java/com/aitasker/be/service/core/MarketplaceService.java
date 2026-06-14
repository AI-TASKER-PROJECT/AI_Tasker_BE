/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/MarketplaceService.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.ForbiddenException;
import com.aitasker.be.dto.core.ProposalRequest;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import com.aitasker.be.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

// Note: Annotation này cho Spring quản lý class như một service chứa nghiệp vụ.
@Service
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class MarketplaceService {
    private final AccessService accessService;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;
    private final SowRepository sowRepository;
    private final MilestoneRepository milestoneRepository;
    private final AcceptanceCriteriaRepository criteriaRepository;
    private final MilestoneAcceptanceCriteriaRepository milestoneCriteriaRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final FirebaseStorageService firebaseStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `createJob` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public JobEntity createJob(JobEntity input) {
        accessService.requireRole("BUSINESS");
        // VALIDATE CAC TRUONG BAT BUOC CUA JOB DE DUNG VOI BR DANG TUYEN BAI TOAN.
        if (input.getTitle() == null || input.getTitle().isBlank()) throw new AppException("TITLE KHONG DUOC DE TRONG");
        if (input.getRawRequirements() == null || input.getRawRequirements().isBlank()) throw new AppException("RAW REQUIREMENTS KHONG DUOC DE TRONG");
        if (input.getBudget() == null || input.getBudget().signum() <= 0) throw new AppException("BUDGET PHAI LON HON 0");
        SowEntity sow = input.getSow();
        List<MilestoneEntity> milestones = input.getMilestones();
        BusinessProfileEntity business = currentApprovedBusiness();
        input.setJobId(null);
        input.setBusinessId(business.getBusinessId());
        input.setStatus("DRAFT");
        input.setPublishedAt(null);
        JobEntity saved = jobRepository.save(input);
        saveSow(saved.getJobId(), sow);
        saveMilestones(saved, milestones);
        auditLogService.record(AuditLogService.ACTION_CREATE_JOB_DRAFT, "jobs", String.valueOf(saved.getJobId()), accessService.currentAccount().getAccountId());
        return attachJobDetails(saved);
    }

    // Note: Hàm `listJobs` chỉ lấy job OPEN để marketplace không làm lộ job nháp của doanh nghiệp.
    public List<JobEntity> listJobs() {
        return attachJobDetails(jobRepository.findByStatusOrderByPublishedAtDescCreatedAtDesc("OPEN"));
    }

    // Note: Hàm `listMyJobs` lấy toàn bộ job của business hiện tại, bao gồm DRAFT để doanh nghiệp kiểm tra trước khi public.
    public List<JobEntity> listMyJobs() {
        accessService.requireRole("BUSINESS");
        BusinessProfileEntity business = currentApprovedBusiness();
        return attachJobDetails(jobRepository.findByBusinessIdOrderByCreatedAtDesc(business.getBusinessId()));
    }

    // Note: Hàm `getJob` kiểm soát quyền xem chi tiết job theo trạng thái public hoặc quyền sở hữu job nháp.
    public JobEntity getJob(Integer id) {
        JobEntity job = jobRepository.findById(id).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        if ("OPEN".equalsIgnoreCase(job.getStatus())) return attachJobDetails(job);
        if (!SecurityUtils.hasRole("BUSINESS")) {
            throw new NotFoundException("JOB CHUA DUOC PUBLIC");
        }
        BusinessProfileEntity business = currentApprovedBusiness();
        if (!business.getBusinessId().equals(job.getBusinessId())) {
            throw new ForbiddenException("BAN KHONG CO QUYEN XEM JOB NAY");
        }
        return attachJobDetails(job);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `submitProposal` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public ProposalEntity submitProposal(ProposalRequest request) {
        accessService.requireRole("EXPERT");
        // CHI CHO EXPERT DA KYC APPROVED NOP PROPOSAL CHO JOB DA MO CONG KHAI.
        if (request.getJobId() == null) throw new AppException("JOB ID KHONG DUOC DE TRONG");
        JobEntity job = jobRepository.findById(request.getJobId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        if (!"OPEN".equalsIgnoreCase(job.getStatus())) {
            throw new AppException("JOB KHONG O TRANG THAI CHO PHEP NOP PROPOSAL");
        }
        if (request.getTechnicalSolution() == null || request.getTechnicalSolution().isBlank()) throw new AppException("TECHNICAL SOLUTION KHONG DUOC DE TRONG");
        if (request.getBidAmount() == null || request.getBidAmount().signum() <= 0) throw new AppException("BID AMOUNT PHAI LON HON 0");
        if (request.getProposalDescription() == null || request.getProposalDescription().isBlank()) {
            throw new AppException("PROPOSAL DESCRIPTION KHONG DUOC DE TRONG");
        }
        ExpertProfileEntity expert = currentApprovedExpert();
        Integer expertId = expert.getExpertId();
        if (proposalRepository.existsByJobIdAndExpertIdAndStatusNotIgnoreCase(request.getJobId(), expertId, "Rejected")) {
            throw new com.aitasker.be.common.exception.ResourceConflictException("DA TON TAI PROPOSAL CHO JOB NAY");
        }
        ProposalEntity input = ProposalEntity.builder()
                .jobId(request.getJobId())
                .technicalSolution(request.getTechnicalSolution())
                .proposalDescription(request.getProposalDescription())
                .proposalFileUrl(request.getProposalFileUrl())
                .bidAmount(request.getBidAmount())
                .build();
        input.assignProposalMilestone(normalizeProposalMilestone(request.proposalMilestoneText(), job, request.getBidAmount()));
        input.setExpertId(expertId);
        input.setStatus("Pending");
        ProposalEntity saved = proposalRepository.save(input);
        auditLogService.record(AuditLogService.ACTION_SUBMIT_PROPOSAL, "proposals", String.valueOf(saved.getProposalId()), accessService.currentAccount().getAccountId());
        businessProfileRepository.findById(job.getBusinessId())
                .ifPresent(business -> notificationService.notifyProposalCreated(
                        business.getAccountId(),
                        accessService.currentAccount().getAccountId(),
                        job.getJobId(),
                        job.getTitle()
                ));
        return saved;
    }

    // Note: Hàm `uploadProposalFile` upload file proposal của chuyên gia lên Firebase và trả path để gán vào proposal.
    public String uploadProposalFile(MultipartFile file) {
        accessService.requireRole("EXPERT");
        ExpertProfileEntity expert = currentApprovedExpert();
        return firebaseStorageService.upload(file, "proposal-files/experts/" + expert.getExpertId());
    }

    // Note: Hàm `listProposalsByJob` lấy proposal theo job và chỉ cho doanh nghiệp sở hữu job xem danh sách này.
    public List<ProposalEntity> listProposalsByJob(Integer jobId) {
        accessService.requireRole("BUSINESS");
        requireBusinessOwnedJob(jobId);
        return proposalRepository.findByJobId(jobId);
    }

    // Note: Hàm `listMyProposals` lấy các proposal của expert hiện tại để chuyên gia theo dõi trạng thái sau khi nộp.
    public List<ProposalEntity> listMyProposals() {
        accessService.requireRole("EXPERT");
        Integer expertId = currentApprovedExpert().getExpertId();
        return proposalRepository.findByExpertIdOrderByCreatedAtDesc(expertId);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `updateJobStatus` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public JobEntity updateJobStatus(Integer jobId, String status) {
        accessService.requireRole("BUSINESS");
        // KIEM TRA STATUS JOB DE DAM BAO DUNG VOI VONG DOI TUYEN DUNG.
        if (!List.of("DRAFT", "OPEN", "CLOSED", "CANCELLED").contains(status)) {
            throw new AppException("STATUS JOB KHONG HOP LE");
        }
        JobEntity job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        BusinessProfileEntity business = currentApprovedBusiness();
        if (!business.getBusinessId().equals(job.getBusinessId())) {
            throw new AppException("BAN KHONG CO QUYEN CAP NHAT JOB NAY");
        }
        job.setStatus(status);
        if ("OPEN".equals(status) && job.getPublishedAt() == null) job.setPublishedAt(LocalDateTime.now());
        JobEntity saved = jobRepository.save(job);
        auditLogService.record(AuditLogService.ACTION_CHANGE_JOB_STATUS, "jobs", String.valueOf(jobId), accessService.currentAccount().getAccountId());
        return attachJobDetails(saved);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `reviewProposal` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public ProposalEntity reviewProposal(Integer proposalId, String status) {
        accessService.requireRole("BUSINESS");
        // CHI CHO PHEP DOANH NGHIEP PHE DUYET HOAC TU CHOI PROPOSAL.
        if (!List.of("Accepted", "Rejected").contains(status)) {
            throw new AppException("STATUS PROPOSAL KHONG HOP LE");
        }
        ProposalEntity proposal = proposalRepository.findById(proposalId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY PROPOSAL"));
        JobEntity job = jobRepository.findById(proposal.getJobId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB CUA PROPOSAL"));
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer businessId = businessProfileRepository.findByAccountId(accountId)
                .map(BusinessProfileEntity::getBusinessId)
                .orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
        if (!businessId.equals(job.getBusinessId())) {
            throw new AppException("BAN KHONG CO QUYEN REVIEW PROPOSAL NAY");
        }
        proposal.setStatus(status);
        ProposalEntity saved = proposalRepository.save(proposal);
        auditLogService.record(AuditLogService.ACTION_REVIEW_PROPOSAL, "proposals", String.valueOf(proposalId), accountId);
        expertProfileRepository.findById(proposal.getExpertId())
                .ifPresent(expert -> notificationService.notifyProposalReviewed(
                        expert.getAccountId(),
                        accountId,
                        job.getJobId(),
                        job.getTitle(),
                        status
                ));
        return saved;
    }

    // Note: Hàm `requireBusinessOwnedJob` kiểm tra job thuộc về business hiện tại trước khi cho xem proposal hoặc cập nhật job.
    private JobEntity requireBusinessOwnedJob(Integer jobId) {
        JobEntity job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        BusinessProfileEntity business = currentApprovedBusiness();
        if (!business.getBusinessId().equals(job.getBusinessId())) {
            throw new AppException("BAN KHONG CO QUYEN THAO TAC JOB NAY");
        }
        return job;
    }

    // Note: Hàm `currentApprovedBusiness` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    // Note: Hàm `attachProposalCounts` gắn tổng số proposal cho từng job trước khi trả response cho giao diện.
    private List<JobEntity> attachProposalCounts(List<JobEntity> jobs) {
        jobs.forEach(this::attachProposalCount);
        return jobs;
    }

    // Note: Hàm `attachProposalCount` đếm proposal theo job và đưa vào field tạm, không làm thay đổi schema bảng jobs.
    private JobEntity attachProposalCount(JobEntity job) {
        if (job != null && job.getJobId() != null) {
            job.setProposalsCount(proposalRepository.countByJobId(job.getJobId()));
        }
        return job;
    }

    private void saveSow(Integer jobId, SowEntity sow) {
        if (sow == null) return;
        if (sow.getTitle() == null || sow.getTitle().isBlank()) throw new AppException("SOW TITLE KHONG DUOC DE TRONG");
        sow.setSowId(null);
        sow.setJobId(jobId);
        sowRepository.save(sow);
    }

    private void saveMilestones(JobEntity job, List<MilestoneEntity> milestones) {
        if (milestones == null || milestones.isEmpty()) return;
        Set<Integer> orderIndexes = new LinkedHashSet<>();
        int defaultOrderIndex = 1;
        for (MilestoneEntity milestone : milestones) {
            if (milestone.getOrderIndex() == null) milestone.setOrderIndex(defaultOrderIndex);
            if (milestone.getMilestoneName() == null || milestone.getMilestoneName().isBlank()) throw new AppException("MILESTONE NAME KHONG DUOC DE TRONG");
            if (milestone.getFundsAllocated() == null || milestone.getFundsAllocated().signum() < 0) throw new AppException("FUNDS ALLOCATED KHONG HOP LE");
            if (milestone.getOrderIndex() == null || milestone.getOrderIndex() <= 0) throw new AppException("ORDER INDEX PHAI LON HON 0");
            if (!orderIndexes.add(milestone.getOrderIndex())) throw new AppException("ORDER INDEX BI TRUNG TRONG MILESTONE");
            milestone.setMilestoneId(null);
            milestone.setJobId(job.getJobId());
            milestone.setContractId(null);
            if (milestone.getStatus() == null) milestone.setStatus("Pending");
            MilestoneEntity saved = milestoneRepository.save(milestone);
            replaceMilestoneCriteria(saved.getMilestoneId(), milestone.getCriteriaIds());
            defaultOrderIndex++;
        }
    }

    private List<JobEntity> attachJobDetails(List<JobEntity> jobs) {
        jobs.forEach(this::attachJobDetails);
        return jobs;
    }

    private JobEntity attachJobDetails(JobEntity job) {
        if (job != null && job.getJobId() != null) {
            job.setProposalsCount(proposalRepository.countByJobId(job.getJobId()));
            job.setSow(sowRepository.findByJobId(job.getJobId()).orElse(null));
            job.setMilestones(attachMilestoneCriteria(milestoneRepository.findByJobIdOrderByOrderIndexAsc(job.getJobId())));
        }
        return job;
    }

    private List<MilestoneEntity> attachMilestoneCriteria(List<MilestoneEntity> milestones) {
        milestones.forEach(this::attachMilestoneCriteria);
        return milestones;
    }

    private MilestoneEntity attachMilestoneCriteria(MilestoneEntity milestone) {
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
            if (!Boolean.TRUE.equals(criteria.getIsActive())) throw new AppException("ACCEPTANCE CRITERIA KHONG CON HOAT DONG " + criteriaId);
            milestoneCriteriaRepository.save(MilestoneAcceptanceCriteriaEntity.builder()
                    .id(new MilestoneAcceptanceCriteriaId(milestoneId, criteriaId))
                    .build());
        }
    }

    private String normalizeProposalMilestone(String proposalMilestone, JobEntity job, BigDecimal bidAmount) {
        if (proposalMilestone == null || proposalMilestone.isBlank()) {
            return null;
        }

        List<MilestoneEntity> jobMilestones = milestoneRepository.findByJobIdOrderByOrderIndexAsc(job.getJobId());
        if (jobMilestones.isEmpty()) {
            throw new AppException("JOB CHUA CO MILESTONE DE DE XUAT NGAN SACH");
        }

        Map<Integer, MilestoneEntity> milestonesById = jobMilestones.stream()
                .collect(Collectors.toMap(MilestoneEntity::getMilestoneId, item -> item));
        ArrayNode normalized = objectMapper.createArrayNode();
        Set<Integer> seenMilestones = new LinkedHashSet<>();
        BigDecimal total = BigDecimal.ZERO;

        try {
            JsonNode root = objectMapper.readTree(proposalMilestone);
            if (!root.isArray()) {
                throw new AppException("PROPOSAL MILESTONE PHAI LA JSON ARRAY");
            }

            for (JsonNode item : root) {
                Integer milestoneId = readRequiredInteger(item, "milestoneId");
                BigDecimal proposedBudget = readRequiredBudget(item, "proposedBudget");
                if (!milestonesById.containsKey(milestoneId)) {
                    throw new AppException("MILESTONE DE XUAT KHONG THUOC JOB NAY " + milestoneId);
                }
                if (!seenMilestones.add(milestoneId)) {
                    throw new AppException("MILESTONE DE XUAT BI TRUNG " + milestoneId);
                }

                ObjectNode normalizedItem = objectMapper.createObjectNode();
                normalizedItem.put("milestoneId", milestoneId);
                normalizedItem.put("proposedBudget", proposedBudget);
                normalized.add(normalizedItem);
                total = total.add(proposedBudget);
            }
        } catch (JsonProcessingException ex) {
            throw new AppException("PROPOSAL MILESTONE KHONG PHAI JSON HOP LE");
        }

        if (seenMilestones.size() != jobMilestones.size()) {
            throw new AppException("PROPOSAL MILESTONE PHAI GIU NGUYEN SO LUONG MILESTONE CUA JOB");
        }
        if (bidAmount != null && total.compareTo(bidAmount) != 0) {
            throw new AppException("TONG NGAN SACH MILESTONE DE XUAT PHAI BANG BID AMOUNT");
        }

        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            throw new AppException("KHONG CHUAN HOA DUOC PROPOSAL MILESTONE");
        }
    }

    private Integer readRequiredInteger(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.get(fieldName);
        if (value == null || !value.canConvertToInt()) {
            throw new AppException(fieldName.toUpperCase() + " KHONG HOP LE");
        }
        return value.asInt();
    }

    private BigDecimal readRequiredBudget(JsonNode node, String fieldName) {
        JsonNode value = node == null ? null : node.get(fieldName);
        if (value == null || !value.isNumber()) {
            throw new AppException(fieldName.toUpperCase() + " KHONG HOP LE");
        }
        BigDecimal budget = value.decimalValue();
        if (budget.signum() <= 0) {
            throw new AppException(fieldName.toUpperCase() + " PHAI LON HON 0");
        }
        return budget;
    }

    private BusinessProfileEntity currentApprovedBusiness() {
        accessService.requireApprovedAccount();
        Integer accountId = accessService.currentAccount().getAccountId();
        BusinessProfileEntity business = businessProfileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
        if (!"Approved".equalsIgnoreCase(business.getKybStatus())) {
            throw new AppException("BUSINESS PROFILE CHUA DUOC KYB APPROVED");
        }
        return business;
    }

    // Note: Hàm `currentApprovedExpert` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private ExpertProfileEntity currentApprovedExpert() {
        accessService.requireApprovedAccount();
        Integer accountId = accessService.currentAccount().getAccountId();
        ExpertProfileEntity expert = expertProfileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new NotFoundException("CHUA CO EXPERT PROFILE"));
        if (!"Approved".equalsIgnoreCase(expert.getKycStatus())) {
            throw new AppException("EXPERT PROFILE CHUA DUOC KYC APPROVED");
        }
        return expert;
    }

}
