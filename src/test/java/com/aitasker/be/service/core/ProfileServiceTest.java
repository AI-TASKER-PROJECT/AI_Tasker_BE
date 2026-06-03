package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.repository.AuditLogRepository;
import com.aitasker.be.repository.BusinessProfileRepository;
import com.aitasker.be.repository.ExpertProfileRepository;
import com.aitasker.be.repository.PortfolioRepository;
import com.aitasker.be.repository.StaffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock private AccessService accessService;
    @Mock private BusinessProfileRepository businessProfileRepository;
    @Mock private ExpertProfileRepository expertProfileRepository;
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private AuditLogRepository auditLogRepository;

    @InjectMocks private ProfileService profileService;

    @Test
    void upsertBusiness_shouldThrowWhenTaxCodeBlank() {
        BusinessProfileEntity input = BusinessProfileEntity.builder().taxCode(" ").companyName("ABC").build();
        AppException ex = assertThrows(AppException.class, () -> profileService.upsertBusiness(input));
        assertEquals("TAX CODE KHONG DUOC DE TRONG", ex.getMessage());
    }

    @Test
    void upsertPortfolio_shouldThrowWhenContextBlank() {
        AppException ex = assertThrows(AppException.class, () -> profileService.upsertPortfolio(com.aitasker.be.entity.PortfolioEntity.builder()
                .context(" ")
                .dataProcessing("DP")
                .modelArchitecture("MA")
                .performanceMetrics("PM")
                .build()));
        assertEquals("CONTEXT KHONG DUOC DE TRONG", ex.getMessage());
    }
}
