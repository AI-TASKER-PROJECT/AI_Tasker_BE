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
        return jobRepository.save(input);
    }

    public List<JobEntity> listJobs() { return jobRepository.findByStatusOrderByPublishedAtDescCreatedAtDesc("OPEN"); }

    // Note: Hàm `listMyJobs` lấy toàn bộ job của business hiện tại, bao gồm DRAFT để doanh nghiệp kiểm tra trước khi public.
    public List<JobEntity> listMyJobs() {
        accessService.requireRole("BUSINESS");
        BusinessProfileEntity business = currentApprovedBusiness();
        return jobRepository.findByBusinessIdOrderByCreatedAtDesc(business.getBusinessId());
    }

    public JobEntity getJob(Integer id) {
        JobEntity job = jobRepository.findById(id).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        if ("OPEN".equalsIgnoreCase(job.getStatus())) return job;
        if (!SecurityUtils.hasRole("BUSINESS")) {
            throw new NotFoundException("JOB CHUA DUOC PUBLIC");
        }
        BusinessProfileEntity business = currentApprovedBusiness();
        if (!business.getBusinessId().equals(job.getBusinessId())) {
            throw new ForbiddenException("BAN KHONG CO QUYEN XEM JOB NAY");
        }
        return job;
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `submitProposal` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public ProposalEntity submitProposal(ProposalEntity input) {
        accessService.requireRole("EXPERT");
        // CHI CHO EXPERT DA KYC APPROVED NOP PROPOSAL CHO JOB DA MO CONG KHAI.
        JobEntity job = jobRepository.findById(input.getJobId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        if (!"OPEN".equalsIgnoreCase(job.getStatus())) {
            throw new AppException("JOB KHONG O TRANG THAI CHO PHEP NOP PROPOSAL");
        }
        if (input.getTechnicalSolution() == null || input.getTechnicalSolution().isBlank()) throw new AppException("TECHNICAL SOLUTION KHONG DUOC DE TRONG");
        if (input.getBidAmount() == null || input.getBidAmount().signum() <= 0) throw new AppException("BID AMOUNT PHAI LON HON 0");
        Integer expertId = currentApprovedExpert().getExpertId();
        if (proposalRepository.existsByJobIdAndExpertId(input.getJobId(), expertId)) {
            throw new com.aitasker.be.common.exception.ResourceConflictException("DA TON TAI PROPOSAL CHO JOB NAY");
        }
        input.setExpertId(expertId);
        input.setStatus(input.getStatus() == null ? "Pending" : input.getStatus());
        return proposalRepository.save(input);
    }

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
        return jobRepository.save(job);
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
        return proposalRepository.save(proposal);
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
