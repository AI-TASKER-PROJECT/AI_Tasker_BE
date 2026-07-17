/*
 * NOTE FILE: src/test/java/com/aitasker/be/service/core/AdminServiceTest.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;


import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.dto.admin.AccountRequest;
import com.aitasker.be.dto.admin.SystemSettingRequest;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.ReviewEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.entity.StaffEntity;
import com.aitasker.be.entity.SystemSettingEntity;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private AccessService accessService;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private ContractRepository contractRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private DisputeRepository disputeRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private TransactionRepository transactionRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private AccountRepository accountRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private BusinessProfileRepository businessProfileRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private ExpertProfileRepository expertProfileRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private StaffRepository staffRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private ReviewRepository reviewRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private SystemSettingRepository systemSettingRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private RoleRepository roleRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditLogService auditLogService;
    @Mock private PaymentWalletService paymentWalletService;
    @Mock private NotificationService notificationService;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @InjectMocks private AdminService adminService;

    @Test
    void createSetting_shouldPersistNewSystemSetting() {
        SystemSettingRequest request = new SystemSettingRequest();
        request.setSettingKey("default_sla_days");
        request.setSettingValue("3");
        request.setValueType("INT");
        request.setDescription("So ngay SLA xet duyet");

        AccountEntity admin = adminAccount();
        when(systemSettingRepository.existsById("default_sla_days")).thenReturn(false);
        when(accessService.currentAccount()).thenReturn(admin);
        when(systemSettingRepository.save(any(SystemSettingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SystemSettingEntity saved = adminService.createSetting(request);

        assertEquals("default_sla_days", saved.getSettingKey());
        assertEquals("3", saved.getSettingValue());
        assertEquals("INT", saved.getValueType());
        assertEquals(Boolean.TRUE, saved.getIsActive());
        assertEquals(Integer.valueOf(1), saved.getUpdatedByRoleId());
        verify(auditLogService).record(AuditLogService.ACTION_UPDATE_SYSTEM_SETTING, "system_settings", "default_sla_days", 1);
    }

    @Test
    void updateSettingBody_shouldUpdateMutableFields() {
        SystemSettingEntity setting = SystemSettingEntity.builder()
                .settingKey("default_sla_days")
                .settingValue("7")
                .valueType("INT")
                .isActive(true)
                .build();
        SystemSettingRequest request = new SystemSettingRequest();
        request.setSettingValue("3");
        request.setValueType("int");
        request.setDescription("SLA xet duyet moi");
        request.setIsActive(false);

        when(systemSettingRepository.findById("default_sla_days")).thenReturn(Optional.of(setting));
        when(accessService.currentAccount()).thenReturn(adminAccount());
        when(systemSettingRepository.save(any(SystemSettingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SystemSettingEntity saved = adminService.updateSetting("default_sla_days", request);

        assertEquals("3", saved.getSettingValue());
        assertEquals("INT", saved.getValueType());
        assertEquals("SLA xet duyet moi", saved.getDescription());
        assertEquals(Boolean.FALSE, saved.getIsActive());
        verify(auditLogService).record(AuditLogService.ACTION_UPDATE_SYSTEM_SETTING, "system_settings", "default_sla_days", 1);
    }

    @Test
    void deleteSetting_shouldDeactivateSetting() {
        SystemSettingEntity setting = SystemSettingEntity.builder()
                .settingKey("default_sla_days")
                .settingValue("3")
                .valueType("INT")
                .isActive(true)
                .build();

        when(systemSettingRepository.findById("default_sla_days")).thenReturn(Optional.of(setting));
        when(accessService.currentAccount()).thenReturn(adminAccount());
        when(systemSettingRepository.save(any(SystemSettingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SystemSettingEntity saved = adminService.deleteSetting("default_sla_days");

        assertEquals(Boolean.FALSE, saved.getIsActive());
        assertEquals(Integer.valueOf(1), saved.getUpdatedByRoleId());
        verify(auditLogService).record(AuditLogService.ACTION_UPDATE_SYSTEM_SETTING, "system_settings", "default_sla_days", 1);
    }

    @Test
    void createSetting_shouldRejectUnsupportedSettingKey() {
        SystemSettingRequest request = new SystemSettingRequest();
        request.setSettingKey("platform_fee_percent");
        request.setSettingValue("10");
        request.setValueType("DECIMAL");

        AppException ex = assertThrows(AppException.class, () -> adminService.createSetting(request));

        assertEquals("SYSTEM SETTING KHONG DUOC HO TRO", ex.getMessage());
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `createReview_shouldThrowWhenRatingOutOfRange` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void createReview_shouldThrowWhenRatingOutOfRange() {
        ReviewEntity input = ReviewEntity.builder()
                .contractId(1)
                .rating(new BigDecimal("5.5"))
                .build();
        AppException ex = assertThrows(AppException.class, () -> adminService.createReview(input));
        assertEquals("RATING PHAI NAM TRONG KHOANG 1 DEN 5", ex.getMessage());
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `createAccount_shouldCreateStaffProfileWhenRoleIsStaff` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void createAccount_shouldCreateStaffProfileWhenRoleIsStaff() {
        AccountRequest request = new AccountRequest();
        request.setEmail("new.staff@mail.com");
        request.setPassword("12345678");
        request.setFullName("New Staff");
        request.setPhone("0900999000");
        request.setRole("STAFF");
        request.setStatus("Approved");
        request.setSpecialization("KYB/KYC profile verification, Data Engineering");

        RoleEntity staffRole = RoleEntity.builder().roleId(4).roleName("STAFF").build();
        when(accountRepository.existsByEmailIgnoreCase("new.staff@mail.com")).thenReturn(false);
        when(roleRepository.findByRoleNameIgnoreCase("STAFF")).thenReturn(Optional.of(staffRole));
        when(passwordEncoder.encode("12345678")).thenReturn("hashed");
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity account = invocation.getArgument(0);
            account.setAccountId(99);
            return account;
        });
        when(staffRepository.findByAccountId(99)).thenReturn(Optional.empty());
        when(staffRepository.save(any(StaffEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accessService.currentAccount()).thenReturn(AccountEntity.builder().accountId(1).role(RoleEntity.builder().roleName("ADMIN").build()).build());

        adminService.createAccount(request);

        ArgumentCaptor<StaffEntity> staffCaptor = ArgumentCaptor.forClass(StaffEntity.class);
        verify(staffRepository).save(staffCaptor.capture());
        assertEquals(99, staffCaptor.getValue().getAccountId());
        assertEquals("KYB/KYC profile verification, Data Engineering", staffCaptor.getValue().getSpecialization());
    }

    @Test
    void createAccount_shouldEnsureQuotaWhenRoleIsBusiness() {
        AccountRequest request = new AccountRequest();
        request.setEmail("new.business@mail.com");
        request.setPassword("12345678");
        request.setFullName("New Business");
        request.setPhone("0900999001");
        request.setRole("BUSINESS");
        request.setStatus("Approved");

        RoleEntity businessRole = RoleEntity.builder().roleId(2).roleName("BUSINESS").build();
        when(accountRepository.existsByEmailIgnoreCase("new.business@mail.com")).thenReturn(false);
        when(roleRepository.findByRoleNameIgnoreCase("BUSINESS")).thenReturn(Optional.of(businessRole));
        when(passwordEncoder.encode("12345678")).thenReturn("hashed");
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity account = invocation.getArgument(0);
            account.setAccountId(100);
            return account;
        });
        when(accessService.currentAccount()).thenReturn(AccountEntity.builder().accountId(1).role(RoleEntity.builder().roleName("ADMIN").build()).build());

        adminService.createAccount(request);

        verify(paymentWalletService).ensureQuotaForAccount(org.mockito.ArgumentMatchers.argThat(account ->
                Integer.valueOf(100).equals(account.getAccountId())
                        && "BUSINESS".equals(account.getRole().getRoleName())
        ));
    }

    @Test
    void createAccount_shouldNotifyAdminsWhenNewAccountIsCreated() {
        AccountRequest request = new AccountRequest();
        request.setEmail("new.expert@mail.com");
        request.setPassword("12345678");
        request.setFullName("New Expert");
        request.setPhone("0900999002");
        request.setRole("EXPERT");
        request.setStatus("Pending");

        RoleEntity expertRole = RoleEntity.builder().roleId(3).roleName("EXPERT").build();
        AccountEntity admin = AccountEntity.builder().accountId(1).role(RoleEntity.builder().roleName("ADMIN").build()).build();
        when(accountRepository.existsByEmailIgnoreCase("new.expert@mail.com")).thenReturn(false);
        when(roleRepository.findByRoleNameIgnoreCase("EXPERT")).thenReturn(Optional.of(expertRole));
        when(passwordEncoder.encode("12345678")).thenReturn("hashed");
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity account = invocation.getArgument(0);
            account.setAccountId(101);
            return account;
        });
        when(accessService.currentAccount()).thenReturn(admin);
        when(accountRepository.findAllByRoleRoleNameOrderByAccountIdAsc("ADMIN")).thenReturn(List.of(admin));

        adminService.createAccount(request);

        verify(notificationService).notifyNewAccountCreated(1, 101, "New Expert", "new.expert@mail.com", "EXPERT");
    }

    private AccountEntity adminAccount() {
        return AccountEntity.builder()
                .accountId(1)
                .role(RoleEntity.builder().roleId(1).roleName("ADMIN").build())
                .build();
    }
}
