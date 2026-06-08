/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/ProfileService.java
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

import java.util.List;

// Note: Annotation này cho Spring quản lý class như một service chứa nghiệp vụ.
@Service
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class ProfileService {
    private final AccessService accessService;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final AccountRepository accountRepository;
    private final PortfolioRepository portfolioRepository;
    private final StaffRepository staffRepository;
    private final AuditLogRepository auditLogRepository;

    // TAO HOAC CAP NHAT HO SO DOANH NGHIEP DE PHUC VU LUONG KYB.
    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `upsertBusiness` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public BusinessProfileEntity upsertBusiness(BusinessProfileEntity input) {
        accessService.requireRole("BUSINESS");
        if (input == null) throw new AppException("BODY REQUEST KHONG HOP LE");
        // KIEM TRA CAC TRUONG BAT BUOC DE TRANH LUU HO SO THIEU DU LIEU KYB.
        if (input.getTaxCode() == null || input.getTaxCode().isBlank()) throw new AppException("TAX CODE KHONG DUOC DE TRONG");
        if (input.getCompanyName() == null || input.getCompanyName().isBlank()) throw new AppException("COMPANY NAME KHONG DUOC DE TRONG");
        AccountEntity account = accessService.currentAccount();
        BusinessProfileEntity entity = businessProfileRepository.findByAccountId(account.getAccountId()).orElseGet(BusinessProfileEntity::new);
        entity.setAccountId(account.getAccountId());
        entity.setTaxCode(input.getTaxCode());
        entity.setCompanyName(input.getCompanyName());
        entity.setAddress(input.getAddress());
        entity.setBusinessLicenseUrl(input.getBusinessLicenseUrl());
        // Moi lan nop/cap nhat KYB deu dua account ve Pending de staff duyet lai.
        entity.setKybStatus("Pending");
        entity.setApprovedBy(null);
        account.setStatus("Pending");
        accountRepository.save(account);
        return businessProfileRepository.save(entity);
    }

    // TAO HOAC CAP NHAT HO SO CHUYEN GIA DE PHUC VU LUONG KYC.
    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `upsertExpert` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public ExpertProfileEntity upsertExpert(ExpertProfileEntity input) {
        accessService.requireRole("EXPERT");
        if (input == null) throw new AppException("BODY REQUEST KHONG HOP LE");
        // KIEM TRA CAC TRUONG BAT BUOC CHO LUONG KYC.
        if (input.getNationalId() == null || input.getNationalId().isBlank()) throw new AppException("NATIONAL ID KHONG DUOC DE TRONG");
        if (input.getPortfolioUrl() == null || input.getPortfolioUrl().isBlank()) throw new AppException("PORTFOLIO URL KHONG DUOC DE TRONG");
        if (input.getYearsOfExperience() == null || input.getYearsOfExperience() < 0) throw new AppException("YEARS OF EXPERIENCE KHONG HOP LE");
        AccountEntity account = accessService.currentAccount();
        expertProfileRepository.findByNationalId(input.getNationalId().trim())
                .filter(existing -> !existing.getAccountId().equals(account.getAccountId()))
                .ifPresent(existing -> { throw new AppException("NATIONAL ID DA DUOC SU DUNG"); });
        ExpertProfileEntity entity = expertProfileRepository.findByAccountId(account.getAccountId()).orElseGet(ExpertProfileEntity::new);
        entity.setAccountId(account.getAccountId());
        entity.setNationalId(input.getNationalId().trim());
        entity.setPortfolioUrl(input.getPortfolioUrl().trim());
        entity.setYearsOfExperience(input.getYearsOfExperience());
        // Moi lan nop/cap nhat KYC deu dua account ve Pending de staff duyet lai.
        entity.setKycStatus("Pending");
        entity.setApprovedBy(null);
        account.setStatus("Pending");
        accountRepository.save(account);
        return expertProfileRepository.save(entity);
    }

    // STAFF duyet ho so business/expert va ghi log audit.
    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `approveProfile` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public Object approveProfile(String type, Integer id, String status) {
        accessService.requireRole("STAFF");
        // CHI CHO PHEP 2 GIA TRI PHE DUYET DUNG THEO BUSINESS RULE.
        if (!"Approved".equalsIgnoreCase(status) && !"Rejected".equalsIgnoreCase(status)) {
            throw new AppException("STATUS PHE DUYET KHONG HOP LE");
        }
        AccountEntity actor = accessService.currentAccount();
        Integer staffId = resolveStaffId(actor.getAccountId());
        if ("BUSINESS".equalsIgnoreCase(type)) {
            BusinessProfileEntity b = businessProfileRepository.findById(id).orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
            b.setKybStatus(status);
            b.setApprovedBy(staffId);
            updateAccountStatus(b.getAccountId(), status);
            audit("APPROVE_BUSINESS_PROFILE", "business_profiles", String.valueOf(id), actor.getAccountId());
            return businessProfileRepository.save(b);
        }
        if (!"EXPERT".equalsIgnoreCase(type)) {
            throw new AppException("TYPE PROFILE KHONG HOP LE");
        }
        ExpertProfileEntity e = expertProfileRepository.findById(id).orElseThrow(() -> new NotFoundException("KHONG TIM THAY EXPERT PROFILE"));
        e.setKycStatus(status);
        e.setApprovedBy(staffId);
        updateAccountStatus(e.getAccountId(), status);
        audit("APPROVE_EXPERT_PROFILE", "expert_profiles", String.valueOf(id), actor.getAccountId());
        return expertProfileRepository.save(e);
    }

    public List<BusinessProfileEntity> allBusinessProfiles() { accessService.requireRole("STAFF"); return businessProfileRepository.findAll(); }
    public List<ExpertProfileEntity> allExpertProfiles() { accessService.requireRole("STAFF", "BUSINESS"); return expertProfileRepository.findAll(); }
    public List<PortfolioEntity> allPortfolios() { accessService.requireRole("STAFF", "BUSINESS"); return portfolioRepository.findAll(); }

    // TAO HOAC CAP NHAT PORTFOLIO 4 THANH PHAN BAT BUOC CUA CHUYEN GIA.
    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `upsertPortfolio` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public PortfolioEntity upsertPortfolio(PortfolioEntity input) {
        accessService.requireRole("EXPERT");
        accessService.requireApprovedAccount();
        if (input == null) throw new AppException("BODY REQUEST KHONG HOP LE");
        // KIEM TRA CAC TRUONG PORTFOLIO MOI DE PHUC VU BUSINESS XEM CHI TIET CHUYEN GIA.
        if (input.getDomainIds() == null || input.getDomainIds().isBlank()) throw new AppException("DOMAIN IDS KHONG DUOC DE TRONG");
        if (input.getSkillIds() == null || input.getSkillIds().isBlank()) throw new AppException("SKILL IDS KHONG DUOC DE TRONG");
        if (input.getYearsExperience() == null || input.getYearsExperience() < 0) throw new AppException("YEARS EXPERIENCE KHONG HOP LE");
        if (input.getSelfDescription() == null || input.getSelfDescription().isBlank()) throw new AppException("SELF DESCRIPTION KHONG DUOC DE TRONG");
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer expertId = expertProfileRepository.findByAccountId(accountId)
                .map(ExpertProfileEntity::getExpertId)
                .orElseThrow(() -> new NotFoundException("CHUA CO EXPERT PROFILE"));
        PortfolioEntity entity = portfolioRepository.findByExpertId(expertId).orElseGet(PortfolioEntity::new);
        entity.setExpertId(expertId);
        entity.setDomainIds(input.getDomainIds());
        entity.setSkillIds(input.getSkillIds());
        entity.setYearsExperience(input.getYearsExperience());
        entity.setCertificates(input.getCertificates());
        entity.setSelfDescription(input.getSelfDescription());
        return portfolioRepository.save(entity);
    }

    // Note: Hàm `audit` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private void audit(String action, String entityName, String entityId, Integer actorId) {
        auditLogRepository.save(AuditLogEntity.builder().action(action).entityName(entityName).entityId(entityId).actorAccountId(actorId).build());
    }

    // Note: Hàm `updateAccountStatus` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private void updateAccountStatus(Integer accountId, String approvalStatus) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY ACCOUNT CUA PROFILE"));
        account.setStatus("Approved".equalsIgnoreCase(approvalStatus) ? "Approved" : "Rejected");
        accountRepository.save(account);
    }

    // BAT BUOC TAI KHOAN STAFF PHAI CO BAN GHI TRONG BANG staffs DE LUU approvedBy.
    // Note: Hàm `resolveStaffId` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private Integer resolveStaffId(Integer accountId) {
        return staffRepository.findByAccountId(accountId)
                .map(StaffEntity::getStaffId)
                .orElseThrow(() -> new AppException("TAI KHOAN STAFF CHUA DUOC KHOI TAO HO SO NHAN SU (staffs)"));
    }
}
