/*
 * NOTE FILE: src/test/java/com/aitasker/be/service/core/ProfileServiceTest.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.repository.AuditLogRepository;
import com.aitasker.be.repository.BusinessProfileRepository;
import com.aitasker.be.repository.ExpertProfileRepository;
import com.aitasker.be.repository.PortfolioRepository;
import com.aitasker.be.repository.StaffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private AccessService accessService;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private BusinessProfileRepository businessProfileRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private ExpertProfileRepository expertProfileRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private AccountRepository accountRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private PortfolioRepository portfolioRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private StaffRepository staffRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private AuditLogRepository auditLogRepository;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @InjectMocks private ProfileService profileService;

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `upsertBusiness_shouldThrowWhenTaxCodeBlank` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void upsertBusiness_shouldThrowWhenTaxCodeBlank() {
        BusinessProfileEntity input = BusinessProfileEntity.builder().taxCode(" ").companyName("ABC").build();
        AppException ex = assertThrows(AppException.class, () -> profileService.upsertBusiness(input));
        assertEquals("TAX CODE KHONG DUOC DE TRONG", ex.getMessage());
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `upsertPortfolio_shouldThrowWhenDomainIdsBlank` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void upsertPortfolio_shouldThrowWhenDomainIdsBlank() {
        AppException ex = assertThrows(AppException.class, () -> profileService.upsertPortfolio(com.aitasker.be.entity.PortfolioEntity.builder()
                .domainIds(" ")
                .skillIds("2,3")
                .yearsExperience(5)
                .selfDescription("Mo ta")
                .build()));
        assertEquals("DOMAIN IDS KHONG DUOC DE TRONG", ex.getMessage());
    }

    @Test
    void businessProfileById_shouldThrowWhenProfileNotFound() {
        Integer businessId = 999;
        doNothing().when(accessService).requireRole(anyString(), anyString(), anyString(), anyString());
        when(businessProfileRepository.findById(businessId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> profileService.businessProfileById(businessId));
        assertEquals("KHONG TIM THAY BUSINESS PROFILE", ex.getMessage());
        verify(accessService).requireRole("EXPERT", "BUSINESS", "STAFF", "ADMIN");
    }

    @Test
    void businessProfileById_shouldReturnProfileWithFullNameOnly() {
        Integer businessId = 1;
        Integer accountId = 10;
        BusinessProfileEntity profile = BusinessProfileEntity.builder()
                .businessId(businessId).accountId(accountId)
                .companyName("Test Corp").taxCode("1234567890")
                .kybStatus("Approved").build();
        AccountEntity account = AccountEntity.builder()
                .accountId(accountId).fullName("Owner Name").email("e@x.com").phone("090")
                .build();

        doNothing().when(accessService).requireRole(anyString(), anyString(), anyString(), anyString());
        when(businessProfileRepository.findById(businessId)).thenReturn(Optional.of(profile));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        BusinessProfileEntity result = profileService.businessProfileById(businessId);

        assertEquals("Owner Name", result.getFullName());
        assertEquals("Test Corp", result.getCompanyName());
    }

    @Test
    void businessProfileById_shouldRequireExpectedRoles() {
        Integer businessId = 1;
        BusinessProfileEntity profile = BusinessProfileEntity.builder()
                .businessId(businessId).accountId(10)
                .companyName("Test Corp").taxCode("123").kybStatus("Pending")
                .build();

        doNothing().when(accessService).requireRole(anyString(), anyString(), anyString(), anyString());
        when(businessProfileRepository.findById(businessId)).thenReturn(Optional.of(profile));
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(AccountEntity.builder().fullName("N").build()));

        profileService.businessProfileById(businessId);

        verify(accessService).requireRole("EXPERT", "BUSINESS", "STAFF", "ADMIN");
    }
}
