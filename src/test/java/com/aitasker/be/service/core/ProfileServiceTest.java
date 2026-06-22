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
import com.aitasker.be.entity.ExpertProfileEntity;
import com.aitasker.be.entity.StaffEntity;
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
import org.springframework.web.multipart.MultipartFile;

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
    @Mock private AuditLogService auditLogService;
    @Mock private FirebaseStorageService firebaseStorageService;
    @Mock private NotificationService notificationService;

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

    @Test
    void expertProfileById_shouldThrowWhenProfileNotFound() {
        Integer expertId = 999;
        doNothing().when(accessService).requireRole(anyString(), anyString(), anyString(), anyString());
        when(expertProfileRepository.findById(expertId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> profileService.expertProfileById(expertId));
        assertEquals("KHONG TIM THAY EXPERT PROFILE", ex.getMessage());
        verify(accessService).requireRole("EXPERT", "BUSINESS", "STAFF", "ADMIN");
    }

    @Test
    void expertProfileById_shouldReturnProfileWithPublicAccountInfoOnly() {
        Integer expertId = 2;
        Integer accountId = 20;
        ExpertProfileEntity profile = ExpertProfileEntity.builder()
                .expertId(expertId).accountId(accountId)
                .nationalId("0123456789").portfolioUrl("https://portfolio.example.com")
                .yearsOfExperience(5).kycStatus("Approved").build();
        AccountEntity account = AccountEntity.builder()
                .accountId(accountId).fullName("Expert Name").email("expert@test.com").phone("091")
                .build();

        doNothing().when(accessService).requireRole(anyString(), anyString(), anyString(), anyString());
        when(expertProfileRepository.findById(expertId)).thenReturn(Optional.of(profile));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        ExpertProfileEntity result = profileService.expertProfileById(expertId);

        assertEquals("Expert Name", result.getFullName());
        assertEquals("Chuyên gia AI", result.getTitle());
        assertNull(result.getPhone());
        assertEquals(5, result.getYearsOfExperience());
    }

    @Test
    void expertProfileById_shouldRequireExpectedRoles() {
        Integer expertId = 2;
        ExpertProfileEntity profile = ExpertProfileEntity.builder()
                .expertId(expertId).accountId(20)
                .nationalId("0123456789").portfolioUrl("https://portfolio.example.com")
                .yearsOfExperience(5).kycStatus("Pending").build();

        doNothing().when(accessService).requireRole(anyString(), anyString(), anyString(), anyString());
        when(expertProfileRepository.findById(expertId)).thenReturn(Optional.of(profile));
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(AccountEntity.builder().fullName("N").build()));

        profileService.expertProfileById(expertId);

        verify(accessService).requireRole("EXPERT", "BUSINESS", "STAFF", "ADMIN");
    }

    @Test
    void uploadExpertPortfolio_shouldUploadToExpertPortfolioFolderAndAuditProfile() {
        Integer accountId = 20;
        Integer expertId = 2;
        MultipartFile file = mock(MultipartFile.class);
        String expectedPath = "expert-portfolios/accounts/20/portfolio.pdf";
        ExpertProfileEntity profile = ExpertProfileEntity.builder()
                .expertId(expertId)
                .accountId(accountId)
                .nationalId("0123456789")
                .portfolioUrl("https://portfolio.example.com")
                .yearsOfExperience(5)
                .kycStatus("Approved")
                .build();

        when(accessService.currentAccount()).thenReturn(AccountEntity.builder().accountId(accountId).build());
        when(firebaseStorageService.upload(file, "expert-portfolios/accounts/" + accountId)).thenReturn(expectedPath);
        when(expertProfileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));

        String path = profileService.uploadExpertPortfolio(file);

        assertEquals(expectedPath, path);
        verify(accessService).requireRole("EXPERT");
        verify(auditLogService).record(AuditLogService.ACTION_UPLOAD_EXPERT_PORTFOLIO_FILE, "expert_profiles", String.valueOf(expertId), accountId);
    }

    @Test
    void approveProfile_shouldRequireReasonWhenRejected() {
        AppException ex = assertThrows(AppException.class,
                () -> profileService.approveProfile("BUSINESS", 1, "Rejected", " "));

        assertEquals("LY DO TU CHOI KHONG DUOC DE TRONG", ex.getMessage());
        verify(accessService).requireRole("STAFF");
        verifyNoInteractions(businessProfileRepository);
    }

    @Test
    void approveProfile_shouldStoreTrimmedBusinessRejectionReason() {
        Integer profileId = 1;
        Integer profileAccountId = 10;
        Integer staffAccountId = 99;
        Integer staffId = 7;
        BusinessProfileEntity profile = BusinessProfileEntity.builder()
                .businessId(profileId)
                .accountId(profileAccountId)
                .taxCode("0312345678")
                .companyName("Nova Retail")
                .kybStatus("Pending")
                .build();

        when(accessService.currentAccount()).thenReturn(AccountEntity.builder().accountId(staffAccountId).build());
        when(staffRepository.findByAccountId(staffAccountId)).thenReturn(Optional.of(StaffEntity.builder().staffId(staffId).accountId(staffAccountId).build()));
        when(businessProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(accountRepository.findById(profileAccountId)).thenReturn(Optional.of(AccountEntity.builder().accountId(profileAccountId).status("Pending").build()));
        when(businessProfileRepository.save(any(BusinessProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BusinessProfileEntity saved = (BusinessProfileEntity) profileService.approveProfile("BUSINESS", profileId, "Rejected", "  Giay phep khong hop le  ");

        assertEquals("Rejected", saved.getKybStatus());
        assertEquals(staffId, saved.getApprovedBy());
        assertEquals("Giay phep khong hop le", saved.getRejectionReason());
        verify(accountRepository).save(argThat(account -> "Rejected".equals(account.getStatus())));
        verify(notificationService).notifyProfileReviewed(profileAccountId, staffAccountId, "BUSINESS", "Rejected");
    }

    @Test
    void approveProfile_shouldClearBusinessRejectionReasonWhenApproved() {
        Integer profileId = 1;
        Integer profileAccountId = 10;
        Integer staffAccountId = 99;
        Integer staffId = 7;
        BusinessProfileEntity profile = BusinessProfileEntity.builder()
                .businessId(profileId)
                .accountId(profileAccountId)
                .taxCode("0312345678")
                .companyName("Nova Retail")
                .kybStatus("Rejected")
                .rejectionReason("Old reason")
                .build();

        when(accessService.currentAccount()).thenReturn(AccountEntity.builder().accountId(staffAccountId).build());
        when(staffRepository.findByAccountId(staffAccountId)).thenReturn(Optional.of(StaffEntity.builder().staffId(staffId).accountId(staffAccountId).build()));
        when(businessProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(accountRepository.findById(profileAccountId)).thenReturn(Optional.of(AccountEntity.builder().accountId(profileAccountId).status("Rejected").build()));
        when(businessProfileRepository.save(any(BusinessProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BusinessProfileEntity saved = (BusinessProfileEntity) profileService.approveProfile("BUSINESS", profileId, "Approved", null);

        assertEquals("Approved", saved.getKybStatus());
        assertEquals(staffId, saved.getApprovedBy());
        assertNull(saved.getRejectionReason());
        verify(accountRepository).save(argThat(account -> "Approved".equals(account.getStatus())));
        verify(notificationService).notifyProfileReviewed(profileAccountId, staffAccountId, "BUSINESS", "Approved");
    }
}
