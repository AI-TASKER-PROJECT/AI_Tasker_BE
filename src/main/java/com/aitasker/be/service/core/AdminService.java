/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/AdminService.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.dto.admin.AccountRequest;
import com.aitasker.be.dto.admin.AccountResponse;
import com.aitasker.be.dto.admin.AuditLogResponse;
import com.aitasker.be.dto.admin.StaffRequest;
import com.aitasker.be.dto.admin.StaffResponse;
import com.aitasker.be.dto.admin.SystemSettingRequest;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Note: Annotation này cho Spring quản lý class như một service chứa nghiệp vụ.
@Service
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class AdminService {
    private final AccessService accessService;
    private final ContractRepository contractRepository;
    private final DisputeRepository disputeRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final StaffRepository staffRepository;
    private final ReviewRepository reviewRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final PaymentWalletService paymentWalletService;
    private final NotificationService notificationService;
    private final StaffDomainRepository staffDomainRepository;
    private final StaffSkillRepository staffSkillRepository;
    private final DomainRepository domainRepository;
    private final SkillRepository skillRepository;

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `createReview` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public ReviewEntity createReview(ReviewEntity input) {
        // CHI CHO BUSINESS/EXPERT TAO DANH GIA SAU KHI HOP DONG DA KET THUC.
        accessService.requireRole("BUSINESS", "EXPERT");
        accessService.requireApprovedAccount();
        if (input.getRating() == null || input.getRating().doubleValue() < 1.0 || input.getRating().doubleValue() > 5.0) {
            throw new AppException("RATING PHAI NAM TRONG KHOANG 1 DEN 5");
        }
        ContractEntity contract = contractRepository.findById(input.getContractId()).orElseThrow(() -> new NotFoundException("KHONG TIM THAY CONTRACT"));
        if (!ContractEntity.STATUS_CLOSED.equals(contract.getStatus())) {
            throw new AppException("CHI DUOC DANH GIA KHI CONTRACT DA KET THUC");
        }
        AccountEntity actor = accessService.currentAccount();
        Integer actorBusinessId = businessProfileRepository.findByAccountId(actor.getAccountId()).map(BusinessProfileEntity::getBusinessId).orElse(null);
        Integer actorExpertId = expertProfileRepository.findByAccountId(actor.getAccountId()).map(ExpertProfileEntity::getExpertId).orElse(null);
        boolean isBusinessActor = actorBusinessId != null && actorBusinessId.equals(contract.getBusinessId());
        boolean isExpertActor = actorExpertId != null && actorExpertId.equals(contract.getExpertId());
        if (!isBusinessActor && !isExpertActor) throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        if (reviewRepository.existsByContractIdAndReviewerId(input.getContractId(), actor.getAccountId())) {
            throw new AppException("BAN DA GUI DANH GIA CHO CONTRACT NAY");
        }
        input.setReviewId(null);
        input.setReviewerId(actor.getAccountId());
        if (isBusinessActor) {
            Integer targetAccountId = expertProfileRepository.findById(contract.getExpertId())
                    .map(ExpertProfileEntity::getAccountId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY TAI KHOAN EXPERT"));
            input.setRevieweeId(targetAccountId);
        } else {
            Integer targetAccountId = businessProfileRepository.findById(contract.getBusinessId())
                    .map(BusinessProfileEntity::getAccountId).orElseThrow(() -> new NotFoundException("KHONG TIM THAY TAI KHOAN BUSINESS"));
            input.setRevieweeId(targetAccountId);
        }
        ReviewEntity saved = reviewRepository.save(input);
        auditLogService.record("REVIEW_CREATED", "reviews", String.valueOf(saved.getReviewId()), actor.getAccountId());
        notificationService.notifyContractEvent(saved.getRevieweeId(), actor.getAccountId(), "REVIEW_CREATED",
                "Bạn nhận được đánh giá mới", "Một bên trong hợp đồng đã gửi đánh giá sau khi hợp đồng đóng.",
                contract.getContractId());
        return saved;
    }

    // Note: Hàm `listReviewsByContract` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<ReviewEntity> listReviewsByContract(Integer contractId) {
        accessService.requireRole("ADMIN", "STAFF", "BUSINESS", "EXPERT");
        return reviewRepository.findByContractId(contractId);
    }

    // Note: Hàm `listSettings` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<SystemSettingEntity> listSettings() {
        accessService.requireRole("ADMIN");
        return systemSettingRepository.findAll();
    }

    @Transactional
    public SystemSettingEntity createSetting(SystemSettingRequest request) {
        accessService.requireRole("ADMIN");
        validateSettingRequest(request, true);
        String key = normalizeSettingKey(request.getSettingKey());
        if (systemSettingRepository.existsById(key)) {
            throw new AppException("SYSTEM SETTING DA TON TAI");
        }
        AccountEntity actor = accessService.currentAccount();
        SystemSettingEntity setting = SystemSettingEntity.builder()
                .settingKey(key)
                .settingValue(request.getSettingValue().trim())
                .valueType(normalizeValueType(request.getValueType()))
                .description(request.getDescription())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .updatedByRoleId(actor.getRole() == null ? null : actor.getRole().getRoleId())
                .build();
        SystemSettingEntity saved = systemSettingRepository.save(setting);
        auditLogService.record(AuditLogService.ACTION_UPDATE_SYSTEM_SETTING, "system_settings", key, actor.getAccountId());
        return saved;
    }

    // Note: Hàm `listAuditLogs` chỉ cho admin lấy danh sách audit log và lọc theo nhóm role nội bộ/bên ngoài.
    public List<AuditLogResponse> listAuditLogs(String actorGroup) {
        return auditLogService.listForAdmin(actorGroup);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `updateSetting` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public SystemSettingEntity updateSetting(String key, String value, Boolean isActive) {
        accessService.requireRole("ADMIN");
        SystemSettingEntity setting = systemSettingRepository.findById(key).orElseThrow(() -> new NotFoundException("KHONG TIM THAY SYSTEM SETTING"));
        // CHI CHO PHEP CAP NHAT GIA TRI/CO HIEU LUC, KHONG CHO DOI VALUE_TYPE TRANH VO HOP DONG DU LIEU.
        if (value != null && !value.isBlank()) setting.setSettingValue(value);
        if (isActive != null) setting.setIsActive(isActive);
        AccountEntity actor = accessService.currentAccount();
        setting.setUpdatedByRoleId(actor.getRole().getRoleId());
        SystemSettingEntity saved = systemSettingRepository.save(setting);
        auditLogService.record(AuditLogService.ACTION_UPDATE_SYSTEM_SETTING, "system_settings", key, actor.getAccountId());
        return saved;
    }

    @Transactional
    public SystemSettingEntity updateSetting(String key, SystemSettingRequest request) {
        accessService.requireRole("ADMIN");
        if (request == null) throw new AppException("BODY REQUEST KHONG HOP LE");
        String normalizedKey = normalizeSettingKey(key);
        SystemSettingEntity setting = systemSettingRepository.findById(normalizedKey)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY SYSTEM SETTING"));
        if (request.getSettingValue() != null && !request.getSettingValue().isBlank()) {
            setting.setSettingValue(request.getSettingValue().trim());
        }
        if (request.getValueType() != null && !request.getValueType().isBlank()) {
            setting.setValueType(normalizeValueType(request.getValueType()));
        }
        if (request.getDescription() != null) setting.setDescription(request.getDescription());
        if (request.getIsActive() != null) setting.setIsActive(request.getIsActive());
        AccountEntity actor = accessService.currentAccount();
        setting.setUpdatedByRoleId(actor.getRole() == null ? null : actor.getRole().getRoleId());
        SystemSettingEntity saved = systemSettingRepository.save(setting);
        auditLogService.record(AuditLogService.ACTION_UPDATE_SYSTEM_SETTING, "system_settings", normalizedKey, actor.getAccountId());
        return saved;
    }

    @Transactional
    public SystemSettingEntity deleteSetting(String key) {
        accessService.requireRole("ADMIN");
        String normalizedKey = normalizeSettingKey(key);
        SystemSettingEntity setting = systemSettingRepository.findById(normalizedKey)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY SYSTEM SETTING"));
        setting.setIsActive(false);
        AccountEntity actor = accessService.currentAccount();
        setting.setUpdatedByRoleId(actor.getRole() == null ? null : actor.getRole().getRoleId());
        SystemSettingEntity saved = systemSettingRepository.save(setting);
        auditLogService.record(AuditLogService.ACTION_UPDATE_SYSTEM_SETTING, "system_settings", normalizedKey, actor.getAccountId());
        return saved;
    }

    // Note: Hàm `listStaffs` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<StaffResponse> listStaffs() {
        accessService.requireRole("ADMIN");
        return staffRepository.findAll().stream()
                .map(this::toStaffResponse)
                .toList();
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `createStaff` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public StaffResponse createStaff(StaffRequest request) {
        accessService.requireRole("ADMIN");
        if (request.getAccountId() == null) throw new AppException("ACCOUNT ID KHONG DUOC DE TRONG");
        AccountEntity account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY ACCOUNT"));
        if (!hasRole(account.getRole(), "STAFF")) {
            throw new AppException("ACCOUNT PHAI CO ROLE STAFF");
        }
        if (request.getDomainIds() == null || request.getDomainIds().isEmpty()) {
            throw new AppException("DOMAIN IDS KHONG DUOC DE TRONG");
        }
        validateCatalogIds(request.getDomainIds(), request.getSkillIds());
        StaffEntity staff = ensureStaffProfile(account.getAccountId(), request.getSpecialization());
        syncStaffDomains(staff.getStaffId(), request.getDomainIds());
        syncStaffSkills(staff.getStaffId(), request.getSkillIds());
        auditLogService.record(AuditLogService.ACTION_CREATE_STAFF_PROFILE, "staffs",
                String.valueOf(staff.getStaffId()), accessService.currentAccount().getAccountId());
        return toStaffResponse(staff);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `updateStaff` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public StaffResponse updateStaff(Integer staffId, StaffRequest request) {
        accessService.requireRole("ADMIN");
        StaffEntity staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY STAFF"));
        if (request.getSpecialization() != null) {
            staff.setSpecialization(normalizeStaffSpecialization(request.getSpecialization()));
        }
        if (request.getDomainIds() != null) {
            if (request.getDomainIds().isEmpty()) {
                throw new AppException("DOMAIN IDS KHONG DUOC DE TRONG, STAFF PHAI CO IT NHAT MOT DOMAIN");
            }
            validateCatalogIds(request.getDomainIds(), request.getSkillIds());
            syncStaffDomains(staffId, request.getDomainIds());
        }
        if (request.getSkillIds() != null) {
            validateCatalogIds(request.getDomainIds() != null ? request.getDomainIds() : currentDomainIds(staffId),
                    request.getSkillIds());
            syncStaffSkills(staffId, request.getSkillIds());
        }
        StaffEntity saved = staffRepository.save(staff);
        auditLogService.record(AuditLogService.ACTION_UPDATE_STAFF_PROFILE, "staffs",
                String.valueOf(staffId), accessService.currentAccount().getAccountId());
        return toStaffResponse(saved);
    }

    // Note: Hàm `analyticsOverview` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public Map<String, Object> analyticsOverview() {
        accessService.requireRole("ADMIN");
        // TONG HOP CHI SO CO BAN DE HO TRO DASHBOARD QUAN TRI MVP.
        long totalContracts = contractRepository.count();
        long completedContracts = contractRepository.findAll().stream().filter(c -> "COMPLETED".equals(c.getStatus())).count();
        long terminatedContracts = contractRepository.findAll().stream().filter(c -> "CANCELLED".equals(c.getStatus())).count();
        long totalDisputes = disputeRepository.count();
        long openDisputes = disputeRepository.findAll().stream()
                .filter(d -> List.of(
                        DisputeEntity.STATUS_PENDING_SELF_RESOLVE,
                        DisputeEntity.STATUS_ESCALATION_REQUESTED,
                        DisputeEntity.STATUS_STAFF_REVIEWING,
                        DisputeEntity.STATUS_STAFF_DECIDED
                ).contains(d.getStatus()))
                .count();
        long totalTransactions = transactionRepository.count();
        BigDecimal totalVolume = transactionRepository.findAll().stream()
                .map(TransactionEntity::getAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal successRate = BigDecimal.ZERO;
        if (totalContracts > 0) {
            successRate = BigDecimal.valueOf(completedContracts * 100.0 / totalContracts).setScale(2, RoundingMode.HALF_UP);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalContracts", totalContracts);
        result.put("completedContracts", completedContracts);
        result.put("terminatedContracts", terminatedContracts);
        result.put("contractSuccessRatePercent", successRate);
        result.put("totalDisputes", totalDisputes);
        result.put("openDisputes", openDisputes);
        result.put("totalTransactions", totalTransactions);
        result.put("transactionVolume", totalVolume);
        return result;
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional(readOnly = true)
    // Note: Hàm `listAccounts` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public List<AccountResponse> listAccounts() {
        accessService.requireRole("ADMIN");
        return accountRepository.findAll().stream()
                .map(this::toAccountResponse)
                .toList();
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `createAccount` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public AccountResponse createAccount(AccountRequest request) {
        accessService.requireRole("ADMIN");
        validateAccountRequest(request, true);
        String email = normalizeEmail(request.getEmail());
        if (accountRepository.existsByEmailIgnoreCase(email)) {
            throw new AppException("EMAIL DA TON TAI");
        }
        RoleEntity role = roleRepository.findByRoleNameIgnoreCase(request.getRole())
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY ROLE"));
        AccountEntity account = AccountEntity.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(trimToNull(request.getPhone()))
                .fullName(request.getFullName().trim())
                .role(role)
                .status(resolveRequestedStatus(request.getStatus(), role.getRoleName()))
                .build();
        AccountEntity saved = accountRepository.save(account);
        if (hasRole(role, "STAFF")) {
            ensureStaffProfile(saved.getAccountId(), request.getSpecialization());
        }
        if (hasRole(role, "BUSINESS") || hasRole(role, "EXPERT")) {
            paymentWalletService.ensureQuotaForAccount(saved);
        }
        auditLogService.record(AuditLogService.ACTION_CREATE_ACCOUNT, "account", String.valueOf(saved.getAccountId()), accessService.currentAccount().getAccountId());
        notifyAdminsNewAccountCreated(saved);
        return toAccountResponse(saved);
    }

    private void notifyAdminsNewAccountCreated(AccountEntity account) {
        accountRepository.findAllByRoleRoleNameOrderByAccountIdAsc("ADMIN")
                .forEach(admin -> notificationService.notifyNewAccountCreated(
                        admin.getAccountId(),
                        account.getAccountId(),
                        account.getFullName(),
                        account.getEmail(),
                        account.getRole() == null ? null : account.getRole().getRoleName()
                ));
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `updateAccount` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public AccountResponse updateAccount(Integer accountId, AccountRequest request) {
        accessService.requireRole("ADMIN");
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY ACCOUNT"));
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = normalizeEmail(request.getEmail());
            accountRepository.findByEmailIgnoreCase(email)
                    .filter(existing -> !existing.getAccountId().equals(accountId))
                    .ifPresent(existing -> { throw new AppException("EMAIL DA TON TAI"); });
            account.setEmail(email);
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 8) throw new AppException("PASSWORD TOI THIEU 8 KY TU");
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getPhone() != null) account.setPhone(trimToNull(request.getPhone()));
        if (request.getFullName() != null && !request.getFullName().isBlank()) account.setFullName(request.getFullName().trim());
        if (request.getRole() != null && !request.getRole().isBlank()) {
            RoleEntity role = roleRepository.findByRoleNameIgnoreCase(request.getRole())
                    .orElseThrow(() -> new NotFoundException("KHONG TIM THAY ROLE"));
            account.setRole(role);
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            String newStatus = normalizeStatus(request.getStatus());
            account.setStatus(newStatus);
            if ("Lock".equals(newStatus)) {
                account.setLockReason("ADMIN_LOCKED");
            } else {
                account.setLockReason(null);
                account.setStatusBeforeLock(null);
            }
        }
        AccountEntity saved = accountRepository.save(account);
        if (hasRole(saved.getRole(), "STAFF")) {
            StaffEntity staff = ensureStaffProfile(saved.getAccountId(), request.getSpecialization());
            if (request.getSpecialization() != null) {
                staff.setSpecialization(normalizeStaffSpecialization(request.getSpecialization()));
                staffRepository.save(staff);
            }
        } else {
            staffRepository.findByAccountId(saved.getAccountId()).ifPresent(staffRepository::delete);
        }
        auditLogService.record(AuditLogService.ACTION_UPDATE_ACCOUNT, "account", String.valueOf(saved.getAccountId()), accessService.currentAccount().getAccountId());
        return toAccountResponse(saved);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `setAccountStatus` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public AccountResponse setAccountStatus(Integer accountId, String status) {
        accessService.requireRole("ADMIN");
        String normalizedStatus = normalizeStatus(status);
        AccountEntity actor = accessService.currentAccount();
        if (actor.getAccountId().equals(accountId) && "Lock".equals(normalizedStatus)) {
            throw new AppException("ADMIN KHONG THE TU KHOA TAI KHOAN DANG DANG NHAP");
        }
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY ACCOUNT"));
        account.setStatus(normalizedStatus);
        if ("Lock".equals(normalizedStatus)) {
            account.setLockReason("ADMIN_LOCKED");
        } else {
            account.setLockReason(null);
            account.setStatusBeforeLock(null);
        }
        AccountEntity saved = accountRepository.save(account);
        auditLogService.record(AuditLogService.ACTION_CHANGE_ACCOUNT_STATUS, "account", String.valueOf(accountId), actor.getAccountId());
        return toAccountResponse(saved);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `setAccountActive` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public AccountResponse setAccountActive(Integer accountId, boolean active) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY ACCOUNT"));
        if (active) {
            account.setStatus("Approved");
            account.setLockReason(null);
            account.setStatusBeforeLock(null);
        } else {
            account.setStatus("Lock");
            account.setLockReason("ADMIN_LOCKED");
        }
        AccountEntity saved = accountRepository.save(account);
        accessService.requireRole("ADMIN");
        auditLogService.record(AuditLogService.ACTION_CHANGE_ACCOUNT_STATUS, "account", String.valueOf(accountId), accessService.currentAccount().getAccountId());
        return toAccountResponse(saved);
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `deactivateAccount` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public AccountResponse deactivateAccount(Integer accountId) {
        return setAccountStatus(accountId, "Lock");
    }

    // Note: Hàm `validateAccountRequest` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private void validateAccountRequest(AccountRequest request, boolean creating) {
        if (request == null) throw new AppException("BODY REQUEST KHONG HOP LE");
        if (creating && (request.getEmail() == null || request.getEmail().isBlank())) throw new AppException("EMAIL KHONG DUOC DE TRONG");
        if (creating && (request.getPassword() == null || request.getPassword().length() < 8)) throw new AppException("PASSWORD TOI THIEU 8 KY TU");
        if (creating && (request.getFullName() == null || request.getFullName().isBlank())) throw new AppException("FULL NAME KHONG DUOC DE TRONG");
        if (creating && (request.getRole() == null || request.getRole().isBlank())) throw new AppException("ROLE KHONG DUOC DE TRONG");
    }

    // Note: Hàm `normalizeEmail` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    // Note: Hàm `trimToNull` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private void validateSettingRequest(SystemSettingRequest request, boolean creating) {
        if (request == null) throw new AppException("BODY REQUEST KHONG HOP LE");
        if (creating && (request.getSettingKey() == null || request.getSettingKey().isBlank())) {
            throw new AppException("SETTING KEY KHONG DUOC DE TRONG");
        }
        if (creating && (request.getSettingValue() == null || request.getSettingValue().isBlank())) {
            throw new AppException("SETTING VALUE KHONG DUOC DE TRONG");
        }
        if (creating && (request.getValueType() == null || request.getValueType().isBlank())) {
            throw new AppException("VALUE TYPE KHONG DUOC DE TRONG");
        }
    }

    private String normalizeSettingKey(String key) {
        if (key == null || key.isBlank()) throw new AppException("SETTING KEY KHONG DUOC DE TRONG");
        return key.trim();
    }

    private String normalizeValueType(String valueType) {
        String normalized = trimToNull(valueType);
        if (normalized == null) throw new AppException("VALUE TYPE KHONG DUOC DE TRONG");
        String upper = normalized.toUpperCase();
        for (String allowed : List.of("STRING", "INT", "DECIMAL", "BOOLEAN", "JSON")) {
            if (allowed.equals(upper)) return upper;
        }
        throw new AppException("VALUE TYPE KHONG HOP LE");
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Note: Hàm `resolveRequestedStatus` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private String resolveRequestedStatus(String requestedStatus, String roleName) {
        if (requestedStatus != null && !requestedStatus.isBlank()) return normalizeStatus(requestedStatus);
        if (isRoleName(roleName, "ADMIN") || isRoleName(roleName, "STAFF")) return "Approved";
        return "Pending";
    }

    // Note: Hàm `ensureStaffProfile` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private StaffEntity ensureStaffProfile(Integer accountId, String specialization) {
        return staffRepository.findByAccountId(accountId)
                .orElseGet(() -> staffRepository.save(StaffEntity.builder()
                        .accountId(accountId)
                        .specialization(normalizeStaffSpecialization(specialization))
                        .build()));
    }

    // Note: Hàm `normalizeStaffSpecialization` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private String normalizeStaffSpecialization(String specialization) {
        String normalized = trimToNull(specialization);
        return normalized == null ? "KYB/KYC profile verification" : normalized;
    }

    // Note: Hàm `toStaffResponse` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private StaffResponse toStaffResponse(StaffEntity staff) {
        AccountEntity account = accountRepository.findById(staff.getAccountId()).orElse(null);
        StaffResponse response = StaffResponse.from(staff, account);
        response.setActiveTickets(disputeRepository.countByAssignedStaffIdAndStatusIn(
                staff.getStaffId(),
                List.of(DisputeEntity.STATUS_STAFF_REVIEWING)
        ));
        List<StaffDomainEntity> domainMappings = staffDomainRepository.findByIdStaffId(staff.getStaffId());
        List<StaffSkillEntity> skillMappings = staffSkillRepository.findByIdStaffId(staff.getStaffId());
        if (!domainMappings.isEmpty()) {
            List<Integer> domainIds = domainMappings.stream().map(d -> d.getId().getDomainId()).toList();
            response.setDomains(domainRepository.findAllById(domainIds).stream()
                    .map(d -> StaffResponse.DomainSummary.builder()
                            .domainId(d.getDomainId()).domainCode(d.getDomainCode()).domainName(d.getDomainName()).build())
                    .toList());
        }
        if (!skillMappings.isEmpty()) {
            List<Integer> skillIds = skillMappings.stream().map(s -> s.getId().getSkillId()).toList();
            response.setSkills(skillRepository.findAllById(skillIds).stream()
                    .map(s -> StaffResponse.SkillSummary.builder()
                            .skillId(s.getSkillId()).skillCode(s.getSkillCode()).skillName(s.getSkillName()).build())
                    .toList());
        }
        return response;
    }

    private void validateCatalogIds(List<Integer> domainIds, List<Integer> skillIds) {
        if (domainIds != null && !domainIds.isEmpty()) {
            List<Integer> existing = domainRepository.findAllById(domainIds).stream()
                    .map(DomainEntity::getDomainId).toList();
            if (existing.size() != domainIds.stream().distinct().count()) {
                throw new NotFoundException("MOT SO DOMAIN ID KHONG TON TAI");
            }
        }
        if (skillIds != null && !skillIds.isEmpty()) {
            List<Integer> existing = skillRepository.findAllById(skillIds).stream()
                    .map(SkillEntity::getSkillId).toList();
            if (existing.size() != skillIds.stream().distinct().count()) {
                throw new NotFoundException("MOT SO SKILL ID KHONG TON TAI");
            }
        }
    }

    private void syncStaffDomains(Integer staffId, List<Integer> domainIds) {
        staffDomainRepository.deleteByIdStaffId(staffId);
        if (domainIds != null) {
            for (Integer domainId : domainIds.stream().distinct().toList()) {
                staffDomainRepository.save(new StaffDomainEntity(
                        new com.aitasker.be.entity.StaffDomainId(staffId, domainId)));
            }
        }
    }

    private void syncStaffSkills(Integer staffId, List<Integer> skillIds) {
        staffSkillRepository.deleteByIdStaffId(staffId);
        if (skillIds != null) {
            for (Integer skillId : skillIds.stream().distinct().toList()) {
                staffSkillRepository.save(new StaffSkillEntity(
                        new com.aitasker.be.entity.StaffSkillId(staffId, skillId)));
            }
        }
    }

    private List<Integer> currentDomainIds(Integer staffId) {
        return staffDomainRepository.findByIdStaffId(staffId).stream()
                .map(d -> d.getId().getDomainId()).toList();
    }

    // Note: Hàm `toAccountResponse` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private AccountResponse toAccountResponse(AccountEntity account) {
        AccountResponse response = AccountResponse.from(account);
        if (hasRole(account.getRole(), "STAFF")) {
            staffRepository.findByAccountId(account.getAccountId())
                    .map(StaffEntity::getSpecialization)
                    .ifPresent(response::setSpecialization);
        }
        return response;
    }

    // Note: Hàm `hasRole` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private boolean hasRole(RoleEntity role, String expectedRole) {
        return role != null && isRoleName(role.getRoleName(), expectedRole);
    }

    // Note: Hàm `isRoleName` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private boolean isRoleName(String roleName, String expectedRole) {
        return roleName != null && expectedRole.equalsIgnoreCase(roleName.trim());
    }

    // Note: Hàm `normalizeStatus` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) throw new AppException("ACCOUNT STATUS KHONG DUOC DE TRONG");
        String normalized = status.trim();
        for (String allowed : List.of("Pending", "Approved", "Rejected", "Lock")) {
            if (allowed.equalsIgnoreCase(normalized)) return allowed;
        }
        throw new AppException("ACCOUNT STATUS KHONG HOP LE");
    }
}
