package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.entity.DomainEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.entity.SkillEntity;
import com.aitasker.be.entity.TechnologyEntity;
import com.aitasker.be.repository.BusinessProfileRepository;
import com.aitasker.be.repository.DomainRepository;
import com.aitasker.be.repository.JobDomainRepository;
import com.aitasker.be.repository.JobRepository;
import com.aitasker.be.repository.JobSkillRepository;
import com.aitasker.be.repository.JobTechnologyRepository;
import com.aitasker.be.repository.SkillRepository;
import com.aitasker.be.repository.TechnologyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {
    @Mock private AccessService accessService;
    @Mock private DomainRepository domainRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private TechnologyRepository technologyRepository;
    @Mock private JobRepository jobRepository;
    @Mock private BusinessProfileRepository businessProfileRepository;
    @Mock private JobDomainRepository jobDomainRepository;
    @Mock private JobSkillRepository jobSkillRepository;
    @Mock private JobTechnologyRepository jobTechnologyRepository;

    @InjectMocks private CatalogService catalogService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listDomains_shouldHideProfileReviewDomainForAnonymousUsers() {
        when(domainRepository.findAll()).thenReturn(List.of(marketplaceDomain(), profileReviewDomain()));

        List<DomainEntity> result = catalogService.listDomains(false);

        assertEquals(1, result.size());
        assertEquals("GENERATIVE_AI", result.get(0).getDomainCode());
    }

    @Test
    void listDomains_shouldShowProfileReviewDomainForAdminUsers() {
        authenticateAs("ADMIN");
        when(domainRepository.findByIsActiveTrueOrderBySortOrderAscDomainNameAsc())
                .thenReturn(List.of(marketplaceDomain(), profileReviewDomain()));

        List<DomainEntity> result = catalogService.listDomains(true);

        assertEquals(2, result.size());
    }

    @Test
    void replaceJobDomains_shouldRejectProfileReviewDomainForBusiness() {
        authenticateAs("BUSINESS");
        AccountEntity business = AccountEntity.builder()
                .accountId(10)
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .status("Approved")
                .build();
        when(jobRepository.findById(5)).thenReturn(Optional.of(JobEntity.builder().jobId(5).businessId(3).build()));
        when(accessService.currentAccount()).thenReturn(business);
        doNothing().when(accessService).requireApprovedAccount();
        when(businessProfileRepository.findByAccountId(10)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(3).build()));
        when(domainRepository.findById(99)).thenReturn(Optional.of(profileReviewDomain()));

        AppException ex = assertThrows(AppException.class, () -> catalogService.replaceJobDomains(5, List.of(99)));

        assertEquals("DOMAIN XET DUYET HO SO KHONG DUOC GAN CHO JOB", ex.getMessage());
        verify(jobDomainRepository, never()).deleteByIdJobId(5);
    }

    @Test
    void deleteDomain_shouldDeactivateDomain() {
        DomainEntity domain = marketplaceDomain();
        when(domainRepository.findById(2)).thenReturn(Optional.of(domain));
        when(domainRepository.save(any(DomainEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DomainEntity saved = catalogService.deleteDomain(2);

        assertEquals(Boolean.FALSE, saved.getIsActive());
        verify(accessService).requireRole("ADMIN");
    }

    @Test
    void deleteSkill_shouldDeactivateSkill() {
        SkillEntity skill = SkillEntity.builder()
                .skillId(5)
                .skillName("Prompt Engineering")
                .isActive(true)
                .build();
        when(skillRepository.findById(5)).thenReturn(Optional.of(skill));
        when(skillRepository.save(any(SkillEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SkillEntity saved = catalogService.deleteSkill(5);

        assertEquals(Boolean.FALSE, saved.getIsActive());
        verify(accessService).requireRole("ADMIN");
    }

    @Test
    void deleteTechnology_shouldDeactivateTechnology() {
        TechnologyEntity technology = TechnologyEntity.builder()
                .technologyId(7)
                .technologyName("Spring Boot")
                .isActive(true)
                .build();
        when(technologyRepository.findById(7)).thenReturn(Optional.of(technology));
        when(technologyRepository.save(any(TechnologyEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TechnologyEntity saved = catalogService.deleteTechnology(7);

        assertEquals(Boolean.FALSE, saved.getIsActive());
        verify(accessService).requireRole("ADMIN");
    }

    private void authenticateAs(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                role.toLowerCase() + "@aitasker.local",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        ));
    }

    private DomainEntity marketplaceDomain() {
        return DomainEntity.builder()
                .domainId(2)
                .domainCode("GENERATIVE_AI")
                .domainName("Generative AI")
                .isActive(true)
                .sortOrder(2)
                .build();
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
}
