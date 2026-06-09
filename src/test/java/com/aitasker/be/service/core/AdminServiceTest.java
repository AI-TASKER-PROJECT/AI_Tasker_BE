/*
 * NOTE FILE: src/test/java/com/aitasker/be/service/core/AdminServiceTest.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.dto.admin.AccountRequest;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.ReviewEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.entity.StaffEntity;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
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

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @InjectMocks private AdminService adminService;

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

        adminService.createAccount(request);

        ArgumentCaptor<StaffEntity> staffCaptor = ArgumentCaptor.forClass(StaffEntity.class);
        verify(staffRepository).save(staffCaptor.capture());
        assertEquals(99, staffCaptor.getValue().getAccountId());
        assertEquals("KYB/KYC profile verification, Data Engineering", staffCaptor.getValue().getSpecialization());
    }
}
