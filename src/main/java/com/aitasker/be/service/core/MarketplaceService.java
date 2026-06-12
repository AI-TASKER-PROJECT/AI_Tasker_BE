/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/MarketplaceService.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.ForbiddenException;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import com.aitasker.be.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final JobDomainRepository jobDomainRepository;
    private final JobSkillRepository jobSkillRepository;
    private final PortfolioRepository portfolioRepository;
    private final ProposalRepository proposalRepository;
    private final AuditLogService auditLogService;

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `createJob` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public JobEntity createJob(JobEntity input) {
        accessService.requireRole("BUSINESS");
        // VALIDATE CAC TRUONG BAT BUOC CUA JOB DE DUNG VOI BR DANG TUYEN BAI TOAN.
        if (input.getTitle() == null || input.getTitle().isBlank()) throw new AppException("TITLE KHONG DUOC DE TRONG");
        if (input.getRawRequirements() == null || input.getRawRequirements().isBlank()) throw new AppException("RAW REQUIREMENTS KHONG DUOC DE TRONG");
        if (input.getBudget() == null || input.getBudget().signum() <= 0) throw new AppException("BUDGET PHAI LON HON 0");
        BusinessProfileEntity business = currentApprovedBusiness();
        input.setJobId(null);
        input.setBusinessId(business.getBusinessId());
        input.setStatus("DRAFT");
        input.setPublishedAt(null);
        JobEntity saved = jobRepository.save(input);
        auditLogService.record(AuditLogService.ACTION_CREATE_JOB_DRAFT, "jobs", String.valueOf(saved.getJobId()), accessService.currentAccount().getAccountId());
        return attachProposalCount(saved);
    }

    // Note: Hàm `listJobs` chỉ lấy job OPEN để marketplace không làm lộ job nháp của doanh nghiệp.
    public List<JobEntity> listJobs() {
        return attachProposalCounts(jobRepository.findByStatusOrderByPublishedAtDescCreatedAtDesc("OPEN"));
    }

    // Note: Hàm `listMyJobs` lấy toàn bộ job của business hiện tại, bao gồm DRAFT để doanh nghiệp kiểm tra trước khi public.
    public List<JobEntity> listMyJobs() {
        accessService.requireRole("BUSINESS");
        BusinessProfileEntity business = currentApprovedBusiness();
        return attachProposalCounts(jobRepository.findByBusinessIdOrderByCreatedAtDesc(business.getBusinessId()));
    }

    // Note: Hàm `getJob` kiểm soát quyền xem chi tiết job theo trạng thái public hoặc quyền sở hữu job nháp.
    public JobEntity getJob(Integer id) {
        JobEntity job = jobRepository.findById(id).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        if ("OPEN".equalsIgnoreCase(job.getStatus())) return attachProposalCount(job);
        if (!SecurityUtils.hasRole("BUSINESS")) {
            throw new NotFoundException("JOB CHUA DUOC PUBLIC");
        }
        BusinessProfileEntity business = currentApprovedBusiness();
        if (!business.getBusinessId().equals(job.getBusinessId())) {
            throw new ForbiddenException("BAN KHONG CO QUYEN XEM JOB NAY");
        }
        return attachProposalCount(job);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `submitProposal` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public ProposalEntity submitProposal(ProposalEntity input) {
        accessService.requireRole("EXPERT");
        // CHI CHO EXPERT DA KYC APPROVED NOP PROPOSAL CHO JOB DA MO CONG KHAI.
        if (input.getJobId() == null) throw new AppException("JOB ID KHONG DUOC DE TRONG");
        JobEntity job = jobRepository.findById(input.getJobId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        if (!"OPEN".equalsIgnoreCase(job.getStatus())) {
            throw new AppException("JOB KHONG O TRANG THAI CHO PHEP NOP PROPOSAL");
        }
        if (input.getTechnicalSolution() == null || input.getTechnicalSolution().isBlank()) throw new AppException("TECHNICAL SOLUTION KHONG DUOC DE TRONG");
        if (input.getBidAmount() == null || input.getBidAmount().signum() <= 0) throw new AppException("BID AMOUNT PHAI LON HON 0");
        ExpertProfileEntity expert = currentApprovedExpert();
        Integer expertId = expert.getExpertId();
        validateProposalFocus(input, expertId);
        if (proposalRepository.existsByJobIdAndExpertId(input.getJobId(), expertId)) {
            throw new com.aitasker.be.common.exception.ResourceConflictException("DA TON TAI PROPOSAL CHO JOB NAY");
        }
        input.setExpertId(expertId);
        input.setStatus(input.getStatus() == null ? "Pending" : input.getStatus());
        ProposalEntity saved = proposalRepository.save(input);
        auditLogService.record(AuditLogService.ACTION_SUBMIT_PROPOSAL, "proposals", String.valueOf(saved.getProposalId()), accessService.currentAccount().getAccountId());
        return attachProposalCount(saved);
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
        return saved;
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

    // Note: Hàm `validateProposalFocus` đảm bảo domain/skill chuyên gia chọn khi nộp proposal thuộc cả job đang mở và portfolio của chính chuyên gia.
    private void validateProposalFocus(ProposalEntity input, Integer expertId) {
        if (input.getDomainId() == null) throw new AppException("DOMAIN CUA PROPOSAL KHONG DUOC DE TRONG");
        if (input.getSkillId() == null) throw new AppException("SKILL CUA PROPOSAL KHONG DUOC DE TRONG");

        boolean jobHasDomain = jobDomainRepository.findByIdJobId(input.getJobId()).stream()
                .anyMatch(item -> input.getDomainId().equals(item.getId().getDomainId()));
        if (!jobHasDomain) throw new AppException("DOMAIN PROPOSAL KHONG THUOC JOB NAY");

        boolean jobHasSkill = jobSkillRepository.findByIdJobId(input.getJobId()).stream()
                .anyMatch(item -> input.getSkillId().equals(item.getId().getSkillId()));
        if (!jobHasSkill) throw new AppException("SKILL PROPOSAL KHONG THUOC JOB NAY");

        PortfolioEntity portfolio = portfolioRepository.findByExpertId(expertId)
                .orElseThrow(() -> new NotFoundException("CHUA CO PORTFOLIO DE NOP PROPOSAL"));
        Set<Integer> portfolioDomains = parseCatalogIds(portfolio.getDomainIds());
        Set<Integer> portfolioSkills = parseCatalogIds(portfolio.getSkillIds());
        if (!portfolioDomains.contains(input.getDomainId())) {
            throw new AppException("DOMAIN PROPOSAL KHONG NAM TRONG PORTFOLIO CUA CHUYEN GIA");
        }
        if (!portfolioSkills.contains(input.getSkillId())) {
            throw new AppException("SKILL PROPOSAL KHONG NAM TRONG PORTFOLIO CUA CHUYEN GIA");
        }
    }

    // Note: Hàm `parseCatalogIds` chuyển chuỗi id trong portfolio thành tập số để kiểm tra domain/skill nhanh và tránh trùng.
    private Set<Integer> parseCatalogIds(String value) {
        if (value == null || value.isBlank()) return Set.of();
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .map(Integer::valueOf)
                .collect(Collectors.toSet());
    }
}
