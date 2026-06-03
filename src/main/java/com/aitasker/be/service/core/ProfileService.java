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
public class ProfileService {
    private final AccessService accessService;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final PortfolioRepository portfolioRepository;
    private final StaffRepository staffRepository;
    private final AuditLogRepository auditLogRepository;

    // TAO HOAC CAP NHAT HO SO DOANH NGHIEP DE PHUC VU LUONG KYB.
    @Transactional
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
        if (entity.getKybStatus() == null) entity.setKybStatus("Pending");
        return businessProfileRepository.save(entity);
    }

    // TAO HOAC CAP NHAT HO SO CHUYEN GIA DE PHUC VU LUONG KYC.
    @Transactional
    public ExpertProfileEntity upsertExpert(ExpertProfileEntity input) {
        accessService.requireRole("EXPERT");
        if (input == null) throw new AppException("BODY REQUEST KHONG HOP LE");
        // KIEM TRA TRUONG DINH DANH BAT BUOC CHO LUONG KYC.
        if (input.getNationalId() == null || input.getNationalId().isBlank()) throw new AppException("NATIONAL ID KHONG DUOC DE TRONG");
        AccountEntity account = accessService.currentAccount();
        ExpertProfileEntity entity = expertProfileRepository.findByAccountId(account.getAccountId()).orElseGet(ExpertProfileEntity::new);
        entity.setAccountId(account.getAccountId());
        entity.setNationalId(input.getNationalId());
        entity.setIdCardFrontUrl(input.getIdCardFrontUrl());
        entity.setIdCardBackUrl(input.getIdCardBackUrl());
        if (entity.getKycStatus() == null) entity.setKycStatus("Pending");
        return expertProfileRepository.save(entity);
    }

    // STAFF/ADMIN DUYET HO SO VA GHI LOG AUDIT.
    @Transactional
    public Object approveProfile(String type, Integer id, String status) {
        accessService.requireRole("ADMIN", "STAFF");
        // CHI CHO PHEP 2 GIA TRI PHE DUYET DUNG THEO BUSINESS RULE.
        if (!"Approved".equalsIgnoreCase(status) && !"Rejected".equalsIgnoreCase(status)) {
            throw new AppException("STATUS PHE DUYET KHONG HOP LE");
        }
        AccountEntity actor = accessService.currentAccount();
        if ("BUSINESS".equalsIgnoreCase(type)) {
            BusinessProfileEntity b = businessProfileRepository.findById(id).orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
            b.setKybStatus(status);
            if (actor.getRole().getRoleName().equals("STAFF")) {
                b.setApprovedBy(resolveStaffId(actor.getAccountId()));
            }
            audit("APPROVE_BUSINESS_PROFILE", "business_profiles", String.valueOf(id), actor.getAccountId());
            return businessProfileRepository.save(b);
        }
        if (!"EXPERT".equalsIgnoreCase(type)) {
            throw new AppException("TYPE PROFILE KHONG HOP LE");
        }
        ExpertProfileEntity e = expertProfileRepository.findById(id).orElseThrow(() -> new NotFoundException("KHONG TIM THAY EXPERT PROFILE"));
        e.setKycStatus(status);
        if (actor.getRole().getRoleName().equals("STAFF")) {
            e.setApprovedBy(resolveStaffId(actor.getAccountId()));
        }
        audit("APPROVE_EXPERT_PROFILE", "expert_profiles", String.valueOf(id), actor.getAccountId());
        return expertProfileRepository.save(e);
    }

    public List<BusinessProfileEntity> allBusinessProfiles() { accessService.requireRole("ADMIN", "STAFF"); return businessProfileRepository.findAll(); }
    public List<ExpertProfileEntity> allExpertProfiles() { accessService.requireRole("ADMIN", "STAFF"); return expertProfileRepository.findAll(); }
    public List<PortfolioEntity> allPortfolios() { accessService.requireRole("ADMIN", "STAFF"); return portfolioRepository.findAll(); }

    // TAO HOAC CAP NHAT PORTFOLIO 4 THANH PHAN BAT BUOC CUA CHUYEN GIA.
    @Transactional
    public PortfolioEntity upsertPortfolio(PortfolioEntity input) {
        accessService.requireRole("EXPERT");
        if (input == null) throw new AppException("BODY REQUEST KHONG HOP LE");
        // KIEM TRA DAY DU CAC TRUONG CHUYEN MON THEO RULE PRF-01.
        if (input.getContext() == null || input.getContext().isBlank()) throw new AppException("CONTEXT KHONG DUOC DE TRONG");
        if (input.getDataProcessing() == null || input.getDataProcessing().isBlank()) throw new AppException("DATA PROCESSING KHONG DUOC DE TRONG");
        if (input.getModelArchitecture() == null || input.getModelArchitecture().isBlank()) throw new AppException("MODEL ARCHITECTURE KHONG DUOC DE TRONG");
        if (input.getPerformanceMetrics() == null || input.getPerformanceMetrics().isBlank()) throw new AppException("PERFORMANCE METRICS KHONG DUOC DE TRONG");
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer expertId = expertProfileRepository.findByAccountId(accountId)
                .map(ExpertProfileEntity::getExpertId)
                .orElseThrow(() -> new NotFoundException("CHUA CO EXPERT PROFILE"));
        PortfolioEntity entity = portfolioRepository.findByExpertId(expertId).orElseGet(PortfolioEntity::new);
        entity.setExpertId(expertId);
        entity.setContext(input.getContext());
        entity.setDataProcessing(input.getDataProcessing());
        entity.setModelArchitecture(input.getModelArchitecture());
        entity.setPerformanceMetrics(input.getPerformanceMetrics());
        entity.setPocUrl(input.getPocUrl());
        return portfolioRepository.save(entity);
    }

    private void audit(String action, String entityName, String entityId, Integer actorId) {
        auditLogRepository.save(AuditLogEntity.builder().action(action).entityName(entityName).entityId(entityId).actorAccountId(actorId).build());
    }

    // BAT BUOC TAI KHOAN STAFF PHAI CO BAN GHI TRONG BANG staffs DE LUU approvedBy.
    private Integer resolveStaffId(Integer accountId) {
        return staffRepository.findByAccountId(accountId)
                .map(StaffEntity::getStaffId)
                .orElseThrow(() -> new AppException("TAI KHOAN STAFF CHUA DUOC KHOI TAO HO SO NHAN SU (staffs)"));
    }
}
