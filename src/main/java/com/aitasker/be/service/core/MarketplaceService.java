package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketplaceService {
    private final AccessService accessService;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;

    @Transactional
    public JobEntity createJob(JobEntity input) {
        accessService.requireRole("BUSINESS");
        // VALIDATE CAC TRUONG BAT BUOC CUA JOB DE DUNG VOI BR DANG TUYEN BAI TOAN.
        if (input.getTitle() == null || input.getTitle().isBlank()) throw new AppException("TITLE KHONG DUOC DE TRONG");
        if (input.getRawRequirements() == null || input.getRawRequirements().isBlank()) throw new AppException("RAW REQUIREMENTS KHONG DUOC DE TRONG");
        if (input.getBudget() == null || input.getBudget().signum() <= 0) throw new AppException("BUDGET PHAI LON HON 0");
        input.setStatus(input.getStatus() == null ? "DRAFT" : input.getStatus());
        if (!List.of("DRAFT", "OPEN", "CLOSED", "CANCELLED").contains(input.getStatus())) {
            throw new AppException("STATUS JOB KHONG HOP LE");
        }
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer businessId = businessProfileRepository.findByAccountId(accountId).map(BusinessProfileEntity::getBusinessId).orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
        input.setJobId(null);
        input.setBusinessId(businessId);
        return jobRepository.save(input);
    }

    public List<JobEntity> listJobs() { return jobRepository.findAll(); }

    public JobEntity getJob(Integer id) { return jobRepository.findById(id).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB")); }

    @Transactional
    public ProposalEntity submitProposal(ProposalEntity input) {
        accessService.requireRole("EXPERT");
        // KIEM TRA JOB TON TAI VA CHO PHEP DAU THAU TRUOC KHI NOP PROPOSAL.
        JobEntity job = jobRepository.findById(input.getJobId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        if (!"OPEN".equalsIgnoreCase(job.getStatus()) && !"DRAFT".equalsIgnoreCase(job.getStatus())) {
            throw new AppException("JOB KHONG O TRANG THAI CHO PHEP NOP PROPOSAL");
        }
        if (input.getTechnicalSolution() == null || input.getTechnicalSolution().isBlank()) throw new AppException("TECHNICAL SOLUTION KHONG DUOC DE TRONG");
        if (input.getBidAmount() == null || input.getBidAmount().signum() <= 0) throw new AppException("BID AMOUNT PHAI LON HON 0");
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer expertId = expertProfileRepository.findByAccountId(accountId).map(ExpertProfileEntity::getExpertId).orElseThrow(() -> new NotFoundException("CHUA CO EXPERT PROFILE"));
        if (proposalRepository.existsByJobIdAndExpertId(input.getJobId(), expertId)) {
            throw new com.aitasker.be.common.exception.ResourceConflictException("DA TON TAI PROPOSAL CHO JOB NAY");
        }
        input.setExpertId(expertId);
        input.setStatus(input.getStatus() == null ? "Pending" : input.getStatus());
        return proposalRepository.save(input);
    }

    public List<ProposalEntity> listProposalsByJob(Integer jobId) { return proposalRepository.findByJobId(jobId); }

    @Transactional
    public JobEntity updateJobStatus(Integer jobId, String status) {
        accessService.requireRole("BUSINESS");
        // KIEM TRA STATUS JOB DE DAM BAO DUNG VOI VONG DOI TUYEN DUNG.
        if (!List.of("DRAFT", "OPEN", "CLOSED", "CANCELLED").contains(status)) {
            throw new AppException("STATUS JOB KHONG HOP LE");
        }
        JobEntity job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer businessId = businessProfileRepository.findByAccountId(accountId)
                .map(BusinessProfileEntity::getBusinessId)
                .orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
        if (!businessId.equals(job.getBusinessId())) {
            throw new AppException("BAN KHONG CO QUYEN CAP NHAT JOB NAY");
        }
        job.setStatus(status);
        return jobRepository.save(job);
    }

    @Transactional
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
}
