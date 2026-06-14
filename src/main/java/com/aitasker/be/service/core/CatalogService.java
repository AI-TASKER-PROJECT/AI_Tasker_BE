/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/CatalogService.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.common.exception.ResourceConflictException;
import com.aitasker.be.dto.catalog.DomainRequest;
import com.aitasker.be.dto.catalog.JobSkillAssignmentRequest;
import com.aitasker.be.dto.catalog.SkillRequest;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Note: Annotation này cho Spring quản lý class như một service chứa nghiệp vụ.
@Service
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class CatalogService {
    private final AccessService accessService;
    private final DomainRepository domainRepository;
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final JobDomainRepository jobDomainRepository;
    private final JobSkillRepository jobSkillRepository;
    private final AcceptanceCriteriaRepository acceptanceCriteriaRepository;

    // Note: Hàm `listDomains` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<DomainEntity> listDomains(Boolean activeOnly) {
        if (Boolean.TRUE.equals(activeOnly))
            return domainRepository.findByIsActiveTrueOrderBySortOrderAscDomainNameAsc();
        return domainRepository.findAll();
    }

    // Note: Hàm `listSkills` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<SkillEntity> listSkills(Boolean activeOnly) {
        if (Boolean.TRUE.equals(activeOnly)) return skillRepository.findByIsActiveTrueOrderBySkillNameAsc();
        return skillRepository.findAll();
    }

    // Note: HÃ m `listAcceptanceCriteria` tráº£ danh má»¥c tiÃªu chÃ­ nghiá»‡m thu do ná»n táº£ng cung cáº¥p Ä‘á»ƒ business chá»n cho milestone.
    public List<AcceptanceCriteriaEntity> listAcceptanceCriteria(Boolean activeOnly) {
        if (Boolean.TRUE.equals(activeOnly)) return acceptanceCriteriaRepository.findByIsActiveTrueOrderBySortOrderAscCriteriaIdAsc();
        return acceptanceCriteriaRepository.findAllByOrderBySortOrderAscCriteriaIdAsc();
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `createDomain` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public DomainEntity createDomain(DomainRequest request) {
        accessService.requireRole("ADMIN");
        validateDomainRequest(request, true);
        String code = normalizeCode(request.getDomainCode());
        if (domainRepository.existsByDomainCode(code)) throw new ResourceConflictException("DOMAIN CODE DA TON TAI");
        DomainEntity entity = DomainEntity.builder()
                .domainCode(code)
                .domainName(request.getDomainName().trim())
                .description(request.getDescription())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .build();
        return domainRepository.save(entity);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `updateDomain` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public DomainEntity updateDomain(Integer domainId, DomainRequest request) {
        accessService.requireRole("ADMIN");
        DomainEntity entity = domainRepository.findById(domainId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DOMAIN"));
        if (request.getDomainCode() != null && !request.getDomainCode().isBlank()) {
            String code = normalizeCode(request.getDomainCode());
            domainRepository.findByDomainCode(code)
                    .filter(existing -> !existing.getDomainId().equals(domainId))
                    .ifPresent(existing -> {
                        throw new ResourceConflictException("DOMAIN CODE DA TON TAI");
                    });
            entity.setDomainCode(code);
        }
        if (request.getDomainName() != null && !request.getDomainName().isBlank())
            entity.setDomainName(request.getDomainName().trim());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getIsActive() != null) entity.setIsActive(request.getIsActive());
        if (request.getSortOrder() != null) entity.setSortOrder(request.getSortOrder());
        return domainRepository.save(entity);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `createSkill` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public SkillEntity createSkill(SkillRequest request) {
        accessService.requireRole("ADMIN");
        validateSkillRequest(request, true);
        String code = normalizeCode(request.getSkillCode());
        if (skillRepository.existsBySkillCode(code)) throw new ResourceConflictException("SKILL CODE DA TON TAI");
        SkillEntity entity = SkillEntity.builder()
                .skillCode(code)
                .skillName(request.getSkillName().trim())
                .description(request.getDescription())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();
        return skillRepository.save(entity);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `updateSkill` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public SkillEntity updateSkill(Integer skillId, SkillRequest request) {
        accessService.requireRole("ADMIN");
        SkillEntity entity = skillRepository.findById(skillId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY SKILL"));
        if (request.getSkillCode() != null && !request.getSkillCode().isBlank()) {
            String code = normalizeCode(request.getSkillCode());
            skillRepository.findBySkillCode(code)
                    .filter(existing -> !existing.getSkillId().equals(skillId))
                    .ifPresent(existing -> {
                        throw new ResourceConflictException("SKILL CODE DA TON TAI");
                    });
            entity.setSkillCode(code);
        }
        if (request.getSkillName() != null && !request.getSkillName().isBlank())
            entity.setSkillName(request.getSkillName().trim());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getIsActive() != null) entity.setIsActive(request.getIsActive());
        return skillRepository.save(entity);
    }

    // Note: Hàm `listJobDomains` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<JobDomainEntity> listJobDomains(Integer jobId) {
        jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        return jobDomainRepository.findByIdJobId(jobId);
    }

    // Note: Hàm `listJobSkills` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<JobSkillEntity> listJobSkills(Integer jobId) {
        jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        return jobSkillRepository.findByIdJobId(jobId);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `replaceJobDomains` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<JobDomainEntity> replaceJobDomains(Integer jobId, List<Integer> domainIds) {
        requireJobOwnerOrAdmin(jobId);
        Set<Integer> ids = new LinkedHashSet<>(domainIds == null ? List.of() : domainIds);
        for (Integer domainId : ids) {
            domainRepository.findById(domainId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY DOMAIN " + domainId));
        }
        jobDomainRepository.deleteByIdJobId(jobId);
        ids.forEach(domainId -> jobDomainRepository.save(JobDomainEntity.builder()
                .id(new JobDomainId(jobId, domainId))
                .build()));
        return jobDomainRepository.findByIdJobId(jobId);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `replaceJobSkills` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<JobSkillEntity> replaceJobSkills(Integer jobId, List<JobSkillAssignmentRequest> assignments) {
        requireJobOwnerOrAdmin(jobId);
        jobSkillRepository.deleteByIdJobId(jobId);
        if (assignments != null) {
            Set<Integer> seen = new LinkedHashSet<>();
            for (JobSkillAssignmentRequest assignment : assignments) {
                if (assignment.getSkillId() == null || !seen.add(assignment.getSkillId())) continue;
                skillRepository.findById(assignment.getSkillId())
                        .orElseThrow(() -> new NotFoundException("KHONG TIM THAY SKILL " + assignment.getSkillId()));
                jobSkillRepository.save(JobSkillEntity.builder()
                        .id(new JobSkillId(jobId, assignment.getSkillId()))
                        .isMandatory(assignment.getIsMandatory() == null || assignment.getIsMandatory())
                        .build());
            }
        }
        return jobSkillRepository.findByIdJobId(jobId);
    }

    // Note: Hàm `requireJobOwnerOrAdmin` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private void requireJobOwnerOrAdmin(Integer jobId) {
        JobEntity job = jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        AccountEntity actor = accessService.currentAccount();
        String role = actor.getRole().getRoleName();
        if ("ADMIN".equals(role)) return;
        if (!"BUSINESS".equals(role)) throw new AppException("CHI BUSINESS HOAC ADMIN DUOC GAN DOMAIN/SKILL CHO JOB");
        accessService.requireApprovedAccount();
        Integer businessId = businessProfileRepository.findByAccountId(actor.getAccountId())
                .map(BusinessProfileEntity::getBusinessId)
                .orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
        if (!businessId.equals(job.getBusinessId())) throw new AppException("BAN KHONG CO QUYEN CAP NHAT JOB NAY");
    }

    // Note: Hàm `validateDomainRequest` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private void validateDomainRequest(DomainRequest request, boolean requireAll) {
        if (request == null) throw new AppException("BODY REQUEST KHONG HOP LE");
        if (requireAll && (request.getDomainCode() == null || request.getDomainCode().isBlank()))
            throw new AppException("DOMAIN CODE KHONG DUOC DE TRONG");
        if (requireAll && (request.getDomainName() == null || request.getDomainName().isBlank()))
            throw new AppException("DOMAIN NAME KHONG DUOC DE TRONG");
    }

    // Note: Hàm `validateSkillRequest` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private void validateSkillRequest(SkillRequest request, boolean requireAll) {
        if (request == null) throw new AppException("BODY REQUEST KHONG HOP LE");
        if (requireAll && (request.getSkillCode() == null || request.getSkillCode().isBlank()))
            throw new AppException("SKILL CODE KHONG DUOC DE TRONG");
        if (requireAll && (request.getSkillName() == null || request.getSkillName().isBlank()))
            throw new AppException("SKILL NAME KHONG DUOC DE TRONG");
    }

    // Note: Hàm `normalizeCode` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private String normalizeCode(String value) {
        return value.trim().replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "").toUpperCase();
    }

}
