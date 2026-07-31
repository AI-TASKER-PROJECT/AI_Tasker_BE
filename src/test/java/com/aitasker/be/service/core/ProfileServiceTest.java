/*
 * NOTE FILE: src/test/java/com/aitasker/be/service/core/ProfileServiceTest.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.dto.auth.TaxCheckResponse;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.entity.DomainEntity;
import com.aitasker.be.entity.ExpertProfileEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.PortfolioEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.entity.StaffDomainEntity;
import com.aitasker.be.entity.StaffDomainId;
import com.aitasker.be.entity.StaffEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.repository.AuditLogRepository;
import com.aitasker.be.repository.BusinessProfileRepository;
import com.aitasker.be.repository.DomainRepository;
import com.aitasker.be.repository.ExpertProfileRepository;
import com.aitasker.be.repository.JobRepository;
import com.aitasker.be.repository.PortfolioRepository;
import com.aitasker.be.repository.ReviewRepository;
import com.aitasker.be.repository.StaffDomainRepository;
import com.aitasker.be.repository.StaffRepository;
import com.aitasker.be.service.auth.TaxCheckService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
    @Mock private TaxCheckService taxCheckService;
    @Mock private JobRepository jobRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private DomainRepository domainRepository;
    @Mock private StaffDomainRepository staffDomainRepository;

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

    @Test
    void upsertBusiness_shouldNotifyOnlyProfileReviewStaffWhenVerificationSubmitted() {
        Integer accountId = 10;
        AccountEntity account = AccountEntity.builder()
                .accountId(accountId)
                .fullName("Nova Retail Owner")
                .status("Rejected")
                .build();
        TaxCheckResponse vietQrResponse = TaxCheckResponse.builder()
                .taxCode("0312345678")
                .companyName("Nova Retail")
                .address("TP HCM")
                .representative("Nguyen Van A")
                .status("FOUND")
                .build();
        BusinessProfileEntity input = BusinessProfileEntity.builder()
                .taxCode("0312345678")
                .businessLicenseUrl("licenses/nova.pdf")
                .build();
        BusinessProfileEntity savedProfile = BusinessProfileEntity.builder()
                .businessId(5)
                .accountId(accountId)
                .taxCode(input.getTaxCode())
                .companyName("Nova Retail")
                .address("TP HCM")
                .verifiedRepresentative("Nguyen Van A")
                .kybStatus("Pending")
                .build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.existsByTaxCodeExcludingAccount("0312345678", accountId)).thenReturn(false);
        when(taxCheckService.checkTaxCode("0312345678")).thenReturn(vietQrResponse);
        when(businessProfileRepository.findByAccountId(accountId)).thenReturn(Optional.empty());
        when(businessProfileRepository.save(any(BusinessProfileEntity.class))).thenReturn(savedProfile);
        when(domainRepository.findByDomainCode(CatalogService.PROFILE_REVIEW_DOMAIN_CODE))
                .thenReturn(Optional.of(profileReviewDomain()));
        when(staffDomainRepository.findByIdDomainId(99)).thenReturn(List.of(
                new StaffDomainEntity(new StaffDomainId(1, 99)),
                new StaffDomainEntity(new StaffDomainId(2, 99))
        ));
        when(staffRepository.findById(1)).thenReturn(Optional.of(StaffEntity.builder().staffId(1).accountId(40).build()));
        when(staffRepository.findById(2)).thenReturn(Optional.of(StaffEntity.builder().staffId(2).accountId(41).build()));

        BusinessProfileEntity result = profileService.upsertBusiness(input);

        assertEquals("Pending", account.getStatus());
        assertEquals(savedProfile, result);
        assertEquals("Nguyen Van A", result.getVerifiedRepresentative());
        verify(accountRepository).save(account);
        verify(notificationService).notifyProfileSubmitted(40, accountId, "BUSINESS", 5, "Nova Retail");
        verify(notificationService).notifyProfileSubmitted(41, accountId, "BUSINESS", 5, "Nova Retail");
    }

    @Test
    void upsertExpert_shouldNotifyOnlyProfileReviewStaffWhenVerificationSubmitted() {
        Integer accountId = 20;
        AccountEntity account = AccountEntity.builder()
                .accountId(accountId)
                .fullName("Tran Hoang Nam")
                .status("Pending")
                .build();
        ExpertProfileEntity input = ExpertProfileEntity.builder()
                .nationalId("079203001234")
                .portfolioUrl("https://portfolio.aitasker.local/tran-hoang-nam")
                .yearsOfExperience(5)
                .build();
        ExpertProfileEntity savedProfile = ExpertProfileEntity.builder()
                .expertId(6)
                .accountId(accountId)
                .nationalId(input.getNationalId())
                .portfolioUrl(input.getPortfolioUrl())
                .yearsOfExperience(input.getYearsOfExperience())
                .kycStatus("Pending")
                .build();

        when(accessService.currentAccount()).thenReturn(account);
        when(expertProfileRepository.findByNationalId(input.getNationalId())).thenReturn(Optional.empty());
        when(expertProfileRepository.findByAccountId(accountId)).thenReturn(Optional.empty());
        when(expertProfileRepository.save(any(ExpertProfileEntity.class))).thenReturn(savedProfile);
        when(domainRepository.findByDomainCode(CatalogService.PROFILE_REVIEW_DOMAIN_CODE))
                .thenReturn(Optional.of(profileReviewDomain()));
        when(staffDomainRepository.findByIdDomainId(99)).thenReturn(List.of(
                new StaffDomainEntity(new StaffDomainId(1, 99))
        ));
        when(staffRepository.findById(1)).thenReturn(Optional.of(StaffEntity.builder().staffId(1).accountId(40).build()));

        ExpertProfileEntity result = profileService.upsertExpert(input);

        assertEquals("Pending", account.getStatus());
        assertEquals(savedProfile, result);
        verify(accountRepository).save(account);
        verify(notificationService).notifyProfileSubmitted(40, accountId, "EXPERT", 6, "Tran Hoang Nam");
    }

    @Test
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
        when(businessProfileRepository.findById(businessId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> profileService.businessProfileById(businessId));
        assertEquals("KHONG TIM THAY BUSINESS PROFILE", ex.getMessage());
        verifyNoInteractions(accessService);
    }

    @Test
    void businessProfileById_shouldReturnProfileWithContactInfo() {
        Integer businessId = 1;
        Integer accountId = 10;
        BusinessProfileEntity profile = BusinessProfileEntity.builder()
                .businessId(businessId).accountId(accountId)
                .companyName("Test Corp").taxCode("1234567890")
                .kybStatus("Approved").build();
        AccountEntity account = AccountEntity.builder()
                .accountId(accountId).fullName("Owner Name").email("e@x.com").phone("090")
                .build();

        when(businessProfileRepository.findById(businessId)).thenReturn(Optional.of(profile));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(reviewRepository.averageRatingByRevieweeId(accountId)).thenReturn(new BigDecimal("4.5"));

        BusinessProfileEntity result = profileService.businessProfileById(businessId);

        assertBusinessContact(result, "Owner Name", "e@x.com", "090");
        assertEquals("Test Corp", result.getCompanyName());
        assertEquals(new BigDecimal("4.5"), result.getAverageRating());
        verifyNoInteractions(accessService);
    }

    @Test
    void currentBusinessProfile_shouldReturnOwnProfileWithContactInfo() {
        Integer accountId = 10;
        BusinessProfileEntity profile = BusinessProfileEntity.builder()
                .businessId(1).accountId(accountId)
                .companyName("My Corp").taxCode("1234567890")
                .kybStatus("Approved").build();
        AccountEntity account = AccountEntity.builder()
                .accountId(accountId).fullName("Business Owner").email("owner@x.com").phone("091")
                .build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        BusinessProfileEntity result = profileService.currentBusinessProfile();

        assertBusinessContact(result, "Business Owner", "owner@x.com", "091");
        verify(accessService).requireRole("BUSINESS");
    }

    @Test
    void businessProfileById_shouldNotRequireRoleForGuestAccess() {
        Integer businessId = 1;
        BusinessProfileEntity profile = BusinessProfileEntity.builder()
                .businessId(businessId).accountId(10)
                .companyName("Test Corp").taxCode("123").kybStatus("Pending")
                .build();

        when(businessProfileRepository.findById(businessId)).thenReturn(Optional.of(profile));
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(AccountEntity.builder().fullName("N").email("n@x.com").phone("090").build()));

        profileService.businessProfileById(businessId);

        verifyNoInteractions(accessService);
    }

    @Test
    void businessProfileByJob_shouldReturnProfileWhenJobOpen() {
        Integer jobId = 5;
        Integer businessId = 7;
        JobEntity job = JobEntity.builder().jobId(jobId).businessId(businessId).status("OPEN").build();
        BusinessProfileEntity profile = BusinessProfileEntity.builder()
                .businessId(businessId).accountId(10).companyName("Open Corp").taxCode("t1").kybStatus("Approved").build();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(businessProfileRepository.findById(businessId)).thenReturn(Optional.of(profile));
        when(accountRepository.findById(10)).thenReturn(Optional.of(AccountEntity.builder()
                .accountId(10).fullName("Open Owner").email("open@corp.com").phone("092").build()));

        BusinessProfileEntity result = profileService.businessProfileByJob(jobId);

        assertEquals(businessId, result.getBusinessId());
        assertBusinessContact(result, "Open Owner", "open@corp.com", "092");
        verifyNoInteractions(accessService);
    }

    @Test
    void businessProfileByJob_shouldRequireRoleWhenJobNotOpen() {
        Integer jobId = 6;
        Integer businessId = 8;
        JobEntity job = JobEntity.builder().jobId(jobId).businessId(businessId).status("IN_PROGRESS").build();
        BusinessProfileEntity profile = BusinessProfileEntity.builder()
                .businessId(businessId).accountId(11).companyName("Closed Corp").taxCode("t2").kybStatus("Approved").build();

        doNothing().when(accessService).requireRole(anyString(), anyString(), anyString());
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(businessProfileRepository.findById(businessId)).thenReturn(Optional.of(profile));
        when(accountRepository.findById(11)).thenReturn(Optional.of(AccountEntity.builder()
                .accountId(11).fullName("Closed Owner").email("closed@corp.com").phone("093").build()));

        BusinessProfileEntity result = profileService.businessProfileByJob(jobId);

        assertEquals(businessId, result.getBusinessId());
        assertBusinessContact(result, "Closed Owner", "closed@corp.com", "093");
        verify(accessService).requireRole("STAFF", "ADMIN", "BUSINESS");
    }

    @Test
    void allBusinessProfiles_shouldReturnListWithContactInfo() {
        BusinessProfileEntity first = BusinessProfileEntity.builder()
                .businessId(1).accountId(10).companyName("A").taxCode("t1").kybStatus("Approved").build();
        BusinessProfileEntity second = BusinessProfileEntity.builder()
                .businessId(2).accountId(11).companyName("B").taxCode("t2").kybStatus("Pending").build();

        when(accessService.currentAccount()).thenReturn(staffReviewerAccount());
        when(staffRepository.findByAccountId(99)).thenReturn(Optional.of(StaffEntity.builder().staffId(7).accountId(99).build()));
        when(domainRepository.findByDomainCode(CatalogService.PROFILE_REVIEW_DOMAIN_CODE)).thenReturn(Optional.of(profileReviewDomain()));
        when(staffDomainRepository.existsByIdStaffIdAndIdDomainId(7, 99)).thenReturn(true);
        when(businessProfileRepository.findAll()).thenReturn(List.of(first, second));
        when(accountRepository.findById(10)).thenReturn(Optional.of(AccountEntity.builder()
                .accountId(10).fullName("Owner A").email("a@corp.com").phone("090").build()));
        when(accountRepository.findById(11)).thenReturn(Optional.of(AccountEntity.builder()
                .accountId(11).fullName("Owner B").email("b@corp.com").phone("091").build()));

        List<BusinessProfileEntity> result = profileService.allBusinessProfiles();

        assertEquals(2, result.size());
        assertBusinessContact(result.get(0), "Owner A", "a@corp.com", "090");
        assertBusinessContact(result.get(1), "Owner B", "b@corp.com", "091");
        verify(accessService).requireRole("STAFF");
    }

    @Test
    void allBusinessProfiles_shouldRejectStaffWithoutProfileReviewDomain() {
        when(accessService.currentAccount()).thenReturn(staffReviewerAccount());
        when(staffRepository.findByAccountId(99)).thenReturn(Optional.of(StaffEntity.builder().staffId(7).accountId(99).build()));
        when(domainRepository.findByDomainCode(CatalogService.PROFILE_REVIEW_DOMAIN_CODE)).thenReturn(Optional.of(profileReviewDomain()));
        when(staffDomainRepository.existsByIdStaffIdAndIdDomainId(7, 99)).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> profileService.allBusinessProfiles());

        assertEquals("STAFF KHONG CO QUYEN XET DUYET HO SO", ex.getMessage());
        verify(businessProfileRepository, never()).findAll();
    }

    @Test
    void allPortfolios_shouldRejectStaffWithoutProfileReviewDomain() {
        when(accessService.currentAccount()).thenReturn(staffReviewerAccount());
        when(staffRepository.findByAccountId(99)).thenReturn(Optional.of(StaffEntity.builder().staffId(7).accountId(99).build()));
        when(domainRepository.findByDomainCode(CatalogService.PROFILE_REVIEW_DOMAIN_CODE)).thenReturn(Optional.of(profileReviewDomain()));
        when(staffDomainRepository.existsByIdStaffIdAndIdDomainId(7, 99)).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> profileService.allPortfolios());

        assertEquals("STAFF KHONG CO QUYEN XET DUYET HO SO", ex.getMessage());
        verify(portfolioRepository, never()).findAll();
    }

    @Test
    void allPortfolios_shouldAllowBusinessWithoutProfileReviewDomain() {
        when(accessService.currentAccount()).thenReturn(AccountEntity.builder()
                .accountId(12)
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .build());
        when(portfolioRepository.findAll()).thenReturn(List.of(PortfolioEntity.builder().portfolioId(1).build()));

        List<PortfolioEntity> result = profileService.allPortfolios();

        assertEquals(1, result.size());
        verifyNoInteractions(domainRepository, staffDomainRepository);
    }

    @Test
    void businessProfileByJob_shouldThrow404WhenJobNotFound() {
        Integer jobId = 999;
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> profileService.businessProfileByJob(jobId));
        assertEquals("KHONG TIM THAY JOB", ex.getMessage());
        verifyNoInteractions(accessService);
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
    void expertProfileById_shouldReturnProfileWithContactInfo() {
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
        when(reviewRepository.averageRatingByRevieweeId(accountId)).thenReturn(new BigDecimal("4.8"));

        ExpertProfileEntity result = profileService.expertProfileById(expertId);

        assertExpertContact(result, "Expert Name", "expert@test.com", "091");
        assertEquals(5, result.getYearsOfExperience());
        assertEquals(new BigDecimal("4.8"), result.getAverageRating());
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
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(AccountEntity.builder().fullName("N").email("n@x.com").phone("090").build()));

        profileService.expertProfileById(expertId);

        verify(accessService).requireRole("EXPERT", "BUSINESS", "STAFF", "ADMIN");
    }

    @Test
    void currentExpertProfile_shouldReturnOwnProfileWithEmail() {
        Integer accountId = 20;
        AccountEntity account = AccountEntity.builder()
                .accountId(accountId).fullName("Expert Self").email("self@expert.com").phone("094")
                .build();
        ExpertProfileEntity profile = ExpertProfileEntity.builder()
                .expertId(2).accountId(accountId).nationalId("0123456789")
                .portfolioUrl("https://portfolio.example.com")
                .yearsOfExperience(4).kycStatus("Approved").build();

        when(accessService.currentAccount()).thenReturn(account);
        when(expertProfileRepository.findByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        ExpertProfileEntity result = profileService.currentExpertProfile();

        assertExpertContact(result, "Expert Self", "self@expert.com", "094");
        verify(accessService).requireRole("EXPERT");
    }

    @Test
    void allExpertProfiles_shouldReturnListWithEmail() {
        ExpertProfileEntity first = ExpertProfileEntity.builder()
                .expertId(1).accountId(20).nationalId("n1").portfolioUrl("p1")
                .yearsOfExperience(3).kycStatus("Approved").build();
        ExpertProfileEntity second = ExpertProfileEntity.builder()
                .expertId(2).accountId(21).nationalId("n2").portfolioUrl("p2")
                .yearsOfExperience(5).kycStatus("Pending").build();

        when(accessService.currentAccount()).thenReturn(AccountEntity.builder()
                .accountId(12)
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .build());
        when(expertProfileRepository.findAll()).thenReturn(List.of(first, second));
        when(accountRepository.findById(20)).thenReturn(Optional.of(AccountEntity.builder()
                .accountId(20).fullName("Expert A").email("a@expert.com").phone("095").build()));
        when(accountRepository.findById(21)).thenReturn(Optional.of(AccountEntity.builder()
                .accountId(21).fullName("Expert B").email("b@expert.com").phone("096").build()));

        List<ExpertProfileEntity> result = profileService.allExpertProfiles();

        assertEquals(2, result.size());
        assertExpertContact(result.get(0), "Expert A", "a@expert.com", "095");
        assertExpertContact(result.get(1), "Expert B", "b@expert.com", "096");
        verify(accessService).requireRole("STAFF", "BUSINESS");
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
    void approveProfile_shouldRejectStaffWithoutProfileReviewDomain() {
        Integer staffAccountId = 99;
        Integer staffId = 7;

        when(accessService.currentAccount()).thenReturn(AccountEntity.builder().accountId(staffAccountId).build());
        when(staffRepository.findByAccountId(staffAccountId)).thenReturn(Optional.of(StaffEntity.builder().staffId(staffId).accountId(staffAccountId).build()));
        when(domainRepository.findByDomainCode(CatalogService.PROFILE_REVIEW_DOMAIN_CODE)).thenReturn(Optional.of(profileReviewDomain()));
        when(staffDomainRepository.existsByIdStaffIdAndIdDomainId(staffId, 99)).thenReturn(false);

        AppException ex = assertThrows(AppException.class,
                () -> profileService.approveProfile("BUSINESS", 1, "Approved", null));

        assertEquals("STAFF KHONG CO QUYEN XET DUYET HO SO", ex.getMessage());
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
        when(domainRepository.findByDomainCode(CatalogService.PROFILE_REVIEW_DOMAIN_CODE)).thenReturn(Optional.of(profileReviewDomain()));
        when(staffDomainRepository.existsByIdStaffIdAndIdDomainId(staffId, 99)).thenReturn(true);
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
                .kybStatus("Pending")
                .rejectionReason("Old reason")
                .build();

        when(accessService.currentAccount()).thenReturn(AccountEntity.builder().accountId(staffAccountId).build());
        when(staffRepository.findByAccountId(staffAccountId)).thenReturn(Optional.of(StaffEntity.builder().staffId(staffId).accountId(staffAccountId).build()));
        when(domainRepository.findByDomainCode(CatalogService.PROFILE_REVIEW_DOMAIN_CODE)).thenReturn(Optional.of(profileReviewDomain()));
        when(staffDomainRepository.existsByIdStaffIdAndIdDomainId(staffId, 99)).thenReturn(true);
        when(businessProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(accountRepository.findById(profileAccountId)).thenReturn(Optional.of(AccountEntity.builder().accountId(profileAccountId).status("Pending").build()));
        when(businessProfileRepository.save(any(BusinessProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BusinessProfileEntity saved = (BusinessProfileEntity) profileService.approveProfile("BUSINESS", profileId, "Approved", null);

        assertEquals("Approved", saved.getKybStatus());
        assertEquals(staffId, saved.getApprovedBy());
        assertNull(saved.getRejectionReason());
        verify(accountRepository).save(argThat(account -> "Approved".equals(account.getStatus())));
        verify(notificationService).notifyProfileReviewed(profileAccountId, staffAccountId, "BUSINESS", "Approved");
    }

    @Test
    void upsertBusiness_shouldThrowWhenTaxCodeFormatInvalid() {
        BusinessProfileEntity input = BusinessProfileEntity.builder().taxCode("12345").build();
        AppException ex = assertThrows(AppException.class, () -> profileService.upsertBusiness(input));
        assertEquals("MA SO THUE KHONG HOP LE", ex.getMessage());
        verify(businessProfileRepository, never()).existsByTaxCodeExcludingAccount(anyString(), anyInt());
        verifyNoInteractions(taxCheckService);
    }

    @Test
    void upsertBusiness_shouldThrowWhenTaxCodeDuplicate() {
        Integer accountId = 10;
        AccountEntity account = AccountEntity.builder().accountId(accountId).fullName("A").build();
        BusinessProfileEntity input = BusinessProfileEntity.builder().taxCode("0312345678").build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.existsByTaxCodeExcludingAccount("0312345678", accountId)).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> profileService.upsertBusiness(input));
        assertEquals("MA SO THUE DA DUOC SU DUNG BOI TAI KHOAN KHAC", ex.getMessage());
        verifyNoInteractions(taxCheckService);
    }

    @Test
    void upsertBusiness_shouldThrowWhenTaxCodeNotFoundInVietQR() {
        Integer accountId = 10;
        AccountEntity account = AccountEntity.builder().accountId(accountId).fullName("A").build();
        BusinessProfileEntity input = BusinessProfileEntity.builder().taxCode("0312345678").build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.existsByTaxCodeExcludingAccount("0312345678", accountId)).thenReturn(false);
        when(taxCheckService.checkTaxCode("0312345678")).thenThrow(new NotFoundException("KHONG TIM THAY DOANH NGHIEP VOI MA SO THUE: 0312345678"));

        NotFoundException ex = assertThrows(NotFoundException.class, () -> profileService.upsertBusiness(input));
        assertEquals("KHONG TIM THAY DOANH NGHIEP VOI MA SO THUE: 0312345678", ex.getMessage());
        verify(businessProfileRepository, never()).save(any());
    }

    @Test
    void upsertBusiness_shouldAutoFillCompanyInfoFromVietQR() {
        Integer accountId = 10;
        AccountEntity account = AccountEntity.builder().accountId(accountId).fullName("Business Owner").status("Pending").build();
        TaxCheckResponse vietQr = TaxCheckResponse.builder()
                .taxCode("0312345678")
                .companyName("CONG TY TNHH ABC")
                .address("123 Nguyen Hue, Q1, TPHCM")
                .representative("Nguyen Van B")
                .status("FOUND")
                .build();
        BusinessProfileEntity input = BusinessProfileEntity.builder()
                .taxCode("0312345678")
                .businessLicenseUrl("licenses/abc.pdf")
                .build();
        BusinessProfileEntity savedProfile = BusinessProfileEntity.builder()
                .businessId(5)
                .accountId(accountId)
                .taxCode("0312345678")
                .companyName("CONG TY TNHH ABC")
                .address("123 Nguyen Hue, Q1, TPHCM")
                .verifiedRepresentative("Nguyen Van B")
                .businessLicenseUrl("licenses/abc.pdf")
                .kybStatus("Pending")
                .build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.existsByTaxCodeExcludingAccount("0312345678", accountId)).thenReturn(false);
        when(taxCheckService.checkTaxCode("0312345678")).thenReturn(vietQr);
        when(businessProfileRepository.findByAccountId(accountId)).thenReturn(Optional.empty());
        when(businessProfileRepository.save(any(BusinessProfileEntity.class))).thenReturn(savedProfile);

        BusinessProfileEntity result = profileService.upsertBusiness(input);

        assertEquals("CONG TY TNHH ABC", result.getCompanyName());
        assertEquals("123 Nguyen Hue, Q1, TPHCM", result.getAddress());
        assertEquals("Nguyen Van B", result.getVerifiedRepresentative());
        assertEquals("Pending", result.getKybStatus());
        verify(accountRepository).save(account);
    }

    @Test
    void upsertBusiness_shouldAllowResubmitSameAccount() {
        Integer accountId = 10;
        AccountEntity account = AccountEntity.builder().accountId(accountId).fullName("Owner").status("Rejected").build();
        TaxCheckResponse vietQr = TaxCheckResponse.builder()
                .taxCode("0312345678")
                .companyName("My Company")
                .address("My Address")
                .representative("Rep")
                .status("FOUND")
                .build();
        BusinessProfileEntity input = BusinessProfileEntity.builder()
                .taxCode("0312345678")
                .businessLicenseUrl("licenses/new.pdf")
                .build();
        BusinessProfileEntity existingProfile = BusinessProfileEntity.builder()
                .businessId(5)
                .accountId(accountId)
                .taxCode("0312345678")
                .companyName("Old Name")
                .kybStatus("Rejected")
                .build();
        BusinessProfileEntity savedProfile = BusinessProfileEntity.builder()
                .businessId(5)
                .accountId(accountId)
                .taxCode("0312345678")
                .companyName("My Company")
                .address("My Address")
                .verifiedRepresentative("Rep")
                .businessLicenseUrl("licenses/new.pdf")
                .kybStatus("Pending")
                .build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.existsByTaxCodeExcludingAccount("0312345678", accountId)).thenReturn(false);
        when(taxCheckService.checkTaxCode("0312345678")).thenReturn(vietQr);
        when(businessProfileRepository.findByAccountId(accountId)).thenReturn(Optional.of(existingProfile));
        when(businessProfileRepository.save(any(BusinessProfileEntity.class))).thenReturn(savedProfile);

        BusinessProfileEntity result = profileService.upsertBusiness(input);

        assertEquals("My Company", result.getCompanyName());
        assertEquals("Pending", result.getKybStatus());
        assertEquals("Rep", result.getVerifiedRepresentative());
        verify(businessProfileRepository).existsByTaxCodeExcludingAccount("0312345678", accountId);
        verify(taxCheckService).checkTaxCode("0312345678");
    }

    @Test
    void upsertBusiness_shouldRejectResubmitWhilePending() {
        Integer accountId = 10;
        AccountEntity account = AccountEntity.builder().accountId(accountId).fullName("Owner").status("Pending").build();
        BusinessProfileEntity input = BusinessProfileEntity.builder()
                .taxCode("0312345678")
                .businessLicenseUrl("licenses/new.pdf")
                .build();
        BusinessProfileEntity existingProfile = BusinessProfileEntity.builder()
                .businessId(5)
                .accountId(accountId)
                .taxCode("0312345678")
                .companyName("Pending Company")
                .kybStatus("Pending")
                .build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.findByAccountId(accountId)).thenReturn(Optional.of(existingProfile));

        AppException ex = assertThrows(AppException.class, () -> profileService.upsertBusiness(input));

        assertEquals("HO SO DANG CHO DUYET, VUI LONG DOI KET QUA XET DUYET", ex.getMessage());
        verify(businessProfileRepository, never()).existsByTaxCodeExcludingAccount(anyString(), anyInt());
        verifyNoInteractions(taxCheckService);
        verify(businessProfileRepository, never()).save(any());
    }

    @Test
    void upsertBusiness_shouldUpdateApprovedProfileWithoutReopeningReview() {
        Integer accountId = 10;
        AccountEntity account = AccountEntity.builder().accountId(accountId).fullName("Owner").status("Approved").build();
        TaxCheckResponse vietQr = TaxCheckResponse.builder()
                .taxCode("0312345678")
                .companyName("Approved Company")
                .address("New Address")
                .representative("Rep")
                .status("FOUND")
                .build();
        BusinessProfileEntity input = BusinessProfileEntity.builder()
                .taxCode("0312345678")
                .businessLicenseUrl("licenses/updated.pdf")
                .build();
        BusinessProfileEntity existingProfile = BusinessProfileEntity.builder()
                .businessId(5)
                .accountId(accountId)
                .taxCode("0312345678")
                .companyName("Old Company")
                .kybStatus("Approved")
                .approvedBy(7)
                .build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.findByAccountId(accountId)).thenReturn(Optional.of(existingProfile));
        when(businessProfileRepository.existsByTaxCodeExcludingAccount("0312345678", accountId)).thenReturn(false);
        when(taxCheckService.checkTaxCode("0312345678")).thenReturn(vietQr);
        when(businessProfileRepository.save(any(BusinessProfileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BusinessProfileEntity result = profileService.upsertBusiness(input);

        assertEquals("Approved", result.getKybStatus());
        assertEquals(Integer.valueOf(7), result.getApprovedBy());
        assertEquals("Approved Company", result.getCompanyName());
        assertEquals("licenses/updated.pdf", result.getBusinessLicenseUrl());
        verify(accountRepository, never()).save(any());
        verify(notificationService, never()).notifyProfileSubmitted(anyInt(), anyInt(), anyString(), anyInt(), anyString());
    }

    @Test
    void approveProfile_shouldRejectAlreadyApprovedBusinessProfile() {
        Integer profileId = 1;
        Integer staffAccountId = 99;
        Integer staffId = 7;
        BusinessProfileEntity profile = BusinessProfileEntity.builder()
                .businessId(profileId)
                .accountId(10)
                .taxCode("0312345678")
                .companyName("Nova Retail")
                .kybStatus("Approved")
                .build();

        when(accessService.currentAccount()).thenReturn(AccountEntity.builder().accountId(staffAccountId).build());
        when(staffRepository.findByAccountId(staffAccountId)).thenReturn(Optional.of(StaffEntity.builder().staffId(staffId).accountId(staffAccountId).build()));
        when(domainRepository.findByDomainCode(CatalogService.PROFILE_REVIEW_DOMAIN_CODE)).thenReturn(Optional.of(profileReviewDomain()));
        when(staffDomainRepository.existsByIdStaffIdAndIdDomainId(staffId, 99)).thenReturn(true);
        when(businessProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));

        AppException ex = assertThrows(AppException.class,
                () -> profileService.approveProfile("BUSINESS", profileId, "Rejected", "reason"));

        assertEquals("CHI DUOC XET DUYET HO SO DANG CHO DUYET", ex.getMessage());
        verify(accountRepository, never()).save(any());
        verify(businessProfileRepository, never()).save(any());
    }

    private void assertBusinessContact(BusinessProfileEntity result, String fullName, String email, String phone) {
        assertEquals(fullName, result.getFullName());
        assertEquals(email, result.getEmail());
        assertEquals(phone, result.getPhone());
    }

    private void assertExpertContact(ExpertProfileEntity result, String fullName, String email, String phone) {
        assertEquals(fullName, result.getFullName());
        assertEquals(email, result.getEmail());
        assertEquals(phone, result.getPhone());
        assertNotNull(result.getTitle());
    }

    private DomainEntity profileReviewDomain() {
        return DomainEntity.builder()
                .domainId(99)
                .domainCode(CatalogService.PROFILE_REVIEW_DOMAIN_CODE)
                .domainName("Xet duyet ho so")
                .isActive(true)
                .sortOrder(999)
                .build();
    }

    private AccountEntity staffReviewerAccount() {
        return AccountEntity.builder()
                .accountId(99)
                .role(RoleEntity.builder().roleName("STAFF").build())
                .build();
    }
}
