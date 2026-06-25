/*
 * NOTE FILE: src/test/java/com/aitasker/be/service/core/AuditLogServiceTest.java
 * Day la file gi: Unit test cho mapper audit log admin.
 * Muc dich note: bao ve cach hien thi action/object audit log sach, khong lo path/id ky thuat tren bang admin.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.dto.admin.AuditLogResponse;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {
    @Mock private AccessService accessService;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private BusinessProfileRepository businessProfileRepository;
    @Mock private ExpertProfileRepository expertProfileRepository;
    @Mock private JobRepository jobRepository;
    @Mock private ProposalRepository proposalRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private DisputeRepository disputeRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private AcceptanceCriteriaRepository acceptanceCriteriaRepository;
    @Mock private ContractDepositRepository contractDepositRepository;
    @Mock private MembershipPurchaseRepository membershipPurchaseRepository;
    @Mock private MembershipPackageRepository membershipPackageRepository;
    @Mock private PaymentOrderRepository paymentOrderRepository;
    @Mock private QuotaUsageLogRepository quotaUsageLogRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private WithdrawalRequestRepository withdrawalRequestRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private TechnologyRepository technologyRepository;

    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogService(
                accessService,
                auditLogRepository,
                accountRepository,
                businessProfileRepository,
                expertProfileRepository,
                jobRepository,
                proposalRepository,
                contractRepository,
                milestoneRepository,
                disputeRepository,
                transactionRepository,
                staffRepository,
                portfolioRepository,
                reviewRepository,
                acceptanceCriteriaRepository,
                contractDepositRepository,
                membershipPurchaseRepository,
                membershipPackageRepository,
                paymentOrderRepository,
                quotaUsageLogRepository,
                walletTransactionRepository,
                withdrawalRequestRepository,
                notificationRepository,
                technologyRepository
        );
    }

    @Test
    void listForAdmin_shouldRenderBusinessProfileWithoutRawId() {
        AccountEntity business = account(10, "Nova Retail", "business@aitasker.local", "BUSINESS");
        when(accountRepository.findById(10)).thenReturn(Optional.of(business));
        doReturn(Optional.of(BusinessProfileEntity.builder()
                .businessId(1)
                .accountId(10)
                .companyName("Nova Retail")
                .build())).when(businessProfileRepository).findById(any());
        when(auditLogRepository.findTop200ByOrderByCreatedAtDesc()).thenReturn(List.of(log(
                "Cập nhật hồ sơ doanh nghiệp",
                "business_profiles",
                "1",
                10
        )));

        AuditLogResponse response = auditLogService.listForAdmin(null).get(0);

        verify(accessService).requireRole("ADMIN");
        assertEquals("Cập nhật hồ sơ doanh nghiệp", response.getAction());
        assertEquals("Hồ sơ doanh nghiệp của Nova Retail", response.getEntityDisplayName());
        assertEquals("Hồ sơ doanh nghiệp", response.getEntityName());
        assertNull(response.getEntityId());
        assertEquals("business_profiles", response.getRawEntityName());
        assertEquals("1", response.getRawEntityId());
    }

    @Test
    void listForAdmin_shouldRenderExpertRecommendationPathAsBusinessEvent() {
        AccountEntity business = account(10, "Nova Retail", "business@aitasker.local", "BUSINESS");
        when(accountRepository.findById(10)).thenReturn(Optional.of(business));
        doReturn(Optional.of(JobEntity.builder()
                .jobId(1)
                .businessId(1)
                .title("Nâng cấp CRM AI")
                .budget(new BigDecimal("1000"))
                .status("OPEN")
                .build())).when(jobRepository).findById(any());
        doReturn(Optional.of(BusinessProfileEntity.builder()
                .businessId(1)
                .accountId(10)
                .companyName("Nova Retail")
                .build())).when(businessProfileRepository).findById(any());
        when(auditLogRepository.findTop200ByOrderByCreatedAtDesc()).thenReturn(List.of(log(
                "Tạo hoặc gửi dữ liệu: /api/jobs/1/expert-recommendations",
                "/api/jobs/1/expert-recommendations",
                "1",
                10
        )));

        AuditLogResponse response = auditLogService.listForAdmin(null).get(0);

        assertEquals("Tạo danh sách chuyên gia được AI gợi ý", response.getAction());
        assertEquals("Danh sách chuyên gia gợi ý cho job Nâng cấp CRM AI", response.getEntityDisplayName());
        assertEquals("Danh sách chuyên gia gợi ý", response.getEntityName());
        assertNull(response.getEntityId());
        assertEquals("/api/jobs/1/expert-recommendations", response.getRawEntityName());
        assertEquals("1", response.getRawEntityId());
    }

    @Test
    void listForAdmin_shouldRenderProposalWithExpertAndJobNames() {
        AccountEntity expert = account(20, "Nguyễn Văn A", "expert@aitasker.local", "EXPERT");
        when(accountRepository.findById(20)).thenReturn(Optional.of(expert));
        when(accountRepository.findById(30)).thenReturn(Optional.of(expert));
        doReturn(Optional.of(ExpertProfileEntity.builder()
                .expertId(2)
                .accountId(30)
                .build())).when(expertProfileRepository).findById(any());
        doReturn(Optional.of(JobEntity.builder()
                .jobId(1)
                .businessId(1)
                .title("Nâng cấp CRM AI")
                .budget(new BigDecimal("1000"))
                .status("OPEN")
                .build())).when(jobRepository).findById(any());
        doReturn(Optional.of(ProposalEntity.builder()
                .proposalId(7)
                .jobId(1)
                .expertId(2)
                .technicalSolution("Solution")
                .bidAmount(new BigDecimal("900"))
                .status("Accepted")
                .build())).when(proposalRepository).findById(any());
        when(auditLogRepository.findTop200ByOrderByCreatedAtDesc()).thenReturn(List.of(log(
                "Gửi proposal",
                "proposals",
                "7",
                20
        )));

        AuditLogResponse response = auditLogService.listForAdmin(null).get(0);

        assertEquals("Gửi proposal", response.getAction());
        assertEquals("Proposal của Nguyễn Văn A cho job Nâng cấp CRM AI", response.getEntityDisplayName());
        assertEquals("Proposal", response.getEntityName());
        assertNull(response.getEntityId());
    }

    @Test
    void listForAdmin_shouldNormalizeLegacyMembershipActionAndDisplayPackageOwner() {
        AccountEntity business = account(10, "Nova Retail", "business@aitasker.local", "BUSINESS");
        when(accountRepository.findById(10)).thenReturn(Optional.of(business));
        doReturn(Optional.of(MembershipPurchaseEntity.builder()
                .purchaseId(7L)
                .accountId(10)
                .packageId(2L)
                .amount(new BigDecimal("500"))
                .status("POSTED")
                .build())).when(membershipPurchaseRepository).findById(any());
        doReturn(Optional.of(MembershipPackageEntity.builder()
                .packageId(2L)
                .packageName("Premium")
                .build())).when(membershipPackageRepository).findById(any());
        when(auditLogRepository.findTop200ByOrderByCreatedAtDesc()).thenReturn(List.of(log(
                "Mua goi thanh vien",
                "membership_purchases",
                "7",
                10
        )));

        AuditLogResponse response = auditLogService.listForAdmin(null).get(0);

        assertEquals("Mua gói thành viên", response.getAction());
        assertEquals("Gói Premium của Nova Retail", response.getEntityDisplayName());
        assertEquals("Gói thành viên", response.getEntityName());
        assertNull(response.getEntityId());
    }

    private AuditLogEntity log(String action, String entityName, String entityId, Integer actorId) {
        return AuditLogEntity.builder()
                .logId(1)
                .actorAccountId(actorId)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .build();
    }

    private AccountEntity account(Integer id, String fullName, String email, String roleName) {
        return AccountEntity.builder()
                .accountId(id)
                .fullName(fullName)
                .email(email)
                .password("secret")
                .role(RoleEntity.builder().roleName(roleName).build())
                .status("Approved")
                .build();
    }
}
