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
import org.mockito.ArgumentCaptor;
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
        assertEquals("Danh sách chuyên gia gợi ý cho dự án Nâng cấp CRM AI", response.getEntityDisplayName());
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

        assertEquals("Gửi bản đề xuất", response.getAction());
        assertEquals("Bản đề xuất của Nguyễn Văn A cho dự án Nâng cấp CRM AI", response.getEntityDisplayName());
        assertEquals("Bản đề xuất", response.getEntityName());
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

    @Test
    void listForAdmin_shouldRenderEnglishWithdrawalActionAsVietnameseDisplay() {
        AccountEntity expert = account(20, "Expert AI", "expert@aitasker.local", "EXPERT");
        when(accountRepository.findById(20)).thenReturn(Optional.of(expert));
        when(auditLogRepository.findTop200ByOrderByCreatedAtDesc()).thenReturn(List.of(log(
                "WITHDRAWAL_REQUEST_CREATED",
                "withdrawal_requests",
                "legacy",
                20
        )));

        AuditLogResponse response = auditLogService.listForAdmin(null).get(0);

        assertEquals("Tạo yêu cầu rút tiền", response.getAction());
        assertEquals("Yêu cầu rút tiền", response.getEntityName());
        assertNull(response.getEntityId());
    }

    @Test
    void record_shouldPersistEnglishRawActionForLegacyVietnameseInput() {
        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);

        auditLogService.record("Tao yeu cau rut tien", "withdrawal_requests", "5", 20);

        verify(auditLogRepository).save(captor.capture());
        assertEquals("WITHDRAWAL_REQUEST_CREATED", captor.getValue().getAction());
        assertEquals("withdrawal_requests", captor.getValue().getEntityName());
        assertEquals("5", captor.getValue().getEntityId());
        assertEquals(20, captor.getValue().getActorAccountId());
    }

    @Test
    void record_shouldPersistEnglishRawActionForLegacyUriInput() {
        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);

        auditLogService.record("POST /api/payments/payos/create", "/api/payments/payos/create", "legacy", 20);

        verify(auditLogRepository).save(captor.capture());
        assertEquals("CREATE_PAYOS_PAYMENT_REQUEST", captor.getValue().getAction());
    }

    @Test
    void listForAdmin_shouldRenderDisputeDecisionObjectAsParticipantsInsteadOfStaffActor() {
        AccountEntity staffActor = account(99, "Staff Reviewer", "staff@aitasker.local", "STAFF");
        AccountEntity business = account(10, "Nova Retail", "business@aitasker.local", "BUSINESS");
        AccountEntity expert = account(20, "Expert AI", "expert@aitasker.local", "EXPERT");

        when(accountRepository.findById(99)).thenReturn(Optional.of(staffActor));
        when(accountRepository.findById(10)).thenReturn(Optional.of(business));
        when(accountRepository.findById(20)).thenReturn(Optional.of(expert));
        doReturn(Optional.of(BusinessProfileEntity.builder()
                .businessId(1)
                .accountId(10)
                .companyName("Nova Retail")
                .build())).when(businessProfileRepository).findById(any());
        doReturn(Optional.of(ExpertProfileEntity.builder()
                .expertId(2)
                .accountId(20)
                .build())).when(expertProfileRepository).findById(any());
        doReturn(Optional.of(ContractEntity.builder()
                .contractId(30)
                .businessId(1)
                .expertId(2)
                .build())).when(contractRepository).findById(any());
        doReturn(Optional.of(DisputeEntity.builder()
                .disputeId(9)
                .contractId(30)
                .assignedStaffId(7)
                .build())).when(disputeRepository).findById(any());
        doReturn(Optional.of(StaffEntity.builder()
                .staffId(7)
                .accountId(99)
                .build())).when(staffRepository).findById(any());
        when(auditLogRepository.findTop200ByOrderByCreatedAtDesc()).thenReturn(List.of(log(
                "DISPUTE_STAFF_AUTO_ASSIGNED",
                "disputes",
                "9",
                99
        )));

        AuditLogResponse response = auditLogService.listForAdmin(null).get(0);

        assertEquals("Tự động phân công tranh chấp", response.getAction());
        assertEquals("Tranh chấp giữa Nova Retail và Expert AI - nhân viên phụ trách: Staff Reviewer", response.getEntityDisplayName());
        assertEquals("Nova Retail", response.getEntityOwner());
        assertEquals("BUSINESS", response.getEntityOwnerRole());
        assertEquals("Staff Reviewer", response.getActor());
        assertEquals("STAFF", response.getActorRole());
    }

    @Test
    void listForAdmin_shouldRenderAutomaticSlaSettlementAsInternalSystemActor() {
        when(milestoneRepository.findById(7)).thenReturn(Optional.of(
                MilestoneEntity.builder().milestoneId(7).milestoneName("Bàn giao cuối").build()));
        when(auditLogRepository.findTop200ByOrderByCreatedAtDesc()).thenReturn(List.of(log(
                "MILESTONE_REVIEW_SLA_AUTO_APPROVED",
                "milestones",
                "7",
                null
        )));

        AuditLogResponse response = auditLogService.listForAdmin("INTERNAL").get(0);

        assertEquals("Hệ thống tự động duyệt và giải ngân cột mốc khi hết hạn nghiệm thu", response.getAction());
        assertEquals("Hệ thống tự động", response.getActor());
        assertEquals("INTERNAL", response.getActorGroup());
        assertNull(response.getActorAccountId());
        assertNull(response.getActorEmail());
        assertNull(response.getActorRole());
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
