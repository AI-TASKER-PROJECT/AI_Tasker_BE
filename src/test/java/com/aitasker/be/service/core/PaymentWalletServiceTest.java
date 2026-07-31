package com.aitasker.be.service.core;

import com.aitasker.be.dto.admin.MembershipPackageRequest;
import com.aitasker.be.dto.payment.CreditPurchaseRequest;
import com.aitasker.be.dto.payment.PaymentActionResponse;
import com.aitasker.be.dto.payment.QuotaResponse;
import com.aitasker.be.dto.payment.WalletTransactionHistoryResponse;
import com.aitasker.be.dto.payment.WithdrawalRequest;
import com.aitasker.be.dto.payment.WithdrawalReviewRequest;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.entity.ContractDepositEntity;
import com.aitasker.be.entity.ContractEntity;
import com.aitasker.be.entity.ContractMilestoneEntity;
import com.aitasker.be.entity.ExpertProfileEntity;
import com.aitasker.be.entity.MembershipPackageEntity;
import com.aitasker.be.entity.MembershipPurchaseEntity;
import com.aitasker.be.entity.MilestoneEntity;
import com.aitasker.be.entity.PaymentOrderEntity;
import com.aitasker.be.entity.PaymentProvider;
import com.aitasker.be.entity.PaymentStatus;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.entity.SystemWalletEntity;
import com.aitasker.be.entity.SystemSettingEntity;
import com.aitasker.be.entity.UserQuotaEntity;
import com.aitasker.be.entity.WalletTransactionEntity;
import com.aitasker.be.entity.WithdrawalRequestEntity;
import com.aitasker.be.repository.BusinessProfileRepository;
import com.aitasker.be.repository.ContractDepositRepository;
import com.aitasker.be.repository.ContractMilestoneRepository;
import com.aitasker.be.repository.ContractRepository;
import com.aitasker.be.repository.ExpertProfileRepository;
import com.aitasker.be.repository.JobRepository;
import com.aitasker.be.repository.MembershipPackageRepository;
import com.aitasker.be.repository.MembershipPurchaseRepository;
import com.aitasker.be.repository.MilestoneRepository;
import com.aitasker.be.repository.PaymentOrderRepository;
import com.aitasker.be.repository.QuotaUsageLogRepository;
import com.aitasker.be.repository.SystemSettingRepository;
import com.aitasker.be.repository.SystemWalletRepository;
import com.aitasker.be.repository.UserQuotaRepository;
import com.aitasker.be.repository.WalletTransactionRepository;
import com.aitasker.be.repository.WithdrawalRequestRepository;
import com.aitasker.be.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentWalletServiceTest {
    @Mock private AccessService accessService;
    @Mock private SystemWalletService systemWalletService;
    @Mock private WalletLedgerService walletLedgerService;
    @Mock private AuditLogService auditLogService;
    @Mock private MembershipPackageRepository membershipPackageRepository;
    @Mock private MembershipPurchaseRepository membershipPurchaseRepository;
    @Mock private UserQuotaRepository userQuotaRepository;
    @Mock private QuotaUsageLogRepository quotaUsageLogRepository;
    @Mock private SystemSettingRepository systemSettingRepository;
    @Mock private BusinessProfileRepository businessProfileRepository;
    @Mock private ExpertProfileRepository expertProfileRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private ContractDepositRepository contractDepositRepository;
    @Mock private ContractMilestoneRepository contractMilestoneRepository;
    @Mock private JobRepository jobRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private PaymentOrderRepository paymentOrderRepository;
    @Mock private SystemWalletRepository systemWalletRepository;
    @Mock private WithdrawalRequestRepository withdrawalRequestRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private PaymentWalletService paymentWalletService;

    @Test
    void getCreditPrices_shouldReturnActiveConfiguredValues() {
        when(systemSettingRepository.findById("credit.job_post.price_vnd")).thenReturn(Optional.of(
                SystemSettingEntity.builder().settingKey("credit.job_post.price_vnd")
                        .settingValue("500").isActive(true).build()));
        when(systemSettingRepository.findById("credit.proposal.price_vnd")).thenReturn(Optional.of(
                SystemSettingEntity.builder().settingKey("credit.proposal.price_vnd")
                        .settingValue("750").isActive(true).build()));

        var prices = paymentWalletService.getCreditPrices();

        assertEquals(new BigDecimal("500"), prices.getJobPostPriceVnd());
        assertEquals(new BigDecimal("750"), prices.getProposalPriceVnd());
        verify(accessService).requireRole("BUSINESS", "EXPERT", "ADMIN");
    }

    @Test
    void getContractDepositRates_shouldReturnBothConfiguredPercentages() {
        when(systemSettingRepository.findById("contract.deposit.business_percentage")).thenReturn(Optional.of(
                SystemSettingEntity.builder().settingValue("25").isActive(true).build()));
        when(systemSettingRepository.findById("contract.deposit.expert_percentage")).thenReturn(Optional.of(
                SystemSettingEntity.builder().settingValue("15").isActive(true).build()));

        var rates = paymentWalletService.getContractDepositRates();

        assertEquals(new BigDecimal("25"), rates.getBusinessPercentage());
        assertEquals(new BigDecimal("15"), rates.getExpertPercentage());
        verify(accessService).requireRole("BUSINESS", "EXPERT", "ADMIN", "STAFF");
    }

    @Test
    void listMembershipPackagesForAdmin_shouldUseActiveOnlyQuery() {
        MembershipPackageEntity active = packageEntity(1L, "BUSINESS_PLUS", "Business Plus", 30);
        when(membershipPackageRepository.findByIsActiveTrueOrderByRoleTypeAscPriceAscPackageNameAsc())
                .thenReturn(List.of(active));

        List<MembershipPackageEntity> result = paymentWalletService.listMembershipPackagesForAdmin(true);

        assertEquals(List.of(active), result);
        verify(accessService).requireRole("ADMIN");
    }

    @Test
    void createMembershipPackage_shouldPersistNormalizedPackage() {
        MembershipPackageRequest request = new MembershipPackageRequest();
        request.setRoleType("business");
        request.setPackageCode("business starter");
        request.setPackageName("Business Starter");
        request.setPrice(new BigDecimal("99000"));
        request.setBadgeDurationDays(30);
        request.setJobPostQuota(5);
        request.setProposalQuota(0);
        request.setRecommendVisibility(true);

        when(accessService.currentAccount()).thenReturn(adminAccount());
        when(membershipPackageRepository.findByPackageCodeIgnoreCase("BUSINESS_STARTER")).thenReturn(Optional.empty());
        when(membershipPackageRepository.save(any(MembershipPackageEntity.class))).thenAnswer(invocation -> {
            MembershipPackageEntity entity = invocation.getArgument(0);
            entity.setPackageId(55L);
            return entity;
        });

        MembershipPackageEntity saved = paymentWalletService.createMembershipPackage(request);

        assertEquals(Long.valueOf(55), saved.getPackageId());
        assertEquals("BUSINESS", saved.getRoleType());
        assertEquals("BUSINESS_STARTER", saved.getPackageCode());
        assertEquals(new BigDecimal("99000"), saved.getPrice());
        assertEquals(Boolean.TRUE, saved.getRecommendVisibility());
        assertEquals(Boolean.TRUE, saved.getIsActive());
        verify(accessService).requireRole("ADMIN");
        verify(auditLogService).record("MEMBERSHIP_PACKAGE_CREATED", "membership_packages", "55", 1);
    }

    @Test
    void updateMembershipPackage_shouldUpdateMutableFields() {
        MembershipPackageEntity entity = packageEntity(55L, "BUSINESS_STARTER", "Business Starter", 30);
        MembershipPackageRequest request = new MembershipPackageRequest();
        request.setPackageName("Business Starter Plus");
        request.setPrice(new BigDecimal("149000"));
        request.setJobPostQuota(9);
        request.setIsActive(false);

        when(accessService.currentAccount()).thenReturn(adminAccount());
        when(membershipPackageRepository.findById(55L)).thenReturn(Optional.of(entity));
        when(membershipPackageRepository.save(any(MembershipPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MembershipPackageEntity saved = paymentWalletService.updateMembershipPackage(55L, request);

        assertEquals("Business Starter Plus", saved.getPackageName());
        assertEquals(new BigDecimal("149000"), saved.getPrice());
        assertEquals(Integer.valueOf(9), saved.getJobPostQuota());
        assertEquals(Boolean.FALSE, saved.getIsActive());
        verify(accessService).requireRole("ADMIN");
        verify(auditLogService).record("MEMBERSHIP_PACKAGE_UPDATED", "membership_packages", "55", 1);
    }

    @Test
    void deleteMembershipPackage_shouldDeactivatePackage() {
        MembershipPackageEntity entity = packageEntity(55L, "BUSINESS_STARTER", "Business Starter", 30);

        when(accessService.currentAccount()).thenReturn(adminAccount());
        when(membershipPackageRepository.findById(55L)).thenReturn(Optional.of(entity));
        when(membershipPackageRepository.save(any(MembershipPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MembershipPackageEntity saved = paymentWalletService.deleteMembershipPackage(55L);

        assertEquals(Boolean.FALSE, saved.getIsActive());
        verify(accessService).requireRole("ADMIN");
        verify(auditLogService).record("MEMBERSHIP_PACKAGE_DELETED", "membership_packages", "55", 1);
    }

    @Test
    void ensureQuotaForAccount_shouldGrantInitialExpertProposalCredits() {
        AccountEntity expert = AccountEntity.builder()
                .accountId(9)
                .role(RoleEntity.builder().roleName("EXPERT").build())
                .build();
        when(userQuotaRepository.findByAccountIdForUpdate(9)).thenReturn(Optional.empty());
        when(userQuotaRepository.save(any(UserQuotaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentWalletService.ensureQuotaForAccount(expert);

        verify(userQuotaRepository).save(any(UserQuotaEntity.class));
        verify(quotaUsageLogRepository).save(argThat(log ->
                "PROPOSAL".equals(log.getQuotaType())
                        && "GRANT".equals(log.getActionType())
                        && Integer.valueOf(3).equals(log.getAmount())
                        && Integer.valueOf(3).equals(log.getBalanceAfter())
        ));
    }

    @Test
    void ensureQuotaForAccount_shouldGrantInitialBusinessJobPostCredits() {
        AccountEntity business = AccountEntity.builder()
                .accountId(10)
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .build();
        when(userQuotaRepository.findByAccountIdForUpdate(10)).thenReturn(Optional.empty());
        when(userQuotaRepository.save(any(UserQuotaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentWalletService.ensureQuotaForAccount(business);

        verify(userQuotaRepository).save(argThat(quota ->
                Integer.valueOf(3).equals(quota.getJobPostQuotaBalance())
                        && Integer.valueOf(0).equals(quota.getProposalQuotaBalance())
        ));
        verify(quotaUsageLogRepository).save(argThat(log ->
                "JOB_POST".equals(log.getQuotaType())
                        && "GRANT".equals(log.getActionType())
                        && Integer.valueOf(3).equals(log.getAmount())
                        && Integer.valueOf(3).equals(log.getBalanceAfter())
                        && "INITIAL_BUSINESS_GRANT".equals(log.getReferenceType())
        ));
    }

    @Test
    void purchaseJobPostCredits_shouldReturnTopupInfoWhenBalanceInsufficient() {
        AccountEntity business = AccountEntity.builder()
                .accountId(10)
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .status("Approved")
                .build();
        CreditPurchaseRequest request = new CreditPurchaseRequest();
        request.setQuantity(2);
        when(accessService.currentAccount()).thenReturn(business);
        when(systemSettingRepository.findById("credit.job_post.price_vnd")).thenReturn(Optional.empty());
        when(walletLedgerService.availableBalance(10)).thenReturn(new BigDecimal("100"));

        PaymentActionResponse<UserQuotaEntity> response = paymentWalletService.purchaseJobPostCredits(request);

        assertFalse(response.isCompleted());
        assertTrue(response.isNeedTopup());
        assertEquals(new BigDecimal("400"), response.getRequiredAmount());
        assertEquals(new BigDecimal("300"), response.getMissingAmount());
        assertEquals("/api/payments/payos/create", response.getRedirectUrl());
    }

    @Test
    void purchaseProposalCredits_shouldReturnTopupInfoWithRetailFallbackPrice() {
        AccountEntity expert = AccountEntity.builder()
                .accountId(11)
                .role(RoleEntity.builder().roleName("EXPERT").build())
                .status("Approved")
                .build();
        CreditPurchaseRequest request = new CreditPurchaseRequest();
        request.setQuantity(3);
        when(accessService.currentAccount()).thenReturn(expert);
        when(systemSettingRepository.findById("credit.proposal.price_vnd")).thenReturn(Optional.empty());
        when(walletLedgerService.availableBalance(11)).thenReturn(new BigDecimal("20"));

        PaymentActionResponse<UserQuotaEntity> response = paymentWalletService.purchaseProposalCredits(request);

        assertFalse(response.isCompleted());
        assertTrue(response.isNeedTopup());
        assertEquals(new BigDecimal("300"), response.getRequiredAmount());
        assertEquals(new BigDecimal("280"), response.getMissingAmount());
        assertEquals("/api/payments/payos/create", response.getRedirectUrl());
    }

    @Test
    void purchaseMembership_shouldPostPurchaserDebitAndPlatformRevenueCredit() {
        AccountEntity business = businessAccount();
        MembershipPackageEntity membershipPackage = packageEntity(
                1L, "BUSINESS_PLUS", "Business Plus", 30);
        membershipPackage.setPrice(new BigDecimal("500"));
        UserQuotaEntity quota = UserQuotaEntity.builder()
                .accountId(10)
                .jobPostQuotaBalance(0)
                .proposalQuotaBalance(0)
                .build();

        when(accessService.currentAccount()).thenReturn(business);
        when(membershipPackageRepository.findByPackageIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(membershipPackage));
        when(walletLedgerService.availableBalance(10)).thenReturn(new BigDecimal("1000"));
        stubPlatformPurchaseDebit(100L);
        when(userQuotaRepository.findByAccountIdForUpdate(10)).thenReturn(Optional.of(quota));
        when(userQuotaRepository.save(any(UserQuotaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipPurchaseRepository.save(any(MembershipPurchaseEntity.class))).thenAnswer(invocation -> {
            MembershipPurchaseEntity purchase = invocation.getArgument(0);
            purchase.setPurchaseId(20L);
            return purchase;
        });

        PaymentActionResponse<MembershipPurchaseEntity> response = paymentWalletService.purchaseMembership(1L);

        assertTrue(response.isCompleted());
        assertEquals(new BigDecimal("500"), response.getData().getAmount());
        verifyPlatformRevenuePosting(10, new BigDecimal("500"), "MEMBERSHIP_PURCHASE");
    }

    @Test
    void purchaseJobPostCredits_shouldPostPurchaserDebitAndPlatformRevenueCredit() {
        AccountEntity business = businessAccount();
        CreditPurchaseRequest request = new CreditPurchaseRequest();
        request.setQuantity(2);
        UserQuotaEntity quota = UserQuotaEntity.builder()
                .accountId(10)
                .jobPostQuotaBalance(0)
                .proposalQuotaBalance(0)
                .build();

        when(accessService.currentAccount()).thenReturn(business);
        when(systemSettingRepository.findById("credit.job_post.price_vnd")).thenReturn(Optional.empty());
        when(walletLedgerService.availableBalance(10)).thenReturn(new BigDecimal("1000"));
        stubPlatformPurchaseDebit(101L);
        when(userQuotaRepository.findByAccountIdForUpdate(10)).thenReturn(Optional.of(quota));
        when(userQuotaRepository.save(any(UserQuotaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentActionResponse<UserQuotaEntity> response = paymentWalletService.purchaseJobPostCredits(request);

        assertTrue(response.isCompleted());
        assertEquals(2, response.getData().getJobPostQuotaBalance());
        verifyPlatformRevenuePosting(10, new BigDecimal("400"), "CREDIT_PURCHASE");
    }

    @Test
    void purchaseProposalCredits_shouldPostPurchaserDebitAndPlatformRevenueCredit() {
        AccountEntity expert = AccountEntity.builder()
                .accountId(11)
                .role(RoleEntity.builder().roleName("EXPERT").build())
                .status("Approved")
                .build();
        CreditPurchaseRequest request = new CreditPurchaseRequest();
        request.setQuantity(3);
        UserQuotaEntity quota = UserQuotaEntity.builder()
                .accountId(11)
                .jobPostQuotaBalance(0)
                .proposalQuotaBalance(0)
                .build();

        when(accessService.currentAccount()).thenReturn(expert);
        when(systemSettingRepository.findById("credit.proposal.price_vnd")).thenReturn(Optional.empty());
        when(walletLedgerService.availableBalance(11)).thenReturn(new BigDecimal("1000"));
        stubPlatformPurchaseDebit(102L);
        when(userQuotaRepository.findByAccountIdForUpdate(11)).thenReturn(Optional.of(quota));
        when(userQuotaRepository.save(any(UserQuotaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentActionResponse<UserQuotaEntity> response = paymentWalletService.purchaseProposalCredits(request);

        assertTrue(response.isCompleted());
        assertEquals(3, response.getData().getProposalQuotaBalance());
        verifyPlatformRevenuePosting(11, new BigDecimal("300"), "CREDIT_PURCHASE");
    }

    @Test
    void purchaseMembership_shouldKeepPremiumActiveWhenLowerTierIsBoughtBeforePremiumExpires() {
        AccountEntity business = businessAccount();
        UserQuotaEntity quota = UserQuotaEntity.builder()
                .accountId(10)
                .jobPostQuotaBalance(0)
                .proposalQuotaBalance(0)
                .build();
        MembershipPackageEntity premium = packageEntity(1L, "BUSINESS_PREMIUM", "Business Premium", 90);
        MembershipPackageEntity plus = packageEntity(2L, "BUSINESS_PLUS", "Business Plus", 60);

        when(accessService.currentAccount()).thenReturn(business);
        when(membershipPackageRepository.findByPackageIdAndIsActiveTrue(1L)).thenReturn(Optional.of(premium));
        when(membershipPackageRepository.findByPackageIdAndIsActiveTrue(2L)).thenReturn(Optional.of(plus));
        when(walletLedgerService.availableBalance(10)).thenReturn(new BigDecimal("3000000"));
        stubPlatformPurchaseDebit(100L);
        when(userQuotaRepository.findByAccountIdForUpdate(10)).thenReturn(Optional.of(quota));
        when(userQuotaRepository.save(any(UserQuotaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipPurchaseRepository.save(any(MembershipPurchaseEntity.class))).thenAnswer(invocation -> {
            MembershipPurchaseEntity purchase = invocation.getArgument(0);
            purchase.setPurchaseId(purchase.getPackageId());
            return purchase;
        });

        paymentWalletService.purchaseMembership(1L);
        LocalDateTime premiumExpiredAt = quota.getPremiumExpiredAt();
        paymentWalletService.purchaseMembership(2L);

        assertNotNull(premiumExpiredAt);
        assertEquals(premiumExpiredAt, quota.getPremiumExpiredAt());
        assertTrue(quota.getPremiumExpiredAt().isAfter(LocalDateTime.now()));
        verify(systemWalletService, org.mockito.Mockito.times(2)).syncWallet();
    }

    @Test
    void purchaseMembership_shouldExtendPremiumExpirationCumulatively() {
        AccountEntity business = businessAccount();
        UserQuotaEntity quota = UserQuotaEntity.builder()
                .accountId(10)
                .jobPostQuotaBalance(0)
                .proposalQuotaBalance(0)
                .build();
        MembershipPackageEntity premium = packageEntity(1L, "BUSINESS_PREMIUM", "Business Premium", 90);

        when(accessService.currentAccount()).thenReturn(business);
        when(membershipPackageRepository.findByPackageIdAndIsActiveTrue(1L)).thenReturn(Optional.of(premium));
        when(walletLedgerService.availableBalance(10)).thenReturn(new BigDecimal("3000000"));
        stubPlatformPurchaseDebit(100L);
        when(userQuotaRepository.findByAccountIdForUpdate(10)).thenReturn(Optional.of(quota));
        when(userQuotaRepository.save(any(UserQuotaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipPurchaseRepository.save(any(MembershipPurchaseEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentWalletService.purchaseMembership(1L);
        LocalDateTime firstExpiration = quota.getPremiumExpiredAt();
        paymentWalletService.purchaseMembership(1L);

        assertEquals(firstExpiration.plusDays(90), quota.getPremiumExpiredAt());
    }

    @Test
    void currentQuota_shouldUsePremiumExpirationAndFallBackToHighestActiveNonPremiumPackage() {
        AccountEntity business = businessAccount();
        LocalDateTime now = LocalDateTime.now();
        UserQuotaEntity quota = UserQuotaEntity.builder()
                .accountId(10)
                .jobPostQuotaBalance(4)
                .proposalQuotaBalance(0)
                .badgeExpiredAt(now.plusDays(20))
                .premiumExpiredAt(now.minusDays(1))
                .build();
        MembershipPurchaseEntity standardPurchase = purchase(1L, 10, 11L, now.minusDays(1), now.plusDays(20));
        MembershipPurchaseEntity plusPurchase = purchase(2L, 10, 12L, now.minusDays(1), now.plusDays(10));
        MembershipPurchaseEntity premiumPurchase = purchase(3L, 10, 13L, now.minusDays(90), now.minusDays(1));
        MembershipPackageEntity standard = packageEntity(11L, "BUSINESS_STANDARD", "Business Standard", 30);
        MembershipPackageEntity plus = packageEntity(12L, "BUSINESS_PLUS", "Business Plus", 60);
        MembershipPackageEntity premium = packageEntity(13L, "BUSINESS_PREMIUM", "Business Premium", 90);

        when(accessService.currentAccount()).thenReturn(business);
        when(userQuotaRepository.findByAccountIdForUpdate(10)).thenReturn(Optional.of(quota));
        when(membershipPurchaseRepository.findByAccountIdOrderByCreatedAtDesc(10))
                .thenReturn(List.of(premiumPurchase, plusPurchase, standardPurchase));
        when(membershipPackageRepository.findAllById(any()))
                .thenReturn(List.of(standard, plus, premium));

        QuotaResponse response = paymentWalletService.currentQuota();

        assertFalse(response.getPremiumActive());
        assertEquals(quota.getPremiumExpiredAt(), response.getPremiumExpiredAt());
        assertEquals("PLUS", response.getActivePackageCode());
        assertEquals("Business Plus", response.getActivePackageName());
        assertEquals(Integer.valueOf(4), response.getJobPostQuotaBalance());
    }

    @Test
    void quotaResponse_shouldNotExposePremiumRecommendationVisible() throws Exception {
        assertThrows(NoSuchFieldException.class, () ->
                QuotaResponse.class.getDeclaredField("premiumRecommendationVisible"));
    }

    @Test
    void consumeJobPostCredit_shouldNotifyBusinessWithRemainingBalance() {
        AccountEntity business = businessAccount();
        UserQuotaEntity quota = UserQuotaEntity.builder()
                .accountId(10)
                .jobPostQuotaBalance(2)
                .proposalQuotaBalance(0)
                .build();
        com.aitasker.be.entity.JobEntity job = com.aitasker.be.entity.JobEntity.builder()
                .jobId(77)
                .title("Build AI assistant")
                .build();

        when(userQuotaRepository.findByAccountIdForUpdate(10)).thenReturn(Optional.of(quota));
        when(jobRepository.findById(77)).thenReturn(Optional.of(job));

        paymentWalletService.consumeJobPostCredit(business, 77L);

        assertEquals(Integer.valueOf(1), quota.getJobPostQuotaBalance());
        verify(notificationService).notifyJobPostQuotaConsumed(10, 10, 77L, "Build AI assistant", 1);
    }

    @Test
    void payContractDeposit_shouldHoldBusinessDepositAndWaitForExpertDeposit() {
        AccountEntity businessAccount = AccountEntity.builder()
                .accountId(10)
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .status("Approved")
                .build();
        BusinessProfileEntity business = BusinessProfileEntity.builder()
                .businessId(20)
                .accountId(10)
                .build();
        ContractEntity contract = ContractEntity.builder()
                .contractId(30)
                .businessId(20)
                .expertId(40)
                .jobId(50)
                .status("PENDING")
                .totalBudget(new BigDecimal("1000000"))
                .build();
        ContractMilestoneEntity contractMilestone = ContractMilestoneEntity.builder()
                .jobMilestoneId(60)
                .finalBudget(new BigDecimal("1000000"))
                .build();
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(60).jobId(50).build();
        com.aitasker.be.entity.JobEntity job = com.aitasker.be.entity.JobEntity.builder()
                .jobId(50)
                .status("OPEN")
                .budget(new BigDecimal("800000"))
                .build();

        when(accessService.currentAccount()).thenReturn(businessAccount);
        when(businessProfileRepository.findByAccountId(10)).thenReturn(Optional.of(business));
        when(contractRepository.findById(30)).thenReturn(Optional.of(contract));
        when(contractDepositRepository.findByContractIdAndOwnerRoleForUpdate(30, "BUSINESS")).thenReturn(Optional.empty());
        when(walletLedgerService.availableBalance(10)).thenReturn(new BigDecimal("300000"));
        when(walletLedgerService.holdEscrowFromAvailable(any(), any(), any(), any(), any(), any(), any(WalletLedgerService.WalletOperationContext.class)))
                .thenReturn(WalletTransactionEntity.builder().id(70L).build());
        when(contractDepositRepository.save(any(ContractDepositEntity.class))).thenAnswer(invocation -> {
            ContractDepositEntity deposit = invocation.getArgument(0);
            deposit.setDepositId(80L);
            return deposit;
        });
        when(contractRepository.save(any(ContractEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentActionResponse<ContractDepositEntity> response = paymentWalletService.payContractDeposit(30);

        assertTrue(response.isCompleted());
        assertEquals("HELD", response.getData().getStatus());
        assertEquals(new BigDecimal("200000.00"), response.getData().getDepositAmount());
        assertEquals("PENDING", contract.getStatus());
        assertNull(contract.getActivatedAt());
    }

    @Test
    void payExpertContractDeposit_shouldActivateWhenBothDepositsAreHeld() {
        AccountEntity expertAccount = AccountEntity.builder()
                .accountId(11).role(RoleEntity.builder().roleName("EXPERT").build()).status("Approved").build();
        ExpertProfileEntity expert = ExpertProfileEntity.builder().expertId(40).accountId(11).build();
        ContractEntity contract = ContractEntity.builder()
                .contractId(30).businessId(20).expertId(40).jobId(50)
                .status("PENDING").totalBudget(new BigDecimal("1000000")).build();
        ContractDepositEntity businessDeposit = ContractDepositEntity.builder()
                .contractId(30).ownerRole("BUSINESS").ownerAccountId(10)
                .heldAmount(new BigDecimal("200000.00")).status("HELD").build();
        ContractDepositEntity expertDeposit = ContractDepositEntity.builder()
                .contractId(30).ownerRole("EXPERT").ownerAccountId(11)
                .requiredAmount(new BigDecimal("100000.00")).heldAmount(new BigDecimal("100000.00"))
                .status("HELD").build();
        com.aitasker.be.entity.JobEntity job =
                com.aitasker.be.entity.JobEntity.builder().jobId(50).status("OPEN").build();

        when(accessService.currentAccount()).thenReturn(expertAccount);
        when(expertProfileRepository.findByAccountId(11)).thenReturn(Optional.of(expert));
        when(contractRepository.findById(30)).thenReturn(Optional.of(contract));
        when(contractDepositRepository.findByContractIdAndOwnerRoleForUpdate(30, "EXPERT"))
                .thenReturn(Optional.of(expertDeposit));
        when(contractDepositRepository.findByContractIdAndOwnerRole(30, "EXPERT"))
                .thenReturn(Optional.of(expertDeposit));
        when(contractDepositRepository.findByContractIdAndOwnerRole(30, "BUSINESS"))
                .thenReturn(Optional.of(businessDeposit));
        when(contractRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(30)).thenReturn(List.of());
        when(jobRepository.findById(50)).thenReturn(Optional.of(job));

        PaymentActionResponse<ContractDepositEntity> result =
                paymentWalletService.payExpertContractDeposit(30);

        assertTrue(result.isCompleted());
        assertEquals("ACTIVE", contract.getStatus());
        assertNotNull(contract.getActivatedAt());
        assertEquals("IN_PROGRESS", job.getStatus());
        verify(walletLedgerService, never()).holdEscrowFromAvailable(any(), any(), any(), any(), any(), any());
    }

    @Test
    void rejectWithdrawal_shouldReleaseHoldingAndMarkRejected() {
        AccountEntity admin = AccountEntity.builder()
                .accountId(1)
                .role(RoleEntity.builder().roleName("ADMIN").build())
                .build();
        WithdrawalRequestEntity withdrawal = WithdrawalRequestEntity.builder()
                .withdrawalId(90L)
                .accountId(10)
                .amount(new BigDecimal("120000"))
                .status("PENDING")
                .build();
        WithdrawalReviewRequest request = new WithdrawalReviewRequest();
        request.setAdminNote("Invalid bank info");

        when(accessService.currentAccount()).thenReturn(admin);
        when(withdrawalRequestRepository.findByIdForUpdate(90L)).thenReturn(Optional.of(withdrawal));
        when(walletLedgerService.releaseHoldingToAvailable(any(), any(), any(), any(), any(), any(), any(WalletLedgerService.WalletOperationContext.class)))
                .thenReturn(WalletTransactionEntity.builder().id(91L).build());
        when(withdrawalRequestRepository.save(any(WithdrawalRequestEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalRequestEntity saved = paymentWalletService.rejectWithdrawal(90L, request);

        assertEquals("REJECTED", saved.getStatus());
        assertEquals(Integer.valueOf(1), saved.getAdminId());
        assertEquals(Long.valueOf(91), saved.getReviewTransactionId());
        assertEquals("Invalid bank info", saved.getAdminNote());
        verify(notificationService).notifyWithdrawalRejected(10, 1, 90L, new BigDecimal("120000"), "Invalid bank info");
    }

    @Test
    void createWithdrawalRequest_shouldNotifyAdminsForReview() {
        AccountEntity expert = AccountEntity.builder()
                .accountId(10)
                .role(RoleEntity.builder().roleName("EXPERT").build())
                .status("Approved")
                .build();
        AccountEntity admin = AccountEntity.builder()
                .accountId(1)
                .role(RoleEntity.builder().roleName("ADMIN").build())
                .build();
        WithdrawalRequest request = new WithdrawalRequest();
        request.setAmount(new BigDecimal("50000"));
        request.setBankName("VCB");
        request.setBankAccountNumber("123456");
        request.setBankAccountHolder("Expert A");

        when(accessService.currentAccount()).thenReturn(expert);
        when(walletLedgerService.availableBalance(10)).thenReturn(new BigDecimal("100000"));
        when(systemWalletService.ensureWalletByAccountId(10))
                .thenReturn(com.aitasker.be.entity.SystemWalletEntity.builder().systemWalletId(20L).build());
        when(walletLedgerService.holdWithdrawalFromAvailable(any(), any(), any(), any(), any(), any(), any(WalletLedgerService.WalletOperationContext.class)))
                .thenReturn(WalletTransactionEntity.builder().id(30L).build());
        when(withdrawalRequestRepository.save(any(WithdrawalRequestEntity.class))).thenAnswer(invocation -> {
            WithdrawalRequestEntity withdrawal = invocation.getArgument(0);
            withdrawal.setWithdrawalId(40L);
            return withdrawal;
        });
        when(accountRepository.findAllByRoleRoleNameOrderByAccountIdAsc("ADMIN")).thenReturn(List.of(admin));

        paymentWalletService.createWithdrawalRequest(request);

        verify(notificationService).notifyWithdrawalReviewRequested(1, 10, 40L, new BigDecimal("50000"));
    }

    @Test
    void approveWithdrawal_shouldNotifyRequester() {
        AccountEntity admin = AccountEntity.builder()
                .accountId(1)
                .role(RoleEntity.builder().roleName("ADMIN").build())
                .build();
        WithdrawalRequestEntity withdrawal = WithdrawalRequestEntity.builder()
                .withdrawalId(90L)
                .accountId(10)
                .amount(new BigDecimal("120000"))
                .status("PENDING")
                .build();

        when(accessService.currentAccount()).thenReturn(admin);
        when(withdrawalRequestRepository.findByIdForUpdate(90L)).thenReturn(Optional.of(withdrawal));
        when(walletLedgerService.debitHolding(any(), any(), any(), any(), any(), any(), any(WalletLedgerService.WalletOperationContext.class)))
                .thenReturn(WalletTransactionEntity.builder().id(91L).build());
        when(withdrawalRequestRepository.save(any(WithdrawalRequestEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentWalletService.approveWithdrawal(90L, null);

        verify(notificationService).notifyWithdrawalApproved(10, 1, 90L, new BigDecimal("120000"));
    }

    @Test
    void listCurrentWalletTransactions_shouldReturnTransparentContractDepositHistory() {
        AccountEntity businessAccount = AccountEntity.builder()
                .accountId(10)
                .fullName("Nova Retail")
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .build();
        WalletTransactionEntity tx = WalletTransactionEntity.builder()
                .id(70L)
                .accountId(10)
                .transactionType("CONTRACT_SECURITY_DEPOSIT_HOLD")
                .direction("HOLD")
                .balanceType("ESCROW")
                .amount(new BigDecimal("200000"))
                .balanceBefore(new BigDecimal("500000"))
                .balanceAfter(new BigDecimal("300000"))
                .status("POSTED")
                .referenceType("CONTRACT_DEPOSIT")
                .referenceId(30L)
                .description("Contract security deposit")
                .build();
        ContractDepositEntity deposit = ContractDepositEntity.builder()
                .depositId(80L)
                .contractId(30)
                .businessId(20)
                .holdTransactionId(70L)
                .build();
        ContractEntity contract = ContractEntity.builder()
                .contractId(30)
                .businessId(20)
                .expertId(40)
                .jobId(50)
                .contractTitle("AI Sales Assistant")
                .build();
        BusinessProfileEntity business = BusinessProfileEntity.builder()
                .businessId(20)
                .companyName("Nova Retail")
                .build();
        ExpertProfileEntity expert = ExpertProfileEntity.builder()
                .expertId(40)
                .accountId(11)
                .build();
        AccountEntity expertAccount = AccountEntity.builder()
                .accountId(11)
                .fullName("Tran Hoang Nam")
                .build();

        when(accessService.currentAccount()).thenReturn(businessAccount);
        when(walletTransactionRepository.findByAccountIdOrderByCreatedAtDesc(10)).thenReturn(List.of(tx));
        when(contractDepositRepository.findByHoldTransactionId(70L)).thenReturn(Optional.of(deposit));
        when(contractRepository.findById(30)).thenReturn(Optional.of(contract));
        when(businessProfileRepository.findById(20)).thenReturn(Optional.of(business));
        when(expertProfileRepository.findById(40)).thenReturn(Optional.of(expert));
        when(accountRepository.findById(11)).thenReturn(Optional.of(expertAccount));
        when(jobRepository.findById(50)).thenReturn(Optional.of(com.aitasker.be.entity.JobEntity.builder()
                .jobId(50)
                .title("Build AI assistant")
                .build()));

        List<WalletTransactionHistoryResponse> history = paymentWalletService.listCurrentWalletTransactions();

        assertEquals(1, history.size());
        WalletTransactionHistoryResponse item = history.get(0);
        assertEquals("Nova Retail đã ký quỹ cho hợp đồng \"AI Sales Assistant\"", item.getTitle());
        assertEquals("hợp đồng \"AI Sales Assistant\"", item.getContractTitle());
        assertEquals("Nova Retail", item.getBusinessName());
        assertEquals("Tran Hoang Nam", item.getExpertName());
        assertEquals("Build AI assistant", item.getJobTitle());
        assertTrue(item.getDescription().contains("Nova Retail đã ký quỹ 200000 VND"));
    }

    @Test
    void listCurrentWalletTransactions_shouldCollapseOperationKeyEscrowDepositIntoSingleHistoryRow() {
        AccountEntity businessAccount = AccountEntity.builder()
                .accountId(10)
                .fullName("Nova Retail")
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .build();
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 12, 8, 0);
        WalletTransactionEntity debitTx = WalletTransactionEntity.builder()
                .id(70L)
                .accountId(10)
                .transactionType(WalletTransactionEntity.TX_ESCROW_DEPOSIT)
                .direction("DEBIT")
                .balanceType("AVAILABLE")
                .amount(new BigDecimal("200000"))
                .status("POSTED")
                .referenceType("MILESTONE")
                .referenceId(30L)
                .operationKey("MILESTONE_ESCROW_DEPOSIT:1:30")
                .description("Deposit milestone escrow")
                .createdAt(createdAt)
                .build();
        WalletTransactionEntity holdTx = WalletTransactionEntity.builder()
                .id(71L)
                .accountId(10)
                .transactionType(WalletTransactionEntity.TX_ESCROW_DEPOSIT)
                .direction("HOLD")
                .balanceType("ESCROW")
                .amount(new BigDecimal("200000"))
                .status("POSTED")
                .referenceType("MILESTONE")
                .referenceId(30L)
                .operationKey("MILESTONE_ESCROW_DEPOSIT:1:30")
                .description("Deposit milestone escrow")
                .createdAt(createdAt.plusSeconds(1))
                .build();

        when(accessService.currentAccount()).thenReturn(businessAccount);
        when(walletTransactionRepository.findByAccountIdOrderByCreatedAtDesc(10)).thenReturn(List.of(holdTx, debitTx));
        when(walletTransactionRepository.findByOperationKeyOrderByCreatedAtAscIdAsc("MILESTONE_ESCROW_DEPOSIT:1:30"))
                .thenReturn(List.of(debitTx, holdTx));

        List<WalletTransactionHistoryResponse> history = paymentWalletService.listCurrentWalletTransactions();

        assertEquals(1, history.size());
        WalletTransactionHistoryResponse item = history.get(0);
        assertEquals("MILESTONE_ESCROW_DEPOSIT:1:30", item.getOperationKey());
        assertEquals("MILESTONE_ESCROW_DEPOSIT", item.getTransactionType());
        assertEquals(createdAt, item.getCreatedAt());
        assertEquals(new BigDecimal("200000"), item.getAmount());
    }

    @Test
    void listCurrentWalletTransactions_shouldCollapsePendingWithdrawalLedgerEntries() {
        AccountEntity expertAccount = AccountEntity.builder()
                .accountId(10)
                .fullName("Expert A")
                .role(RoleEntity.builder().roleName("EXPERT").build())
                .build();
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 3, 10, 0);
        WalletTransactionEntity debitTx = WalletTransactionEntity.builder()
                .id(90L)
                .accountId(10)
                .transactionType("WITHDRAW_HOLD")
                .direction("DEBIT")
                .balanceType("AVAILABLE")
                .amount(new BigDecimal("120000"))
                .status("POSTED")
                .createdAt(requestedAt)
                .build();
        WalletTransactionEntity holdTx = WalletTransactionEntity.builder()
                .id(91L)
                .accountId(10)
                .transactionType("WITHDRAW_HOLD")
                .direction("HOLD")
                .balanceType("HOLDING")
                .amount(new BigDecimal("120000"))
                .status("POSTED")
                .createdAt(requestedAt.plusSeconds(1))
                .build();
        WithdrawalRequestEntity withdrawal = WithdrawalRequestEntity.builder()
                .withdrawalId(100L)
                .accountId(10)
                .amount(new BigDecimal("120000"))
                .bankName("VCB")
                .bankAccountHolder("Expert A")
                .status("PENDING")
                .holdTransactionId(91L)
                .requestedAt(requestedAt)
                .build();

        when(accessService.currentAccount()).thenReturn(expertAccount);
        when(walletTransactionRepository.findByAccountIdOrderByCreatedAtDesc(10))
                .thenReturn(List.of(holdTx, debitTx));
        when(withdrawalRequestRepository.findByHoldTransactionId(91L)).thenReturn(Optional.of(withdrawal));

        List<WalletTransactionHistoryResponse> history = paymentWalletService.listCurrentWalletTransactions();

        assertEquals(1, history.size());
        WalletTransactionHistoryResponse item = history.get(0);
        assertEquals(100L, item.getWithdrawalId());
        assertEquals("WITHDRAW_HOLD", item.getTransactionType());
        assertEquals("Yêu cầu rút tiền đang chờ duyệt", item.getTitle());
        assertEquals(requestedAt, item.getCreatedAt());
        verify(withdrawalRequestRepository, never()).findById(100L);
    }

    @Test
    void listCurrentWalletTransactions_shouldCollapseApprovedWithdrawalLedgerEntries() {
        AccountEntity expertAccount = AccountEntity.builder()
                .accountId(10)
                .fullName("Expert A")
                .role(RoleEntity.builder().roleName("EXPERT").build())
                .build();
        AccountEntity adminAccount = AccountEntity.builder()
                .accountId(1)
                .fullName("Admin One")
                .build();
        LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 3, 10, 0);
        LocalDateTime reviewedAt = requestedAt.plusHours(1);
        WalletTransactionEntity holdTx = WalletTransactionEntity.builder()
                .id(91L)
                .accountId(10)
                .transactionType("WITHDRAW_HOLD")
                .direction("HOLD")
                .balanceType("HOLDING")
                .amount(new BigDecimal("120000"))
                .status("POSTED")
                .createdAt(requestedAt)
                .build();
        WalletTransactionEntity approvedTx = WalletTransactionEntity.builder()
                .id(92L)
                .accountId(10)
                .transactionType("WITHDRAW_APPROVED")
                .direction("DEBIT")
                .balanceType("HOLDING")
                .amount(new BigDecimal("120000"))
                .status("POSTED")
                .createdAt(reviewedAt)
                .build();
        WithdrawalRequestEntity withdrawal = WithdrawalRequestEntity.builder()
                .withdrawalId(100L)
                .accountId(10)
                .amount(new BigDecimal("120000"))
                .bankName("VCB")
                .bankAccountHolder("Expert A")
                .status("APPROVED")
                .holdTransactionId(91L)
                .reviewTransactionId(92L)
                .adminId(1)
                .requestedAt(requestedAt)
                .reviewedAt(reviewedAt)
                .build();

        when(accessService.currentAccount()).thenReturn(expertAccount);
        when(walletTransactionRepository.findByAccountIdOrderByCreatedAtDesc(10))
                .thenReturn(List.of(approvedTx, holdTx));
        when(withdrawalRequestRepository.findByReviewTransactionId(92L)).thenReturn(Optional.of(withdrawal));
        when(withdrawalRequestRepository.findByHoldTransactionId(91L)).thenReturn(Optional.of(withdrawal));
        when(accountRepository.findById(1)).thenReturn(Optional.of(adminAccount));

        List<WalletTransactionHistoryResponse> history = paymentWalletService.listCurrentWalletTransactions();

        assertEquals(1, history.size());
        WalletTransactionHistoryResponse item = history.get(0);
        assertEquals(100L, item.getWithdrawalId());
        assertEquals("WITHDRAW_APPROVED", item.getTransactionType());
        assertEquals("Rút tiền thành công", item.getTitle());
        assertEquals(reviewedAt, item.getCreatedAt());
        verify(withdrawalRequestRepository, never()).findById(100L);
    }

    @Test
    void listCurrentWalletTransactions_shouldReturnTransparentRejectedWithdrawalHistory() {
        AccountEntity expertAccount = AccountEntity.builder()
                .accountId(10)
                .fullName("Expert A")
                .role(RoleEntity.builder().roleName("EXPERT").build())
                .build();
        AccountEntity adminAccount = AccountEntity.builder()
                .accountId(1)
                .fullName("Admin One")
                .build();
        WalletTransactionEntity tx = WalletTransactionEntity.builder()
                .id(91L)
                .accountId(10)
                .transactionType("WITHDRAW_REJECTED")
                .direction("RELEASE")
                .balanceType("WITHDRAW_HOLD")
                .amount(new BigDecimal("120000"))
                .balanceBefore(BigDecimal.ZERO)
                .balanceAfter(new BigDecimal("120000"))
                .status("POSTED")
                .referenceType("WITHDRAW_REQUEST")
                .referenceId(90L)
                .description("Withdrawal rejected")
                .build();
        WithdrawalRequestEntity withdrawal = WithdrawalRequestEntity.builder()
                .withdrawalId(90L)
                .accountId(10)
                .amount(new BigDecimal("120000"))
                .bankName("VCB")
                .bankAccountHolder("Expert A")
                .adminId(1)
                .adminNote("Sai số tài khoản")
                .status("REJECTED")
                .reviewTransactionId(91L)
                .requestedAt(LocalDateTime.of(2026, 7, 3, 10, 0))
                .reviewedAt(LocalDateTime.of(2026, 7, 3, 11, 0))
                .build();

        when(accessService.currentAccount()).thenReturn(expertAccount);
        when(walletTransactionRepository.findByAccountIdOrderByCreatedAtDesc(10)).thenReturn(List.of(tx));
        when(withdrawalRequestRepository.findByReviewTransactionId(91L)).thenReturn(Optional.of(withdrawal));
        when(accountRepository.findById(1)).thenReturn(Optional.of(adminAccount));

        List<WalletTransactionHistoryResponse> history = paymentWalletService.listCurrentWalletTransactions();

        assertEquals(1, history.size());
        WalletTransactionHistoryResponse item = history.get(0);
        assertEquals("Yêu cầu rút tiền bị từ chối", item.getTitle());
        assertEquals("VCB", item.getBankName());
        assertEquals("Admin One", item.getAdminName());
        assertTrue(item.getDescription().contains("bị từ chối bởi Admin One"));
        assertTrue(item.getDescription().contains("Lý do: Sai số tài khoản."));
        assertEquals("Sai số tài khoản", item.getAdminNote());
        verify(withdrawalRequestRepository, never()).findById(90L);
    }

    @Test
    void listCurrentWalletTransactions_shouldExposeReconciliationFieldsForTopup() {
        AccountEntity businessAccount = AccountEntity.builder()
                .accountId(10)
                .fullName("Nova Retail")
                .email("finance@nova.test")
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .build();
        WalletTransactionEntity tx = WalletTransactionEntity.builder()
                .id(200L)
                .systemWalletId(5L)
                .accountId(10)
                .paymentOrderId(77L)
                .transactionType("TOPUP")
                .direction("CREDIT")
                .balanceType("AVAILABLE")
                .amount(new BigDecimal("300000"))
                .balanceBefore(new BigDecimal("100000"))
                .balanceAfter(new BigDecimal("400000"))
                .status("POSTED")
                .referenceType("PAYMENT_ORDER")
                .referenceId(77L)
                .operationKey("TOPUP:77")
                .operationLeg(WalletLedgerService.LEG_AVAILABLE_CREDIT)
                .metadata("{\"source\":\"PAYOS_SYNC\"}")
                .description("Top up wallet")
                .createdAt(LocalDateTime.of(2026, 7, 21, 9, 0))
                .build();
        PaymentOrderEntity paymentOrder = PaymentOrderEntity.builder()
                .id(77L)
                .provider(PaymentProvider.PAYOS)
                .providerOrderCode(90077L)
                .providerTransactionNo("BANK-TXN-77")
                .providerPaymentLinkId("plink_77")
                .status(PaymentStatus.PAID)
                .build();
        SystemWalletEntity wallet = SystemWalletEntity.builder()
                .systemWalletId(5L)
                .accountId(10)
                .walletType("BUSINESS")
                .currency("VND")
                .build();

        when(accessService.currentAccount()).thenReturn(businessAccount);
        when(walletTransactionRepository.findByAccountIdOrderByCreatedAtDesc(10)).thenReturn(List.of(tx));
        when(systemWalletRepository.findById(5L)).thenReturn(Optional.of(wallet));
        when(paymentOrderRepository.findById(77L)).thenReturn(Optional.of(paymentOrder));

        List<WalletTransactionHistoryResponse> history = paymentWalletService.listCurrentWalletTransactions();

        assertEquals(1, history.size());
        WalletTransactionHistoryResponse item = history.get(0);
        assertEquals("USER_WALLET", item.getHistoryScope());
        assertEquals("TOPUP", item.getTransactionCategory());
        assertEquals(5L, item.getSystemWalletId());
        assertEquals("BUSINESS", item.getWalletType());
        assertEquals("BUSINESS", item.getActorRole());
        assertEquals("VND", item.getCurrency());
        assertEquals(new BigDecimal("300000"), item.getGrossAmount());
        assertEquals(BigDecimal.ZERO, item.getFeeAmount());
        assertEquals(new BigDecimal("300000"), item.getNetAmount());
        assertEquals("PAYOS", item.getPaymentProvider());
        assertEquals(90077L, item.getProviderOrderCode());
        assertEquals("BANK-TXN-77", item.getProviderTransactionNo());
        assertEquals("plink_77", item.getProviderPaymentLinkId());
        assertEquals("PayOS", item.getSenderName());
        assertEquals("BANK-TXN-77", item.getSenderAccount());
        assertEquals("Cổng thanh toán", item.getSenderRoleLabel());
        assertEquals("Nova Retail", item.getReceiverName());
        assertEquals("finance@nova.test", item.getReceiverAccount());
        assertEquals("Doanh nghiệp", item.getReceiverRoleLabel());
        assertEquals("{\"source\":\"PAYOS_SYNC\"}", item.getMetadata());
        assertFalse(item.getPlatformBalanceChanging());
    }

    @Test
    void payContractDeposit_shouldUseConfiguredRateAndFinalMilestoneBudget() {
        AccountEntity account = AccountEntity.builder().accountId(10)
                .role(RoleEntity.builder().roleName("BUSINESS").build()).status("Approved").build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(20).accountId(10).build();
        ContractEntity contract = ContractEntity.builder().contractId(30).businessId(20)
                .status("PENDING").totalBudget(new BigDecimal("1000000")).build();
        ContractMilestoneEntity milestone = ContractMilestoneEntity.builder().contractId(30)
                .originalBudget(new BigDecimal("1000000")).finalBudget(new BigDecimal("1200000")).build();
        SystemSettingEntity rate = SystemSettingEntity.builder()
                .settingKey("contract.deposit.business_percentage").settingValue("25").isActive(true).build();

        when(accessService.currentAccount()).thenReturn(account);
        when(businessProfileRepository.findByAccountId(10)).thenReturn(Optional.of(business));
        when(contractRepository.findById(30)).thenReturn(Optional.of(contract));
        when(systemSettingRepository.findById("contract.deposit.business_percentage")).thenReturn(Optional.of(rate));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(30)).thenReturn(List.of(milestone));
        when(contractDepositRepository.findByContractIdAndOwnerRoleForUpdate(30, "BUSINESS")).thenReturn(Optional.empty());
        when(walletLedgerService.availableBalance(10)).thenReturn(new BigDecimal("500000"));
        when(walletLedgerService.holdEscrowFromAvailable(any(), any(), any(), any(), any(), any(), any(WalletLedgerService.WalletOperationContext.class)))
                .thenReturn(WalletTransactionEntity.builder().id(70L).build());
        when(contractDepositRepository.save(any(ContractDepositEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentActionResponse<ContractDepositEntity> response = paymentWalletService.payContractDeposit(30);

        assertTrue(response.isCompleted());
        assertEquals(new BigDecimal("25"), response.getData().getRequiredPercentage());
        assertEquals(new BigDecimal("300000.00"), response.getData().getRequiredAmount());
        assertEquals(new BigDecimal("300000.00"), response.getData().getDepositAmount());
    }

    @Test
    void listPlatformWalletTransactions_shouldReturnVietnameseBusinessEventsForAdmin() {
        AccountEntity businessAccount = AccountEntity.builder()
                .accountId(10)
                .fullName("Doanh nghiệp A")
                .build();
        AccountEntity expertAccount = AccountEntity.builder()
                .accountId(11)
                .fullName("Chuyên gia E")
                .build();
        LocalDateTime start = LocalDateTime.of(2026, 6, 27, 0, 0);
        WalletTransactionEntity skippedWithdrawDebit = WalletTransactionEntity.builder()
                .id(39L)
                .accountId(11)
                .transactionType("WITHDRAW_HOLD")
                .direction("DEBIT")
                .balanceType("AVAILABLE")
                .amount(new BigDecimal("5000000"))
                .description("Withdrawal request")
                .build();
        WalletTransactionEntity withdrawHold = WalletTransactionEntity.builder()
                .id(40L)
                .accountId(11)
                .transactionType("WITHDRAW_HOLD")
                .direction("HOLD")
                .balanceType("HOLDING")
                .amount(new BigDecimal("5000000"))
                .description("Withdrawal request")
                .build();
        WalletTransactionEntity creditPurchase = WalletTransactionEntity.builder()
                .id(50L)
                .accountId(10)
                .transactionType("CREDIT_PURCHASE")
                .direction("DEBIT")
                .balanceType("AVAILABLE")
                .amount(new BigDecimal("100000"))
                .description("Buy job-post credits: 10")
                .build();
        WalletTransactionEntity membershipPurchase = WalletTransactionEntity.builder()
                .id(60L)
                .accountId(10)
                .transactionType("MEMBERSHIP_PURCHASE")
                .direction("DEBIT")
                .balanceType("AVAILABLE")
                .amount(new BigDecimal("500000"))
                .referenceId(1L)
                .description("Premium Business")
                .build();
        WalletTransactionEntity platformRevenueCredit = WalletTransactionEntity.builder()
                .id(61L)
                .accountId(1)
                .transactionType("MEMBERSHIP_PURCHASE")
                .direction("CREDIT")
                .balanceType("AVAILABLE")
                .amount(new BigDecimal("500000"))
                .operationLeg(WalletLedgerService.LEG_PLATFORM_REVENUE_CREDIT)
                .description("Premium Business")
                .build();
        WithdrawalRequestEntity withdrawal = WithdrawalRequestEntity.builder()
                .withdrawalId(90L)
                .accountId(11)
                .amount(new BigDecimal("5000000"))
                .bankName("Vietcombank")
                .bankAccountHolder("Nguyen Van E")
                .holdTransactionId(40L)
                .build();
        MembershipPurchaseEntity purchase = MembershipPurchaseEntity.builder()
                .purchaseId(70L)
                .accountId(10)
                .packageId(1L)
                .walletTransactionId(60L)
                .badgeStartAt(start)
                .badgeEndAt(start.plusDays(30))
                .build();
        MembershipPackageEntity membershipPackage = packageEntity(1L, "BUSINESS_PREMIUM", "Premium Business", 30);

        when(walletTransactionRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(skippedWithdrawDebit, withdrawHold, creditPurchase, membershipPurchase,
                        platformRevenueCredit));
        when(accountRepository.findById(10)).thenReturn(Optional.of(businessAccount));
        when(accountRepository.findById(11)).thenReturn(Optional.of(expertAccount));
        when(withdrawalRequestRepository.findByHoldTransactionId(40L)).thenReturn(Optional.of(withdrawal));
        when(membershipPurchaseRepository.findByWalletTransactionId(60L)).thenReturn(Optional.of(purchase));
        when(membershipPackageRepository.findById(1L)).thenReturn(Optional.of(membershipPackage));

        List<WalletTransactionHistoryResponse> history = paymentWalletService.listPlatformWalletTransactions();

        assertEquals(3, history.size());
        assertEquals("Chuyên gia E đã tạo yêu cầu rút tiền", history.get(0).getTitle());
        assertEquals("Hệ thống đã tạm giữ 5000000 VND cho yêu cầu rút tiền của Chuyên gia E. Ngân hàng: Vietcombank, chủ tài khoản: Nguyen Van E.",
                history.get(0).getDescription());
        assertEquals("Doanh nghiệp A đã mua 10 lượt đăng dự án", history.get(1).getTitle());
        assertEquals("Doanh nghiệp A thanh toán 100000 VNĐ để mua 10 lượt đăng dự án.", history.get(1).getDescription());
        assertEquals("Doanh nghiệp A đã mua gói Premium Business", history.get(2).getTitle());
        assertEquals("Doanh nghiệp A thanh toán 500000 VND để mua gói Premium Business. Thời hạn từ 27/06/2026 đến 27/07/2026.",
                history.get(2).getDescription());
    }

    @Test
    void listPlatformWalletLedger_shouldReturnOnlyPlatformWalletLedgerRows() {
        AccountEntity admin = adminAccount();
        admin.setFullName("Admin System");
        AccountEntity purchaser = AccountEntity.builder()
                .accountId(10)
                .fullName("Doanh nghiệp A")
                .email("billing@business-a.test")
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .build();
        SystemWalletEntity platformWallet = SystemWalletEntity.builder()
                .systemWalletId(1L)
                .accountId(1)
                .walletType("ADMIN_SYSTEM")
                .currency("VND")
                .build();
        WalletTransactionEntity purchaserDebit = WalletTransactionEntity.builder()
                .id(60L)
                .accountId(10)
                .transactionType("MEMBERSHIP_PURCHASE")
                .direction("DEBIT")
                .balanceType("AVAILABLE")
                .amount(new BigDecimal("500000"))
                .operationKey("MEMBERSHIP_PURCHASE:abc")
                .operationLeg(WalletLedgerService.LEG_PURCHASER_AVAILABLE_DEBIT)
                .description("Premium Business")
                .build();
        WalletTransactionEntity platformRevenueCredit = WalletTransactionEntity.builder()
                .id(61L)
                .systemWalletId(1L)
                .accountId(1)
                .transactionType("MEMBERSHIP_PURCHASE")
                .direction("CREDIT")
                .balanceType("AVAILABLE")
                .amount(new BigDecimal("500000"))
                .balanceBefore(new BigDecimal("1000000"))
                .balanceAfter(new BigDecimal("1500000"))
                .status("POSTED")
                .operationKey("MEMBERSHIP_PURCHASE:abc")
                .operationLeg(WalletLedgerService.LEG_PLATFORM_REVENUE_CREDIT)
                .description("Premium Business")
                .createdAt(LocalDateTime.of(2026, 7, 21, 10, 0))
                .build();

        when(accountRepository.findFirstByRoleRoleNameOrderByAccountIdAsc("ADMIN"))
                .thenReturn(Optional.of(admin));
        when(walletTransactionRepository.findByAccountIdOrderByCreatedAtDesc(1))
                .thenReturn(List.of(platformRevenueCredit));
        when(systemWalletRepository.findById(1L)).thenReturn(Optional.of(platformWallet));
        when(walletTransactionRepository.findByOperationKeyOrderByCreatedAtAscIdAsc("MEMBERSHIP_PURCHASE:abc"))
                .thenReturn(List.of(purchaserDebit, platformRevenueCredit));
        when(accountRepository.findById(10)).thenReturn(Optional.of(purchaser));

        List<WalletTransactionHistoryResponse> history = paymentWalletService.listPlatformWalletLedger();

        assertEquals(1, history.size());
        WalletTransactionHistoryResponse item = history.get(0);
        assertEquals("PLATFORM_WALLET", item.getHistoryScope());
        assertEquals("REVENUE", item.getTransactionCategory());
        assertEquals(Boolean.TRUE, item.getPlatformBalanceChanging());
        assertEquals(1, item.getAccountId());
        assertEquals("ADMIN_SYSTEM", item.getWalletType());
        assertEquals("ADMIN", item.getWalletOwnerRole());
        assertEquals("Doanh nghiệp A", item.getCounterpartyName());
        assertEquals(Integer.valueOf(10), item.getCounterpartyAccountId());
        assertEquals("BUSINESS", item.getCounterpartyRole());
        assertEquals("Doanh nghiệp A", item.getSenderName());
        assertEquals("billing@business-a.test", item.getSenderAccount());
        assertEquals("Doanh nghiệp", item.getSenderRoleLabel());
        assertEquals("Admin System", item.getReceiverName());
        assertEquals("platform@aitasker.test", item.getReceiverAccount());
        assertEquals("Nội bộ", item.getReceiverRoleLabel());
        assertTrue(item.getTitle().contains("Nền tảng ghi nhận doanh thu"));
    }

    private AccountEntity businessAccount() {
        return AccountEntity.builder()
                .accountId(10)
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .status("Approved")
                .build();
    }

    private void stubPlatformPurchaseDebit(Long transactionId) {
        when(accountRepository.findFirstByRoleRoleNameOrderByAccountIdAsc("ADMIN"))
                .thenReturn(Optional.of(adminAccount()));
        when(walletLedgerService.debitAvailable(
                any(), any(), any(), any(), any(), any(),
                any(WalletLedgerService.WalletOperationContext.class)))
                .thenReturn(WalletTransactionEntity.builder().id(transactionId).build());
    }

    private void verifyPlatformRevenuePosting(Integer purchaserAccountId, BigDecimal amount, String transactionType) {
        ArgumentCaptor<WalletLedgerService.WalletOperationContext> debitContext =
                ArgumentCaptor.forClass(WalletLedgerService.WalletOperationContext.class);
        ArgumentCaptor<WalletLedgerService.WalletOperationContext> creditContext =
                ArgumentCaptor.forClass(WalletLedgerService.WalletOperationContext.class);

        verify(walletLedgerService).debitAvailable(
                eq(purchaserAccountId), eq(amount), eq(transactionType), any(), any(), any(), debitContext.capture());
        verify(walletLedgerService).creditPlatformRevenue(
                eq(1), eq(amount), eq(transactionType), any(), any(), any(), creditContext.capture());

        assertNotNull(debitContext.getValue().operationKey());
        assertEquals(debitContext.getValue().operationKey(), creditContext.getValue().operationKey());
        assertEquals(WalletLedgerService.LEG_PURCHASER_AVAILABLE_DEBIT,
                debitContext.getValue().operationLeg());
        assertEquals(WalletLedgerService.LEG_PLATFORM_REVENUE_CREDIT,
                creditContext.getValue().operationLeg());
    }

    private AccountEntity adminAccount() {
        return AccountEntity.builder()
                .accountId(1)
                .email("platform@aitasker.test")
                .role(RoleEntity.builder().roleName("ADMIN").build())
                .status("Approved")
                .build();
    }

    private MembershipPackageEntity packageEntity(Long packageId, String code, String name, int durationDays) {
        return MembershipPackageEntity.builder()
                .packageId(packageId)
                .roleType("BUSINESS")
                .packageCode(code)
                .packageName(name)
                .price(BigDecimal.ZERO)
                .badgeDurationDays(durationDays)
                .jobPostQuota(0)
                .proposalQuota(0)
                .recommendVisibility(code.endsWith("PREMIUM"))
                .isActive(true)
                .build();
    }

    private MembershipPurchaseEntity purchase(Long purchaseId, Integer accountId, Long packageId, LocalDateTime start, LocalDateTime end) {
        return MembershipPurchaseEntity.builder()
                .purchaseId(purchaseId)
                .accountId(accountId)
                .packageId(packageId)
                .amount(BigDecimal.ZERO)
                .status("SUCCESS")
                .badgeStartAt(start)
                .badgeEndAt(end)
                .build();
    }

    // --- v2.3 auto-participant-deposit-refund tests ---

    @Test
    void autoRefundParticipantDeposits_shouldRefundBothAndCloseContract() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5)
                .status(ContractEntity.STATUS_COMPLETED).build();
        ContractDepositEntity bizDeposit = ContractDepositEntity.builder()
                .depositId(1L).contractId(1).ownerRole("BUSINESS").status("HELD").ownerAccountId(50)
                .heldAmount(BigDecimal.valueOf(20_000_000)).refundedAmount(BigDecimal.ZERO).build();
        ContractDepositEntity expDeposit = ContractDepositEntity.builder()
                .depositId(2L).contractId(1).ownerRole("EXPERT").status("HELD").ownerAccountId(60)
                .heldAmount(BigDecimal.valueOf(10_000_000)).refundedAmount(BigDecimal.ZERO).build();
        WalletTransactionEntity bizTx = WalletTransactionEntity.builder().id(701L).build();
        WalletTransactionEntity expTx = WalletTransactionEntity.builder().id(702L).build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(contractDepositRepository.findByContractIdOrderByOwnerRoleAsc(1))
                .thenReturn(List.of(bizDeposit, expDeposit));
        when(walletLedgerService.releaseEscrowToAvailable(any(), any(), any(), any(), any(), any(), any(WalletLedgerService.WalletOperationContext.class)))
                .thenReturn(bizTx, expTx);
        when(contractDepositRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<ContractDepositEntity> result = paymentWalletService.autoRefundParticipantDeposits(1, 1);

        assertEquals(2, result.size());
        assertEquals("REFUNDED", bizDeposit.getStatus());
        assertEquals("REFUNDED", expDeposit.getStatus());
        assertEquals("STANDARD_REFUND", bizDeposit.getResolutionType());
        assertNotNull(bizDeposit.getResolvedAt());
        verify(walletLedgerService, times(2)).releaseEscrowToAvailable(any(), any(), any(), any(), any(), any(), any(WalletLedgerService.WalletOperationContext.class));
        verify(auditLogService).record(eq("PARTICIPANT_DEPOSITS_REFUNDED"), any(), any(), eq(1));
    }

    @Test
    void autoRefundParticipantDeposits_shouldSkipAlreadyRefunded() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5)
                .status(ContractEntity.STATUS_COMPLETED).build();
        ContractDepositEntity bizDeposit = ContractDepositEntity.builder()
                .depositId(1L).contractId(1).ownerRole("BUSINESS").status("REFUNDED").ownerAccountId(50)
                .heldAmount(BigDecimal.ZERO).refundedAmount(BigDecimal.valueOf(20_000_000)).build();
        ContractDepositEntity expDeposit = ContractDepositEntity.builder()
                .depositId(2L).contractId(1).ownerRole("EXPERT").status("HELD").ownerAccountId(60)
                .heldAmount(BigDecimal.valueOf(10_000_000)).refundedAmount(BigDecimal.ZERO).build();
        WalletTransactionEntity tx = WalletTransactionEntity.builder().id(703L).build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(contractDepositRepository.findByContractIdOrderByOwnerRoleAsc(1))
                .thenReturn(List.of(bizDeposit, expDeposit));
        when(walletLedgerService.releaseEscrowToAvailable(any(), any(), any(), any(), any(), any(), any(WalletLedgerService.WalletOperationContext.class))).thenReturn(tx);
        when(contractDepositRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        paymentWalletService.autoRefundParticipantDeposits(1, 1);

        verify(walletLedgerService, times(1)).releaseEscrowToAvailable(any(), any(), any(), any(), any(), any(), any(WalletLedgerService.WalletOperationContext.class));
        assertEquals("REFUNDED", expDeposit.getStatus());
        assertEquals("REFUNDED", bizDeposit.getStatus());
    }

    @Test
    void refundParticipantDeposits_shouldDelegateToInternalAndRequireAdmin() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5)
                .status(ContractEntity.STATUS_COMPLETED).build();
        ContractDepositEntity bizDeposit = ContractDepositEntity.builder()
                .depositId(1L).contractId(1).ownerRole("BUSINESS").status("HELD").ownerAccountId(50)
                .heldAmount(BigDecimal.valueOf(20_000_000)).refundedAmount(BigDecimal.ZERO).build();
        ContractDepositEntity expDeposit = ContractDepositEntity.builder()
                .depositId(2L).contractId(1).ownerRole("EXPERT").status("HELD").ownerAccountId(60)
                .heldAmount(BigDecimal.valueOf(10_000_000)).refundedAmount(BigDecimal.ZERO).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(1).role(RoleEntity.builder().roleName("ADMIN").build()).build());
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(contractDepositRepository.findByContractIdOrderByOwnerRoleAsc(1))
                .thenReturn(List.of(bizDeposit, expDeposit));
        when(walletLedgerService.releaseEscrowToAvailable(any(), any(), any(), any(), any(), any(), any(WalletLedgerService.WalletOperationContext.class)))
                .thenReturn(WalletTransactionEntity.builder().id(704L).build());
        when(contractDepositRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).accountId(50).build()));
        when(expertProfileRepository.findById(5)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).accountId(60).build()));

        List<ContractDepositEntity> result = paymentWalletService.refundParticipantDeposits(1, null);

        assertEquals(2, result.size());
        assertEquals("REFUNDED", bizDeposit.getStatus());
        assertEquals("REFUNDED", expDeposit.getStatus());
    }
}
