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

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private AccessService accessService;
    @Mock private ContractRepository contractRepository;
    @Mock private DisputeRepository disputeRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private BusinessProfileRepository businessProfileRepository;
    @Mock private ExpertProfileRepository expertProfileRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private SystemSettingRepository systemSettingRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AdminService adminService;

    @Test
    void createReview_shouldThrowWhenRatingOutOfRange() {
        ReviewEntity input = ReviewEntity.builder()
                .contractId(1)
                .rating(new BigDecimal("5.5"))
                .build();
        AppException ex = assertThrows(AppException.class, () -> adminService.createReview(input));
        assertEquals("RATING PHAI NAM TRONG KHOANG 1 DEN 5", ex.getMessage());
    }

    @Test
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
