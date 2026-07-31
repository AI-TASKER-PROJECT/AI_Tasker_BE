/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/ProfileService.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.ForbiddenException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.dto.auth.TaxCheckResponse;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import com.aitasker.be.service.auth.TaxCheckService;
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
    private final ReviewRepository reviewRepository;
    private final TaxCheckService taxCheckService;
    private final DomainRepository domainRepository;
    private final StaffDomainRepository staffDomainRepository;

    // TAO HOAC CAP NHAT HO SO DOANH NGHIEP DE PHUC VU LUONG KYB.
    // Note: Annotation nay dam bao cac thao tac database trong ham chay cung mot transaction.
    @Transactional
    // Note: Ham `upsertBusiness` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    // Chức năng 1: Tạo hoặc cập nhật hồ sơ định danh doanh nghiệp của tài khoản hiện tại.
    public BusinessProfileEntity upsertBusiness(BusinessProfileEntity input) {
        accessService.requireRole("BUSINESS");
        if (input == null) throw new AppException("BODY REQUEST KHONG HOP LE");
        // KIEM TRA CAC TRUONG BAT BUOC DE TRANH LUU HO SO THIEU DU LIEU KYB.
        if (input.getTaxCode() == null || input.getTaxCode().isBlank()) throw new AppException("TAX CODE KHONG DUOC DE TRONG");
        // KIEM TRA DINH DANG MA SO THUE (10 HOAC 13 CHU SO).
        if (!input.getTaxCode().matches("\\d{10}|\\d{13}")) throw new AppException("MA SO THUE KHONG HOP LE");

        AccountEntity account = accessService.currentAccount();

        // KIEM TRA TRUNG MST — MOT MST CHI THUOC VE MOT ACCOUNT.
        BusinessProfileEntity entity = businessProfileRepository.findByAccountId(account.getAccountId()).orElseGet(BusinessProfileEntity::new);
        ProfileSubmissionMode submissionMode = resolveBusinessSubmissionMode(entity);

        if (businessProfileRepository.existsByTaxCodeExcludingAccount(input.getTaxCode(), account.getAccountId())) {
            throw new AppException("MA SO THUE DA DUOC SU DUNG BOI TAI KHOAN KHAC");
        }

        // XAC THUC MST VOI VIETQR — HE THONG TU DONG DIEN THONG TIN DOANH NGHIEP TU NGUON CHINH THUC.
        TaxCheckResponse vietQrData = taxCheckService.checkTaxCode(input.getTaxCode());

        entity.setAccountId(account.getAccountId());
        entity.setTaxCode(input.getTaxCode());
        entity.setCompanyName(vietQrData.getCompanyName());
        entity.setAddress(resolveBusinessAddress(input, vietQrData, submissionMode));
        entity.setVerifiedRepresentative(vietQrData.getRepresentative());
        entity.setBusinessLicenseUrl(input.getBusinessLicenseUrl());
        // Moi lan nop/cap nhat KYB deu dua account ve Pending de staff duyet lai.
        if (submissionMode.reopensReview()) {
            entity.setKybStatus("Pending");
            entity.setApprovedBy(null);
            entity.setRejectionReason(null);
            account.setStatus("Pending");
            accountRepository.save(account);
        }
        BusinessProfileEntity saved = businessProfileRepository.save(entity);
        auditLogService.record(AuditLogService.ACTION_UPSERT_BUSINESS_PROFILE, "business_profiles", String.valueOf(saved.getBusinessId()), account.getAccountId());
        if (submissionMode.reopensReview()) {
            notifyStaffProfileSubmitted("BUSINESS", saved.getBusinessId(), account.getAccountId(), saved.getCompanyName());
        }
        return saved;
    }

    // TAO HOAC CAP NHAT HO SO CHUYEN GIA DE PHUC VU LUONG KYC.
    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `upsertExpert` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    // Chức năng 2: Tạo hoặc cập nhật hồ sơ định danh chuyên gia của tài khoản hiện tại.
    public ExpertProfileEntity upsertExpert(ExpertProfileEntity input) {
        accessService.requireRole("EXPERT");
        if (input == null) throw new AppException("BODY REQUEST KHONG HOP LE");
        // KIEM TRA CAC TRUONG BAT BUOC CHO LUONG KYC.
        if (input.getNationalId() == null || input.getNationalId().isBlank()) throw new AppException("NATIONAL ID KHONG DUOC DE TRONG");
        if (input.getPortfolioUrl() == null || input.getPortfolioUrl().isBlank()) throw new AppException("PORTFOLIO URL KHONG DUOC DE TRONG");
        if (input.getYearsOfExperience() == null || input.getYearsOfExperience() < 0) throw new AppException("YEARS OF EXPERIENCE KHONG HOP LE");
        AccountEntity account = accessService.currentAccount();
        ExpertProfileEntity entity = expertProfileRepository.findByAccountId(account.getAccountId()).orElseGet(ExpertProfileEntity::new);
        ProfileSubmissionMode submissionMode = resolveExpertSubmissionMode(entity);
        expertProfileRepository.findByNationalId(input.getNationalId().trim())
                .filter(existing -> !existing.getAccountId().equals(account.getAccountId()))
                .ifPresent(existing -> { throw new AppException("NATIONAL ID DA DUOC SU DUNG"); });
        entity.setAccountId(account.getAccountId());
        entity.setNationalId(input.getNationalId().trim());
        entity.setPortfolioUrl(input.getPortfolioUrl().trim());
        entity.setYearsOfExperience(input.getYearsOfExperience());
        // Moi lan nop/cap nhat KYC deu dua account ve Pending de staff duyet lai.
        if (submissionMode.reopensReview()) {
            entity.setKycStatus("Pending");
            entity.setApprovedBy(null);
            entity.setRejectionReason(null);
            account.setStatus("Pending");
            accountRepository.save(account);
        }
        ExpertProfileEntity saved = expertProfileRepository.save(entity);
        syncPortfolioYearsExperience(saved);
        auditLogService.record(AuditLogService.ACTION_UPSERT_EXPERT_PROFILE, "expert_profiles", String.valueOf(saved.getExpertId()), account.getAccountId());
        if (submissionMode.reopensReview()) {
            notifyStaffProfileSubmitted("EXPERT", saved.getExpertId(), account.getAccountId(), account.getFullName());
        }
        return saved;
    }

    // STAFF duyet ho so business/expert va ghi log audit.
    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `approveProfile` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    // Chức năng 3: Staff/Admin duyệt hoặc từ chối hồ sơ định danh doanh nghiệp/chuyên gia.
    public Object approveProfile(String type, Integer id, String status, String reason) {
        accessService.requireRole("STAFF");
        // CHI CHO PHEP 2 GIA TRI PHE DUYET DUNG THEO BUSINESS RULE.
        if (!"Approved".equalsIgnoreCase(status) && !"Rejected".equalsIgnoreCase(status)) {
            throw new AppException("STATUS PHE DUYET KHONG HOP LE");
        }
        String normalizedReason = normalizeRejectionReason(status, reason);
        AccountEntity actor = accessService.currentAccount();
        Integer staffId = resolveStaffId(actor.getAccountId());
        requireProfileReviewDomain(staffId);
        if ("BUSINESS".equalsIgnoreCase(type)) {
            BusinessProfileEntity b = businessProfileRepository.findById(id).orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
            requirePendingProfileStatus(b.getKybStatus());
            b.setKybStatus(status);
            b.setApprovedBy(staffId);
            b.setRejectionReason(normalizedReason);
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
        requirePendingProfileStatus(e.getKycStatus());
        e.setKycStatus(status);
        e.setApprovedBy(staffId);
        e.setRejectionReason(normalizedReason);
        updateAccountStatus(e.getAccountId(), status);
        audit(approvalAction("EXPERT", status), "expert_profiles", String.valueOf(id), actor.getAccountId());
        ExpertProfileEntity saved = expertProfileRepository.save(e);
        notificationService.notifyProfileReviewed(saved.getAccountId(), actor.getAccountId(), "EXPERT", status);
        return saved;
    }

    // Note: Hàm `currentBusinessProfile` lấy hồ sơ KYB của chính doanh nghiệp đang đăng nhập để reload trang vẫn thấy status/file mới nhất.
    // Chức năng 4: Lấy hồ sơ doanh nghiệp của tài khoản đang đăng nhập.
    public BusinessProfileEntity currentBusinessProfile() {
        accessService.requireRole("BUSINESS");
        Integer accountId = accessService.currentAccount().getAccountId();
        return businessProfileRepository.findByAccountId(accountId)
                .map(this::attachBusinessAccountInfo)
                .orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
    }

    // Note: Hàm `businessProfileById` lấy hồ sơ doanh nghiệp theo businessId để hiển thị trang cá nhân public cho expert xem.
    // Chức năng 5: Lấy hồ sơ doanh nghiệp theo ID để xem công khai hoặc duyệt hồ sơ.
    public BusinessProfileEntity businessProfileById(Integer businessId) {
        return businessProfileRepository.findById(businessId)
                .map(this::attachBusinessAccountInfo)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
    }

    // Note: Hàm `businessProfileByJob` lấy hồ sơ doanh nghiệp đăng một job để chuyên gia xem chi tiết khi job đã public.
    // Chức năng 6: Lấy hồ sơ doanh nghiệp theo Job để chuyên gia xem trước khi nộp proposal.
    public BusinessProfileEntity businessProfileByJob(Integer jobId) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        // Job OPEN là dữ liệu public nên chuyên gia được xem doanh nghiệp đăng job; job chưa public vẫn giới hạn cho nội bộ có quyền.
        if (!"OPEN".equalsIgnoreCase(job.getStatus())) {
            accessService.requireRole("STAFF", "ADMIN", "BUSINESS");
        }
        return businessProfileRepository.findById(job.getBusinessId())
                .map(this::attachBusinessAccountInfo)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
    }

    // Note: Hàm `currentExpertProfile` lấy hồ sơ KYC của chính chuyên gia đang đăng nhập để reload trang vẫn thấy status mới nhất.
    // Chức năng 7: Lấy hồ sơ chuyên gia của tài khoản đang đăng nhập.
    public ExpertProfileEntity currentExpertProfile() {
        accessService.requireRole("EXPERT");
        Integer accountId = accessService.currentAccount().getAccountId();
        return expertProfileRepository.findByAccountId(accountId)
                .map(this::attachExpertAccountInfo)
                .orElseThrow(() -> new NotFoundException("CHUA CO EXPERT PROFILE"));
    }

    // Note: Hàm `expertProfileById` lấy hồ sơ chuyên gia theo expertId để hiển thị trang cá nhân public cho business xem.
    // Chức năng 8: Lấy hồ sơ chuyên gia theo ID để xem công khai hoặc duyệt hồ sơ.
    public ExpertProfileEntity expertProfileById(Integer expertId) {
        accessService.requireRole("EXPERT", "BUSINESS", "STAFF", "ADMIN");
        return expertProfileRepository.findById(expertId)
                .map(this::attachExpertPublicAccountInfo)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY EXPERT PROFILE"));
    }

    // Note: Hàm `currentPortfolio` lấy portfolio của chính chuyên gia đang đăng nhập để form không mất dữ liệu sau khi reload.
    // Chức năng 9: Lấy portfolio năng lực của chuyên gia đang đăng nhập.
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
    // Chức năng 10: Tạo URL xem file hồ sơ hoặc tài liệu đã upload.
    public String createFileViewUrl(String path) {
        accessService.requireRole("STAFF", "ADMIN", "BUSINESS", "EXPERT");
        if (path != null && (path.startsWith("http://") || path.startsWith("https://"))) {
            return path;
        }
        return firebaseStorageService.createReadUrl(path);
    }

    // Chức năng 11: Lấy toàn bộ hồ sơ doanh nghiệp cho màn duyệt định danh.
    public List<BusinessProfileEntity> allBusinessProfiles() {
        accessService.requireRole("STAFF");
        requireCurrentStaffProfileReviewDomain();
        return businessProfileRepository.findAll().stream()
                .map(this::attachBusinessAccountInfo)
                .toList();
    }
    // Note: Hàm `allExpertProfiles` cho STAFF quản trị hồ sơ và BUSINESS đọc thông tin expert khi xem proposal.
    // Chức năng 12: Lấy toàn bộ hồ sơ chuyên gia cho màn duyệt định danh.
    public List<ExpertProfileEntity> allExpertProfiles() {
        accessService.requireRole("STAFF", "BUSINESS");
        AccountEntity actor = accessService.currentAccount();
        if (actor.getRole() != null && "STAFF".equals(actor.getRole().getRoleName())) {
            requireProfileReviewDomain(resolveStaffId(actor.getAccountId()));
        }
        return expertProfileRepository.findAll().stream()
                .map(this::attachExpertAccountInfo)
                .toList();
    }
    // Note: Hàm `allPortfolios` cho STAFF quản trị portfolio và BUSINESS xem năng lực expert trong màn proposal.
    public List<PortfolioEntity> allPortfolios() {
        accessService.requireRole("STAFF", "BUSINESS");
        AccountEntity actor = accessService.currentAccount();
        if (actor.getRole() != null && "STAFF".equals(actor.getRole().getRoleName())) {
            requireProfileReviewDomain(resolveStaffId(actor.getAccountId()));
        }
        return portfolioRepository.findAll();
    }

    // Note: Hàm `uploadBusinessLicense` upload file giấy phép kinh doanh lên Firebase Storage và trả về storage path để lưu vào hồ sơ KYB.
    // Chức năng 13: Upload giấy phép kinh doanh phục vụ định danh doanh nghiệp.
    public String uploadBusinessLicense(MultipartFile file) {
        accessService.requireRole("BUSINESS");
        Integer accountId = accessService.currentAccount().getAccountId();
        String path = firebaseStorageService.upload(file, "business-licenses/accounts/" + accountId);
        businessProfileRepository.findByAccountId(accountId)
                .ifPresent(profile -> auditLogService.record(AuditLogService.ACTION_UPLOAD_BUSINESS_LICENSE, "business_profiles", String.valueOf(profile.getBusinessId()), accountId));
        return path;
    }

    // Note: Hàm `uploadExpertCertificate` upload file chứng chỉ chuyên gia lên Firebase Storage và trả về storage path để lưu vào portfolio.
    // Chức năng 14: Upload chứng chỉ chuyên gia phục vụ hồ sơ năng lực.
    public String uploadExpertCertificate(MultipartFile file) {
        accessService.requireRole("EXPERT");
        Integer accountId = accessService.currentAccount().getAccountId();
        String path = firebaseStorageService.upload(file, "expert-certificates/accounts/" + accountId);
        expertProfileRepository.findByAccountId(accountId)
                .ifPresent(profile -> auditLogService.record(AuditLogService.ACTION_UPLOAD_EXPERT_CERTIFICATE, "expert_profiles", String.valueOf(profile.getExpertId()), accountId));
        return path;
    }

    // Note: Hàm `uploadExpertPortfolio` upload file portfolio chuyên gia lên Firebase Storage và trả về storage path để frontend lưu vào hồ sơ KYC/portfolio.
    // Chức năng 15: Upload file portfolio hoặc hồ sơ năng lực của chuyên gia.
    public String uploadExpertPortfolio(MultipartFile file) {
        accessService.requireRole("EXPERT");
        Integer accountId = accessService.currentAccount().getAccountId();
        String path = firebaseStorageService.upload(file, "expert-portfolios/accounts/" + accountId);
        expertProfileRepository.findByAccountId(accountId)
                .ifPresent(profile -> auditLogService.record(AuditLogService.ACTION_UPLOAD_EXPERT_PORTFOLIO_FILE, "expert_profiles", String.valueOf(profile.getExpertId()), accountId));
        return path;
    }

    // TAO HOAC CAP NHAT PORTFOLIO MOI CUA CHUYEN GIA DE BUSINESS DOC KHI REVIEW PROPOSAL.
    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `upsertPortfolio` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    // Chức năng 16: Tạo hoặc cập nhật portfolio năng lực của chuyên gia.
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
        accountRepository.findById(business.getAccountId()).ifPresent(account -> {
            business.setFullName(account.getFullName());
            business.setEmail(account.getEmail());
            business.setPhone(account.getPhone());
        });
        business.setAverageRating(reviewRepository.averageRatingByRevieweeId(business.getAccountId()));
        return business;
    }

    // Note: Hàm `attachExpertPublicAccountInfo` chỉ gắn thông tin public của tài khoản vào trang cá nhân expert.
    private ExpertProfileEntity attachExpertPublicAccountInfo(ExpertProfileEntity expert) {
        accountRepository.findById(expert.getAccountId()).ifPresent(account -> {
            expert.setFullName(account.getFullName());
            expert.setEmail(account.getEmail());
            expert.setPhone(account.getPhone());
            expert.setTitle("Chuyên gia AI");
        });
        expert.setAverageRating(reviewRepository.averageRatingByRevieweeId(expert.getAccountId()));
        return expert;
    }

    // Note: Hàm `attachExpertAccountInfo` gắn thông tin tài khoản đọc được vào response expert để BUSINESS xem chi tiết proposal.
    private ExpertProfileEntity attachExpertAccountInfo(ExpertProfileEntity expert) {
        accountRepository.findById(expert.getAccountId()).ifPresent(account -> {
            expert.setFullName(account.getFullName());
            expert.setEmail(account.getEmail());
            expert.setPhone(account.getPhone());
            expert.setTitle("Chuyên gia AI");
        });
        expert.setAverageRating(reviewRepository.averageRatingByRevieweeId(expert.getAccountId()));
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

    private String normalizeRejectionReason(String status, String reason) {
        if (!"Rejected".equalsIgnoreCase(status)) return null;
        if (reason == null || reason.isBlank()) {
            throw new AppException("LY DO TU CHOI KHONG DUOC DE TRONG");
        }
        String normalized = reason.trim();
        if (normalized.length() > 500) {
            throw new AppException("LY DO TU CHOI KHONG DUOC VUOT QUA 500 KY TU");
        }
        return normalized;
    }

    private ProfileSubmissionMode resolveBusinessSubmissionMode(BusinessProfileEntity entity) {
        if (entity.getBusinessId() == null) return ProfileSubmissionMode.REOPEN_REVIEW;
        return resolveSubmissionMode(entity.getKybStatus());
    }

    private String resolveBusinessAddress(
            BusinessProfileEntity input,
            TaxCheckResponse vietQrData,
            ProfileSubmissionMode submissionMode
    ) {
        if (submissionMode == ProfileSubmissionMode.UPDATE_APPROVED_ONLY
                && input.getAddress() != null
                && !input.getAddress().isBlank()) {
            return input.getAddress().trim();
        }
        return vietQrData.getAddress();
    }

    private ProfileSubmissionMode resolveExpertSubmissionMode(ExpertProfileEntity entity) {
        if (entity.getExpertId() == null) return ProfileSubmissionMode.REOPEN_REVIEW;
        return resolveSubmissionMode(entity.getKycStatus());
    }

    private void syncPortfolioYearsExperience(ExpertProfileEntity expert) {
        if (expert.getExpertId() == null || expert.getYearsOfExperience() == null) {
            return;
        }
        portfolioRepository.findByExpertId(expert.getExpertId()).ifPresent(portfolio -> {
            portfolio.setYearsExperience(expert.getYearsOfExperience());
            portfolioRepository.save(portfolio);
        });
    }

    private ProfileSubmissionMode resolveSubmissionMode(String currentStatus) {
        if ("Pending".equalsIgnoreCase(currentStatus)) {
            throw new AppException("HO SO DANG CHO DUYET, VUI LONG DOI KET QUA XET DUYET");
        }
        if ("Approved".equalsIgnoreCase(currentStatus)) {
            return ProfileSubmissionMode.UPDATE_APPROVED_ONLY;
        }
        return ProfileSubmissionMode.REOPEN_REVIEW;
    }

    private void requirePendingProfileStatus(String currentStatus) {
        if (!"Pending".equalsIgnoreCase(currentStatus)) {
            throw new AppException("CHI DUOC XET DUYET HO SO DANG CHO DUYET");
        }
    }

    private enum ProfileSubmissionMode {
        REOPEN_REVIEW,
        UPDATE_APPROVED_ONLY;

        boolean reopensReview() {
            return this == REOPEN_REVIEW;
        }
    }

    private void notifyStaffProfileSubmitted(String profileType, Integer profileId, Integer submitterAccountId, String displayName) {
        domainRepository.findByDomainCode(CatalogService.PROFILE_REVIEW_DOMAIN_CODE)
                .ifPresent(domain -> staffDomainRepository.findByIdDomainId(domain.getDomainId()).forEach(mapping ->
                        staffRepository.findById(mapping.getId().getStaffId()).ifPresent(staff ->
                                notificationService.notifyProfileSubmitted(
                                        staff.getAccountId(),
                                        submitterAccountId,
                                        profileType,
                                        profileId,
                                        displayName
                                )
                        )
                ));
    }

    // BAT BUOC TAI KHOAN STAFF PHAI CO BAN GHI TRONG BANG staffs DE LUU approvedBy.
    // Note: Hàm `resolveStaffId` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private Integer resolveStaffId(Integer accountId) {
        return staffRepository.findByAccountId(accountId)
                .map(StaffEntity::getStaffId)
                .orElseThrow(() -> new AppException("TAI KHOAN STAFF CHUA DUOC KHOI TAO HO SO NHAN SU (staffs)"));
    }

    private void requireCurrentStaffProfileReviewDomain() {
        AccountEntity actor = accessService.currentAccount();
        requireProfileReviewDomain(resolveStaffId(actor.getAccountId()));
    }

    private void requireProfileReviewDomain(Integer staffId) {
        DomainEntity profileReviewDomain = domainRepository.findByDomainCode(CatalogService.PROFILE_REVIEW_DOMAIN_CODE)
                .orElseThrow(() -> new AppException("CHUA CAU HINH DOMAIN XET DUYET HO SO"));
        if (!staffDomainRepository.existsByIdStaffIdAndIdDomainId(staffId, profileReviewDomain.getDomainId())) {
            throw new ForbiddenException("STAFF KHONG CO QUYEN XET DUYET HO SO");
        }
    }
}
