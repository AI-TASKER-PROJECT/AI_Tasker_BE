package com.aitasker.be.service.core;

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
import com.aitasker.be.entity.RoleEntity;
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
import com.aitasker.be.repository.UserQuotaRepository;
import com.aitasker.be.repository.WalletTransactionRepository;
import com.aitasker.be.repository.WithdrawalRequestRepository;
import com.aitasker.be.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    @Mock private WithdrawalRequestRepository withdrawalRequestRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private PaymentWalletService paymentWalletService;

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
        when(walletLedgerService.debitAvailable(any(), any(), any(), any(), any(), any()))
                .thenReturn(WalletTransactionEntity.builder().id(100L).build());
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
        when(walletLedgerService.debitAvailable(any(), any(), any(), any(), any(), any()))
                .thenReturn(WalletTransactionEntity.builder().id(100L).build());
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
        when(contractDepositRepository.findByContractIdAndOwnerRole(30, "BUSINESS")).thenReturn(Optional.empty());
        when(walletLedgerService.availableBalance(10)).thenReturn(new BigDecimal("300000"));
        when(walletLedgerService.holdEscrowFromAvailable(any(), any(), any(), any(), any(), any()))
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
        when(withdrawalRequestRepository.findById(90L)).thenReturn(Optional.of(withdrawal));
        when(walletLedgerService.releaseHoldingToAvailable(any(), any(), any(), any(), any(), any()))
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
        when(walletLedgerService.holdWithdrawalFromAvailable(any(), any(), any(), any(), any(), any()))
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
        when(withdrawalRequestRepository.findById(90L)).thenReturn(Optional.of(withdrawal));
        when(walletLedgerService.debitHolding(any(), any(), any(), any(), any(), any()))
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
                .thenReturn(List.of(skippedWithdrawDebit, withdrawHold, creditPurchase, membershipPurchase));
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
        assertEquals("Doanh nghiệp A đã mua 10 lượt đăng job", history.get(1).getTitle());
        assertEquals("Doanh nghiệp A thanh toán 100000 VND để mua 10 lượt đăng job.", history.get(1).getDescription());
        assertEquals("Doanh nghiệp A đã mua gói Premium Business", history.get(2).getTitle());
        assertEquals("Doanh nghiệp A thanh toán 500000 VND để mua gói Premium Business. Thời hạn từ 2026-06-27T00:00 đến 2026-07-27T00:00.",
                history.get(2).getDescription());
    }

    private AccountEntity businessAccount() {
        return AccountEntity.builder()
                .accountId(10)
                .role(RoleEntity.builder().roleName("BUSINESS").build())
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
        when(walletLedgerService.releaseEscrowToAvailable(any(), any(), any(), any(), any(), any()))
                .thenReturn(bizTx, expTx);
        when(contractDepositRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<ContractDepositEntity> result = paymentWalletService.autoRefundParticipantDeposits(1, 1);

        assertEquals(2, result.size());
        assertEquals("REFUNDED", bizDeposit.getStatus());
        assertEquals("REFUNDED", expDeposit.getStatus());
        assertEquals("STANDARD_REFUND", bizDeposit.getResolutionType());
        assertNotNull(bizDeposit.getResolvedAt());
        verify(walletLedgerService, times(2)).releaseEscrowToAvailable(any(), any(), any(), any(), any(), any());
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
        when(walletLedgerService.releaseEscrowToAvailable(any(), any(), any(), any(), any(), any())).thenReturn(tx);
        when(contractDepositRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        paymentWalletService.autoRefundParticipantDeposits(1, 1);

        verify(walletLedgerService, times(1)).releaseEscrowToAvailable(any(), any(), any(), any(), any(), any());
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
        when(walletLedgerService.releaseEscrowToAvailable(any(), any(), any(), any(), any(), any()))
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
