package com.aitasker.be.service.core;

import com.aitasker.be.dto.payment.CreditPurchaseRequest;
import com.aitasker.be.dto.payment.PaymentActionResponse;
import com.aitasker.be.dto.payment.WithdrawalReviewRequest;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.entity.ContractDepositEntity;
import com.aitasker.be.entity.ContractEntity;
import com.aitasker.be.entity.ContractMilestoneEntity;
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
import com.aitasker.be.repository.QuotaUsageLogRepository;
import com.aitasker.be.repository.SystemSettingRepository;
import com.aitasker.be.repository.UserQuotaRepository;
import com.aitasker.be.repository.WalletTransactionRepository;
import com.aitasker.be.repository.WithdrawalRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
    @Mock private WithdrawalRequestRepository withdrawalRequestRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;

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
        when(walletLedgerService.availableBalance(10)).thenReturn(new BigDecimal("50000"));

        PaymentActionResponse<UserQuotaEntity> response = paymentWalletService.purchaseJobPostCredits(request);

        assertFalse(response.isCompleted());
        assertTrue(response.isNeedTopup());
        assertEquals(new BigDecimal("200000"), response.getRequiredAmount());
        assertEquals(new BigDecimal("150000"), response.getMissingAmount());
        assertEquals("/api/payments/payos/create", response.getRedirectUrl());
    }

    @Test
    void payContractDeposit_shouldHoldDepositAndActivateContract() {
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
                .status("PendingDeposit")
                .totalBudget(new BigDecimal("1000000"))
                .build();
        ContractMilestoneEntity contractMilestone = ContractMilestoneEntity.builder()
                .jobMilestoneId(60)
                .finalBudget(new BigDecimal("1000000"))
                .build();
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(60).jobId(50).build();
        com.aitasker.be.entity.JobEntity job = com.aitasker.be.entity.JobEntity.builder()
                .jobId(50)
                .status("PROPOSAL_REVIEW")
                .budget(new BigDecimal("800000"))
                .build();

        when(accessService.currentAccount()).thenReturn(businessAccount);
        when(businessProfileRepository.findByAccountId(10)).thenReturn(Optional.of(business));
        when(contractRepository.findById(30)).thenReturn(Optional.of(contract));
        when(contractDepositRepository.findByContractId(30)).thenReturn(Optional.empty());
        when(walletLedgerService.availableBalance(10)).thenReturn(new BigDecimal("300000"));
        when(walletLedgerService.holdEscrowFromAvailable(any(), any(), any(), any(), any(), any()))
                .thenReturn(WalletTransactionEntity.builder().id(70L).build());
        when(contractDepositRepository.save(any(ContractDepositEntity.class))).thenAnswer(invocation -> {
            ContractDepositEntity deposit = invocation.getArgument(0);
            deposit.setDepositId(80L);
            return deposit;
        });
        when(contractRepository.save(any(ContractEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(30)).thenReturn(List.of(contractMilestone));
        when(milestoneRepository.findById(60)).thenReturn(Optional.of(milestone));
        when(jobRepository.findById(50)).thenReturn(Optional.of(job));

        PaymentActionResponse<ContractDepositEntity> response = paymentWalletService.payContractDeposit(30);

        assertTrue(response.isCompleted());
        assertEquals("HELD", response.getData().getStatus());
        assertEquals(new BigDecimal("200000.00"), response.getData().getDepositAmount());
        assertEquals("Active", contract.getStatus());
        assertNotNull(contract.getActivatedAt());
        assertEquals("IN_PROGRESS", job.getStatus());
        assertEquals(new BigDecimal("1000000"), job.getBudget());
        assertEquals(Integer.valueOf(30), milestone.getContractId());
        assertEquals(new BigDecimal("1000000"), milestone.getFundsAllocated());
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
    }
}
