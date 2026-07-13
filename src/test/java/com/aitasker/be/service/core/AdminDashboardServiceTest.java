package com.aitasker.be.service.core;

import com.aitasker.be.dto.admin.dashboard.*;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {
    @Mock private AccessService accessService;
    @Mock private AccountRepository accountRepository;
    @Mock private BusinessProfileRepository businessProfileRepository;
    @Mock private ExpertProfileRepository expertProfileRepository;
    @Mock private JobRepository jobRepository;
    @Mock private ProposalRepository proposalRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private DisputeRepository disputeRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private SystemWalletRepository systemWalletRepository;
    @Mock private MembershipPurchaseRepository membershipPurchaseRepository;
    @Mock private MembershipPackageRepository membershipPackageRepository;
    @Mock private WithdrawalRequestRepository withdrawalRequestRepository;

    @InjectMocks private AdminDashboardService service;

    @Test
    void summary_shouldReturnDashboardCards() {
        when(accountRepository.findAll()).thenReturn(List.of(account("BUSINESS"), account("EXPERT"), account("STAFF")));
        when(businessProfileRepository.findAll()).thenReturn(List.of(BusinessProfileEntity.builder().kybStatus("Pending").build()));
        when(expertProfileRepository.findAll()).thenReturn(List.of(ExpertProfileEntity.builder().kycStatus("Approved").build()));
        when(jobRepository.findAll()).thenReturn(List.of(job("OPEN"), job("CLOSED")));
        when(proposalRepository.findAll()).thenReturn(List.of(proposal("Accepted", "100"), proposal("Pending", "200")));
        when(contractRepository.findAll()).thenReturn(List.of(
                contract(ContractEntity.STATUS_ACTIVE),
                contract(ContractEntity.STATUS_COMPLETED),
                contract(ContractEntity.STATUS_CLOSED)
        ));
        when(disputeRepository.findAll()).thenReturn(List.of(
                dispute(DisputeEntity.STATUS_STAFF_REVIEWING, null),
                dispute(DisputeEntity.STATUS_RESOLVED, null)
        ));
        when(walletTransactionRepository.findAll()).thenReturn(List.of(walletTx("CREDIT_PURCHASE", "POSTED", "300")));
        when(membershipPurchaseRepository.findAll()).thenReturn(List.of(purchase(1L, "SUCCESS", "900")));
        when(withdrawalRequestRepository.findAll()).thenReturn(List.of(withdrawal("PENDING", "120")));
        when(systemWalletRepository.findTopByOrderBySystemWalletIdAsc()).thenReturn(Optional.of(SystemWalletEntity.builder()
                .availableBalance(new BigDecimal("1000"))
                .escrowBalance(new BigDecimal("500"))
                .build()));

        DashboardSummaryResponse result = service.summary();

        assertEquals(3, result.getTotalUsers());
        assertEquals(1, result.getBusinessUsers());
        assertEquals(1, result.getPendingProfileReviews());
        assertEquals(2, result.getTotalJobs());
        assertEquals(1, result.getOpenJobs());
        assertEquals(2, result.getTotalProposals());
        assertEquals(1, result.getAcceptedProposals());
        assertEquals(3, result.getTotalContracts());
        assertEquals(1, result.getActiveContracts());
        assertEquals(2, result.getTotalDisputes());
        assertEquals(1, result.getOpenDisputes());
        assertEquals(new BigDecimal("900"), result.getTotalMembershipRevenue());
        assertEquals(new BigDecimal("300"), result.getGrossTransactionVolume());
        assertEquals(new BigDecimal("120"), result.getPendingWithdrawalAmount());
        verify(accessService).requireRole("ADMIN");
    }

    @Test
    void revenue_shouldReturnSeriesAndBreakdown() {
        LocalDateTime jan = LocalDateTime.of(2026, 1, 5, 9, 0);
        LocalDateTime feb = LocalDateTime.of(2026, 2, 5, 9, 0);
        when(walletTransactionRepository.findAll()).thenReturn(List.of(
                walletTx("CREDIT_PURCHASE", "POSTED", "100", jan),
                walletTx("MEMBERSHIP_PURCHASE", "POSTED", "200", jan),
                walletTx("WITHDRAW_APPROVED", "POSTED", "50", feb),
                walletTx("IGNORED", "FAILED", "999", feb)
        ));

        DashboardSeriesResponse result = service.revenue(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 28),
                "month");

        assertEquals(new BigDecimal("350"), result.getTotalAmount());
        assertEquals(3, result.getTotalCount());
        assertEquals(2, result.getSeries().size());
        assertEquals("2026-01", result.getSeries().get(0).getPeriod());
        assertEquals(new BigDecimal("300"), result.getSeries().get(0).getAmount());
        assertEquals(3, result.getBreakdown().size());
        verify(accessService).requireRole("ADMIN");
    }

    @Test
    void contracts_shouldReturnStatusBreakdownAndTrend() {
        when(contractRepository.findAll()).thenReturn(List.of(
                contract(ContractEntity.STATUS_ACTIVE, LocalDateTime.of(2026, 1, 1, 0, 0)),
                contract(ContractEntity.STATUS_ACTIVE, LocalDateTime.of(2026, 1, 2, 0, 0)),
                contract(ContractEntity.STATUS_TERMINATED, LocalDateTime.of(2026, 2, 1, 0, 0))
        ));

        DashboardContractsResponse result = service.contracts(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 28),
                "month");

        assertEquals(3, result.getTotalContracts());
        assertEquals(2, result.getActiveContracts());
        assertEquals(1, result.getTerminatedContracts());
        assertEquals(2, result.getStatusBreakdown().size());
        assertEquals(2, result.getCreatedTrend().size());
    }

    @Test
    void users_shouldReturnRoleStatusAndPendingProfileBacklog() {
        when(accountRepository.findAll()).thenReturn(List.of(
                account("BUSINESS", "Approved", LocalDateTime.of(2026, 1, 1, 0, 0)),
                account("EXPERT", "Pending", LocalDateTime.of(2026, 1, 2, 0, 0))
        ));
        when(businessProfileRepository.findAll()).thenReturn(List.of(BusinessProfileEntity.builder().kybStatus("Pending").build()));
        when(expertProfileRepository.findAll()).thenReturn(List.of(ExpertProfileEntity.builder().kycStatus("Pending").build()));

        DashboardUsersResponse result = service.users(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                "day");

        assertEquals(2, result.getTotalUsers());
        assertEquals(2, result.getPendingProfileReviews());
        assertEquals(2, result.getRoleBreakdown().size());
        assertEquals(2, result.getStatusBreakdown().size());
        assertEquals(2, result.getNewUsersTrend().size());
    }

    @Test
    void jobsProposals_shouldReturnMarketplaceFunnel() {
        when(jobRepository.findAll()).thenReturn(List.of(job("OPEN"), job("CLOSED")));
        when(proposalRepository.findAll()).thenReturn(List.of(proposal("Accepted", "100"), proposal("Rejected", "50")));

        DashboardJobsProposalsResponse result = service.jobsProposals(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "month");

        assertEquals(2, result.getTotalJobs());
        assertEquals(1, result.getOpenJobs());
        assertEquals(2, result.getTotalProposals());
        assertEquals(1, result.getAcceptedProposals());
        assertEquals(new BigDecimal("50.00"), result.getProposalAcceptanceRatePercent());
    }

    @Test
    void disputes_shouldReturnOpenAndOverdueSlaCounts() {
        when(disputeRepository.findAll()).thenReturn(List.of(
                dispute(DisputeEntity.STATUS_STAFF_REVIEWING, LocalDateTime.now().minusDays(1)),
                dispute(DisputeEntity.STATUS_RESOLVED, null)
        ));

        DashboardDisputesResponse result = service.disputes(
                LocalDate.now().minusDays(7),
                LocalDate.now(),
                "week");

        assertEquals(2, result.getTotalDisputes());
        assertEquals(1, result.getOpenDisputes());
        assertEquals(1, result.getResolvedDisputes());
        assertEquals(1, result.getOverdueStaffSlaDisputes());
    }

    @Test
    void membership_shouldReturnPackageRevenueBreakdown() {
        when(membershipPackageRepository.findAll()).thenReturn(List.of(MembershipPackageEntity.builder()
                .packageId(1L)
                .packageCode("BUSINESS_PLUS")
                .build()));
        when(membershipPurchaseRepository.findAll()).thenReturn(List.of(
                purchase(1L, "SUCCESS", "100"),
                purchase(1L, "FAILED", "999")
        ));

        DashboardMembershipResponse result = service.membership(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "month");

        assertEquals(1, result.getTotalPurchases());
        assertEquals(new BigDecimal("100"), result.getTotalRevenue());
        assertEquals("BUSINESS_PLUS", result.getPackageBreakdown().get(0).getKey());
    }

    @Test
    void financeBreakdown_shouldReturnWalletAndWithdrawalTotals() {
        when(walletTransactionRepository.findAll()).thenReturn(List.of(walletTx("CREDIT_PURCHASE", "POSTED", "100")));
        when(withdrawalRequestRepository.findAll()).thenReturn(List.of(
                withdrawal("PENDING", "30"),
                withdrawal("APPROVED", "70")
        ));
        when(systemWalletRepository.findTopByOrderBySystemWalletIdAsc()).thenReturn(Optional.of(SystemWalletEntity.builder()
                .currentBalance(new BigDecimal("1000"))
                .availableBalance(new BigDecimal("800"))
                .escrowBalance(new BigDecimal("200"))
                .totalRevenue(new BigDecimal("150"))
                .build()));

        DashboardFinanceBreakdownResponse result = service.financeBreakdown(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31));

        assertEquals(new BigDecimal("1000"), result.getSystemCurrentBalance());
        assertEquals(new BigDecimal("30"), result.getPendingWithdrawalAmount());
        assertEquals(new BigDecimal("70"), result.getApprovedWithdrawalAmount());
        assertEquals(new BigDecimal("100"), result.getGrossTransactionVolume());
    }

    private AccountEntity account(String role) {
        return account(role, "Approved", LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    private AccountEntity account(String role, String status, LocalDateTime createdAt) {
        return AccountEntity.builder()
                .role(RoleEntity.builder().roleName(role).build())
                .status(status)
                .createdAt(createdAt)
                .build();
    }

    private JobEntity job(String status) {
        return JobEntity.builder()
                .status(status)
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
    }

    private ProposalEntity proposal(String status, String bidAmount) {
        return ProposalEntity.builder()
                .status(status)
                .bidAmount(new BigDecimal(bidAmount))
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
    }

    private ContractEntity contract(String status) {
        return contract(status, LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    private ContractEntity contract(String status, LocalDateTime createdAt) {
        return ContractEntity.builder()
                .status(status)
                .createdAt(createdAt)
                .build();
    }

    private DisputeEntity dispute(String status, LocalDateTime staffSlaDueAt) {
        return DisputeEntity.builder()
                .status(status)
                .staffSlaDueAt(staffSlaDueAt)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    private WalletTransactionEntity walletTx(String type, String status, String amount) {
        return walletTx(type, status, amount, LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    private WalletTransactionEntity walletTx(String type, String status, String amount, LocalDateTime createdAt) {
        return WalletTransactionEntity.builder()
                .transactionType(type)
                .status(status)
                .amount(new BigDecimal(amount))
                .createdAt(createdAt)
                .build();
    }

    private MembershipPurchaseEntity purchase(Long packageId, String status, String amount) {
        return MembershipPurchaseEntity.builder()
                .packageId(packageId)
                .status(status)
                .amount(new BigDecimal(amount))
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
    }

    private WithdrawalRequestEntity withdrawal(String status, String amount) {
        return WithdrawalRequestEntity.builder()
                .status(status)
                .amount(new BigDecimal(amount))
                .requestedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
    }
}
