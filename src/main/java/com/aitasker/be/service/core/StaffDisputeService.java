package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.dto.core.StaffDisputeFilter;
import com.aitasker.be.dto.core.StaffDisputeListItem;
import com.aitasker.be.dto.core.StaffDisputeListResponse;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffDisputeService {
    private final AccessService accessService;
    private final StaffRepository staffRepository;
    private final DisputeRepository disputeRepository;
    private final ContractRepository contractRepository;
    private final JobRepository jobRepository;
    private final JobDomainRepository jobDomainRepository;
    private final JobSkillRepository jobSkillRepository;
    private final StaffDomainRepository staffDomainRepository;
    private final StaffSkillRepository staffSkillRepository;
    private final DomainRepository domainRepository;
    private final SkillRepository skillRepository;

    public StaffDisputeListResponse listDisputes(StaffDisputeFilter filter) {
        accessService.requireRole("STAFF");
        AccountEntity actor = accessService.currentAccount();
        Integer staffId = staffRepository.findByAccountId(actor.getAccountId())
                .map(StaffEntity::getStaffId)
                .orElseThrow(() -> new NotFoundException("CHUA CO STAFF PROFILE"));

        int page = filter.getPage();
        int size = filter.getSize();
        if (page < 0) throw new AppException("PAGE PHAI LON HON HOAC BANG 0");
        if (size < 1 || size > 100) throw new AppException("SIZE PHAI TU 1 DEN 100");

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt", "disputeId");
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<DisputeEntity> result;
        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            result = disputeRepository.findByAssignedStaffIdAndStatusOrderByCreatedAtDescDisputeIdDesc(
                    staffId, filter.getStatus().trim().toUpperCase(), pageable);
        } else {
            result = disputeRepository.findByAssignedStaffIdOrderByCreatedAtDescDisputeIdDesc(
                    staffId, pageable);
        }

        List<StaffDisputeListItem> items = result.getContent().stream()
                .map(this::toListItem)
                .toList();

        return StaffDisputeListResponse.builder()
                .content(items)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    private StaffDisputeListItem toListItem(DisputeEntity dispute) {
        List<Integer> jobDomainIds = contractRepository.findById(dispute.getContractId())
                .map(c -> jobDomainRepository.findByIdJobId(c.getJobId()).stream()
                        .map(jd -> jd.getId().getDomainId()).toList())
                .orElse(List.of());
        List<Integer> jobSkillIds = contractRepository.findById(dispute.getContractId())
                .map(c -> jobSkillRepository.findByIdJobId(c.getJobId()).stream()
                        .map(js -> js.getId().getSkillId()).toList())
                .orElse(List.of());
        List<String> jobDomainNames = domainRepository.findAllById(jobDomainIds).stream()
                .map(DomainEntity::getDomainName).toList();
        List<String> jobSkillNames = skillRepository.findAllById(jobSkillIds).stream()
                .map(SkillEntity::getSkillName).toList();
        List<Integer> staffDomainIds = staffDomainRepository.findByIdStaffId(
                dispute.getAssignedStaffId()).stream()
                .map(sd -> sd.getId().getDomainId()).toList();
        List<Integer> staffSkillIds = staffSkillRepository.findByIdStaffId(
                dispute.getAssignedStaffId()).stream()
                .map(ss -> ss.getId().getSkillId()).toList();
        List<Integer> matchedDomainIds = staffDomainIds.stream().filter(jobDomainIds::contains).toList();
        List<Integer> matchedSkillIds = staffSkillIds.stream().filter(jobSkillIds::contains).toList();
        List<String> matchedDomainNames = domainRepository.findAllById(matchedDomainIds).stream()
                .map(DomainEntity::getDomainName).toList();
        List<String> matchedSkillNames = skillRepository.findAllById(matchedSkillIds).stream()
                .map(SkillEntity::getSkillName).toList();
        String jobTitle = contractRepository.findById(dispute.getContractId())
                .flatMap(c -> jobRepository.findById(c.getJobId()))
                .map(JobEntity::getTitle).orElse(null);
        Integer jobId = contractRepository.findById(dispute.getContractId())
                .map(ContractEntity::getJobId).orElse(null);

        return StaffDisputeListItem.builder()
                .disputeId(dispute.getDisputeId())
                .contractId(dispute.getContractId())
                .milestoneId(dispute.getMilestoneId())
                .jobId(jobId)
                .jobTitle(jobTitle)
                .status(dispute.getStatus())
                .initiatedBy(dispute.getInitiatedBy())
                .initiationType(dispute.getInitiationType())
                .reason(dispute.getEscalationReason())
                .createdAt(dispute.getCreatedAt())
                .jobDomains(jobDomainNames)
                .jobSkills(jobSkillNames)
                .matchedStaffDomains(matchedDomainNames)
                .matchedStaffSkills(matchedSkillNames)
                .evidenceCollectionDueAt(dispute.getEvidenceCollectionDueAt())
                .staffSlaDueAt(dispute.getStaffSlaDueAt())
                .staffReviewStartedAt(dispute.getStaffReviewStartedAt())
                .staffDecidedAt(dispute.getStaffDecidedAt())
                .staffDecisionMade(dispute.getStaffDecisionPercentage() != null)
                .build();
    }
}
