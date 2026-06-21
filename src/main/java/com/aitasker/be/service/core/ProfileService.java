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
import org.springframework.web.multipart.MultipartFile;

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
    private final AuditLogService auditLogService;
    private final FirebaseStorageService firebaseStorageService;
    private final JobRepository jobRepository;
    private final NotificationService notificationService;

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
        BusinessProfileEntity saved = businessProfileRepository.save(entity);
        auditLogService.record(AuditLogService.ACTION_UPSERT_BUSINESS_PROFILE, "business_profiles", String.valueOf(saved.getBusinessId()), account.getAccountId());
        return saved;
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
        ExpertProfileEntity saved = expertProfileRepository.save(entity);
        auditLogService.record(AuditLogService.ACTION_UPSERT_EXPERT_PROFILE, "expert_profiles", String.valueOf(saved.getExpertId()), account.getAccountId());
        return saved;
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
            audit(approvalAction("BUSINESS", status), "business_profiles", String.valueOf(id), actor.getAccountId());
            BusinessProfileEntity saved = businessProfileRepository.save(b);
            notificationService.notifyProfileReviewed(saved.getAccountId(), actor.getAccountId(), "BUSINESS", status);
            return saved;
        }
        if (!"EXPERT".equalsIgnoreCase(type)) {
            throw new AppException("TYPE PROFILE KHONG HOP LE");
        }
        ExpertProfileEntity e = expertProfileRepository.findById(id).orElseThrow(() -> new NotFoundException("KHONG TIM THAY EXPERT PROFILE"));
        e.setKycStatus(status);
        e.setApprovedBy(staffId);
        updateAccountStatus(e.getAccountId(), status);
        audit(approvalAction("EXPERT", status), "expert_profiles", String.valueOf(id), actor.getAccountId());
        ExpertProfileEntity saved = expertProfileRepository.save(e);
        notificationService.notifyProfileReviewed(saved.getAccountId(), actor.getAccountId(), "EXPERT", status);
        return saved;
    }

    // Note: Hàm `currentBusinessProfile` lấy hồ sơ KYB của chính doanh nghiệp đang đăng nhập để reload trang vẫn thấy status/file mới nhất.
    public BusinessProfileEntity currentBusinessProfile() {
        accessService.requireRole("BUSINESS");
        Integer accountId = accessService.currentAccount().getAccountId();
        return businessProfileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
    }

    // Note: Hàm `businessProfileById` lấy hồ sơ doanh nghiệp theo businessId để hiển thị trang cá nhân public cho expert xem.
    public BusinessProfileEntity businessProfileById(Integer businessId) {
        accessService.requireRole("EXPERT", "BUSINESS", "STAFF", "ADMIN");
        return businessProfileRepository.findById(businessId)
                .map(this::attachBusinessAccountInfo)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
    }

    // Note: Hàm `businessProfileByJob` lấy hồ sơ doanh nghiệp đăng một job để chuyên gia xem chi tiết khi job đã public.
    public BusinessProfileEntity businessProfileByJob(Integer jobId) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        // Job OPEN là dữ liệu public nên chuyên gia được xem doanh nghiệp đăng job; job chưa public vẫn giới hạn cho nội bộ có quyền.
        if (!"OPEN".equalsIgnoreCase(job.getStatus())) {
            accessService.requireRole("STAFF", "ADMIN", "BUSINESS");
        }
        return businessProfileRepository.findById(job.getBusinessId())
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
    }

    // Note: Hàm `currentExpertProfile` lấy hồ sơ KYC của chính chuyên gia đang đăng nhập để reload trang vẫn thấy status mới nhất.
    public ExpertProfileEntity currentExpertProfile() {
        accessService.requireRole("EXPERT");
        Integer accountId = accessService.currentAccount().getAccountId();
        return expertProfileRepository.findByAccountId(accountId)
                .map(this::attachExpertAccountInfo)
                .orElseThrow(() -> new NotFoundException("CHUA CO EXPERT PROFILE"));
    }

    // Note: Hàm `currentPortfolio` lấy portfolio của chính chuyên gia đang đăng nhập để form không mất dữ liệu sau khi reload.
    public PortfolioEntity currentPortfolio() {
        accessService.requireRole("EXPERT");
        Integer accountId = accessService.currentAccount().getAccountId();
        Integer expertId = expertProfileRepository.findByAccountId(accountId)
                .map(ExpertProfileEntity::getExpertId)
                .orElseThrow(() -> new NotFoundException("CHUA CO EXPERT PROFILE"));
        return portfolioRepository.findByExpertId(expertId)
                .orElseThrow(() -> new NotFoundException("CHUA CO PORTFOLIO"));
    }

    // Note: Hàm `createFileViewUrl` tạo link xem file Firebase cho người dùng có quyền trong hệ thống thay vì trả raw storage path lên UI.
    public String createFileViewUrl(String path) {
        accessService.requireRole("STAFF", "ADMIN", "BUSINESS", "EXPERT");
        if (path != null && (path.startsWith("http://") || path.startsWith("https://"))) {
            return path;
        }
        return firebaseStorageService.createReadUrl(path);
    }

    public List<BusinessProfileEntity> allBusinessProfiles() { accessService.requireRole("STAFF"); return businessProfileRepository.findAll(); }
    // Note: Hàm `allExpertProfiles` cho STAFF quản trị hồ sơ và BUSINESS đọc thông tin expert khi xem proposal.
    public List<ExpertProfileEntity> allExpertProfiles() {
        accessService.requireRole("STAFF", "BUSINESS");
        return expertProfileRepository.findAll().stream()
                .map(this::attachExpertAccountInfo)
                .toList();
    }
    // Note: Hàm `allPortfolios` cho STAFF quản trị portfolio và BUSINESS xem năng lực expert trong màn proposal.
    public List<PortfolioEntity> allPortfolios() { accessService.requireRole("STAFF", "BUSINESS"); return portfolioRepository.findAll(); }

    // Note: Hàm `uploadBusinessLicense` upload file giấy phép kinh doanh lên Firebase Storage và trả về storage path để lưu vào hồ sơ KYB.
    public String uploadBusinessLicense(MultipartFile file) {
        accessService.requireRole("BUSINESS");
        Integer accountId = accessService.currentAccount().getAccountId();
        String path = firebaseStorageService.upload(file, "business-licenses/accounts/" + accountId);
        businessProfileRepository.findByAccountId(accountId)
                .ifPresent(profile -> auditLogService.record(AuditLogService.ACTION_UPLOAD_BUSINESS_LICENSE, "business_profiles", String.valueOf(profile.getBusinessId()), accountId));
        return path;
    }

    // Note: Hàm `uploadExpertCertificate` upload file chứng chỉ chuyên gia lên Firebase Storage và trả về storage path để lưu vào portfolio.
    public String uploadExpertCertificate(MultipartFile file) {
        accessService.requireRole("EXPERT");
        Integer accountId = accessService.currentAccount().getAccountId();
        String path = firebaseStorageService.upload(file, "expert-certificates/accounts/" + accountId);
        expertProfileRepository.findByAccountId(accountId)
                .ifPresent(profile -> auditLogService.record(AuditLogService.ACTION_UPLOAD_EXPERT_CERTIFICATE, "expert_profiles", String.valueOf(profile.getExpertId()), accountId));
        return path;
    }

    // TAO HOAC CAP NHAT PORTFOLIO MOI CUA CHUYEN GIA DE BUSINESS DOC KHI REVIEW PROPOSAL.
    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `upsertPortfolio` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public PortfolioEntity upsertPortfolio(PortfolioEntity input) {
        accessService.requireRole("EXPERT");
        if (input == null) throw new AppException("BODY REQUEST KHONG HOP LE");
        // KIEM TRA CAC TRUONG PORTFOLIO MOI DE PHUC VU BUSINESS XEM CHI TIET CHUYEN GIA.
        if (input.getDomainIds() == null || input.getDomainIds().isBlank()) throw new AppException("DOMAIN IDS KHONG DUOC DE TRONG");
        if (input.getSkillIds() == null || input.getSkillIds().isBlank()) throw new AppException("SKILL IDS KHONG DUOC DE TRONG");
        if (input.getTechnologyIds() == null || input.getTechnologyIds().isBlank()) throw new AppException("TECHNOLOGY IDS KHONG DUOC DE TRONG");
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
        entity.setTechnologyIds(input.getTechnologyIds());
        entity.setYearsExperience(input.getYearsExperience());
        entity.setCertificates(input.getCertificates());
        entity.setSelfDescription(input.getSelfDescription());
        PortfolioEntity saved = portfolioRepository.save(entity);
        auditLogService.record(AuditLogService.ACTION_UPSERT_PORTFOLIO, "portfolios", String.valueOf(saved.getPortfolioId()), accountId);
        return saved;
    }

    // Note: Hàm `attachBusinessAccountInfo` gắn thông tin tài khoản đọc được vào response business để trang cá nhân hiển thị đầy đủ.
    private BusinessProfileEntity attachBusinessAccountInfo(BusinessProfileEntity business) {
        accountRepository.findById(business.getAccountId()).ifPresent(account ->
                business.setFullName(account.getFullName()));
        return business;
    }

    // Note: Hàm `attachExpertAccountInfo` gắn thông tin tài khoản đọc được vào response expert để BUSINESS xem chi tiết proposal.
    private ExpertProfileEntity attachExpertAccountInfo(ExpertProfileEntity expert) {
        accountRepository.findById(expert.getAccountId()).ifPresent(account -> {
            expert.setFullName(account.getFullName());
            expert.setPhone(account.getPhone());
            expert.setTitle("Chuyên gia AI");
        });
        return expert;
    }

    // Note: Hàm `audit` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private void audit(String action, String entityName, String entityId, Integer actorId) {
        auditLogService.record(action, entityName, entityId, actorId);
    }

    // Note: Hàm `approvalAction` chọn action audit tiếng Việt đúng với loại hồ sơ và kết quả duyệt.
    private String approvalAction(String type, String status) {
        boolean approved = "Approved".equalsIgnoreCase(status);
        if ("BUSINESS".equalsIgnoreCase(type)) {
            return approved ? AuditLogService.ACTION_APPROVE_BUSINESS_PROFILE : AuditLogService.ACTION_REJECT_BUSINESS_PROFILE;
        }
        return approved ? AuditLogService.ACTION_APPROVE_EXPERT_PROFILE : AuditLogService.ACTION_REJECT_EXPERT_PROFILE;
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
