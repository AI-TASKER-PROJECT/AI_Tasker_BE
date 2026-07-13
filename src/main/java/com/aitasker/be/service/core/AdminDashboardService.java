package com.aitasker.be.service.core;

import com.aitasker.be.dto.admin.dashboard.*;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private static final Set<String> OPEN_DISPUTE_STATUSES = Set.of(
            DisputeEntity.STATUS_PENDING_SELF_RESOLVE,
            DisputeEntity.STATUS_ESCALATION_REQUESTED,
            DisputeEntity.STATUS_STAFF_REVIEWING,
            DisputeEntity.STATUS_STAFF_DECIDED
    );

    private final AccessService accessService;
    private final AccountRepository accountRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;
    private final ContractRepository contractRepository;
    private final DisputeRepository disputeRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final SystemWalletRepository systemWalletRepository;
    private final MembershipPurchaseRepository membershipPurchaseRepository;
    private final MembershipPackageRepository membershipPackageRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        accessService.requireRole("ADMIN");
        List<AccountEntity> accounts = accountRepository.findAll();
        List<BusinessProfileEntity> businesses = businessProfileRepository.findAll();
        List<ExpertProfileEntity> experts = expertProfileRepository.findAll();
        List<JobEntity> jobs = jobRepository.findAll();
        List<ProposalEntity> proposals = proposalRepository.findAll();
        List<ContractEntity> contracts = contractRepository.findAll();
        List<DisputeEntity> disputes = disputeRepository.findAll();
        List<WalletTransactionEntity> walletTransactions = walletTransactionRepository.findAll();
        List<MembershipPurchaseEntity> purchases = membershipPurchaseRepository.findAll();
        List<WithdrawalRequestEntity> withdrawals = withdrawalRequestRepository.findAll();
        SystemWalletEntity wallet = systemWalletRepository.findTopByOrderBySystemWalletIdAsc().orElse(null);

        return DashboardSummaryResponse.builder()
                .totalUsers((long) accounts.size())
                .businessUsers(countByRole(accounts, "BUSINESS"))
                .expertUsers(countByRole(accounts, "EXPERT"))
                .staffUsers(countByRole(accounts, "STAFF"))
                .pendingProfileReviews(pendingProfiles(businesses, experts))
                .totalJobs((long) jobs.size())
                .openJobs(countByStatus(jobs, JobEntity::getStatus, "OPEN"))
                .totalProposals((long) proposals.size())
                .acceptedProposals(countByStatus(proposals, ProposalEntity::getStatus, "Accepted"))
                .totalContracts((long) contracts.size())
                .activeContracts(countByStatus(contracts, ContractEntity::getStatus, ContractEntity.STATUS_ACTIVE))
                .completedContracts(countByStatus(contracts, ContractEntity::getStatus, ContractEntity.STATUS_COMPLETED))
                .closedContracts(countByStatus(contracts, ContractEntity::getStatus, ContractEntity.STATUS_CLOSED))
                .terminatedContracts(countByStatus(contracts, ContractEntity::getStatus, ContractEntity.STATUS_TERMINATED))
                .totalDisputes((long) disputes.size())
                .openDisputes(disputes.stream().filter(d -> OPEN_DISPUTE_STATUSES.contains(safe(d.getStatus()))).count())
                .totalMembershipPurchases(purchases.stream().filter(this::successfulPurchase).count())
                .totalMembershipRevenue(sum(purchases.stream()
                        .filter(this::successfulPurchase)
                        .map(MembershipPurchaseEntity::getAmount)
                        .toList()))
                .grossTransactionVolume(sum(walletTransactions.stream()
                        .filter(this::posted)
                        .map(WalletTransactionEntity::getAmount)
                        .toList()))
                .systemAvailableBalance(wallet == null ? BigDecimal.ZERO : money(wallet.getAvailableBalance()))
                .systemEscrowBalance(wallet == null ? BigDecimal.ZERO : money(wallet.getEscrowBalance()))
                .pendingWithdrawalAmount(sum(withdrawals.stream()
                        .filter(w -> "PENDING".equals(safe(w.getStatus())))
                        .map(WithdrawalRequestEntity::getAmount)
                        .toList()))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardSeriesResponse revenue(LocalDate from, LocalDate to, String groupBy) {
        accessService.requireRole("ADMIN");
        Range range = range(from, to, groupBy);
        List<WalletTransactionEntity> transactions = walletTransactionRepository.findAll().stream()
                .filter(this::posted)
                .filter(tx -> inRange(tx.getCreatedAt(), range))
                .toList();
        return DashboardSeriesResponse.builder()
                .from(range.from())
                .to(range.to())
                .groupBy(range.groupBy())
                .totalAmount(sum(transactions.stream().map(WalletTransactionEntity::getAmount).toList()))
                .totalCount((long) transactions.size())
                .series(series(transactions, WalletTransactionEntity::getCreatedAt, WalletTransactionEntity::getAmount, range))
                .breakdown(breakdownByAmount(transactions, WalletTransactionEntity::getTransactionType, WalletTransactionEntity::getAmount))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardContractsResponse contracts(LocalDate from, LocalDate to, String groupBy) {
        accessService.requireRole("ADMIN");
        Range range = range(from, to, groupBy);
        List<ContractEntity> all = contractRepository.findAll();
        List<ContractEntity> ranged = all.stream().filter(c -> inRange(c.getCreatedAt(), range)).toList();
        return DashboardContractsResponse.builder()
                .from(range.from())
                .to(range.to())
                .groupBy(range.groupBy())
                .totalContracts((long) all.size())
                .activeContracts(countByStatus(all, ContractEntity::getStatus, ContractEntity.STATUS_ACTIVE))
                .completedContracts(countByStatus(all, ContractEntity::getStatus, ContractEntity.STATUS_COMPLETED))
                .closedContracts(countByStatus(all, ContractEntity::getStatus, ContractEntity.STATUS_CLOSED))
                .terminatedContracts(countByStatus(all, ContractEntity::getStatus, ContractEntity.STATUS_TERMINATED))
                .statusBreakdown(breakdownByCount(all, ContractEntity::getStatus))
                .createdTrend(series(ranged, ContractEntity::getCreatedAt, item -> BigDecimal.ZERO, range))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardUsersResponse users(LocalDate from, LocalDate to, String groupBy) {
        accessService.requireRole("ADMIN");
        Range range = range(from, to, groupBy);
        List<AccountEntity> accounts = accountRepository.findAll();
        List<BusinessProfileEntity> businesses = businessProfileRepository.findAll();
        List<ExpertProfileEntity> experts = expertProfileRepository.findAll();
        return DashboardUsersResponse.builder()
                .from(range.from())
                .to(range.to())
                .groupBy(range.groupBy())
                .totalUsers((long) accounts.size())
                .pendingProfileReviews(pendingProfiles(businesses, experts))
                .roleBreakdown(breakdownByCount(accounts, account -> account.getRole() == null ? "UNKNOWN" : account.getRole().getRoleName()))
                .statusBreakdown(breakdownByCount(accounts, AccountEntity::getStatus))
                .newUsersTrend(series(accounts.stream().filter(a -> inRange(a.getCreatedAt(), range)).toList(),
                        AccountEntity::getCreatedAt, item -> BigDecimal.ZERO, range))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardJobsProposalsResponse jobsProposals(LocalDate from, LocalDate to, String groupBy) {
        accessService.requireRole("ADMIN");
        Range range = range(from, to, groupBy);
        List<JobEntity> jobs = jobRepository.findAll();
        List<ProposalEntity> proposals = proposalRepository.findAll();
        long accepted = countByStatus(proposals, ProposalEntity::getStatus, "Accepted");
        BigDecimal acceptanceRate = proposals.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(accepted * 100.0 / proposals.size()).setScale(2, RoundingMode.HALF_UP);
        return DashboardJobsProposalsResponse.builder()
                .from(range.from())
                .to(range.to())
                .groupBy(range.groupBy())
                .totalJobs((long) jobs.size())
                .openJobs(countByStatus(jobs, JobEntity::getStatus, "OPEN"))
                .totalProposals((long) proposals.size())
                .acceptedProposals(accepted)
                .proposalAcceptanceRatePercent(acceptanceRate)
                .jobStatusBreakdown(breakdownByCount(jobs, JobEntity::getStatus))
                .proposalStatusBreakdown(breakdownByCount(proposals, ProposalEntity::getStatus))
                .jobCreatedTrend(series(jobs.stream().filter(j -> inRange(j.getCreatedAt(), range)).toList(),
                        JobEntity::getCreatedAt, item -> BigDecimal.ZERO, range))
                .proposalCreatedTrend(series(proposals.stream().filter(p -> inRange(p.getCreatedAt(), range)).toList(),
                        ProposalEntity::getCreatedAt, ProposalEntity::getBidAmount, range))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardDisputesResponse disputes(LocalDate from, LocalDate to, String groupBy) {
        accessService.requireRole("ADMIN");
        Range range = range(from, to, groupBy);
        LocalDateTime now = LocalDateTime.now();
        List<DisputeEntity> disputes = disputeRepository.findAll();
        return DashboardDisputesResponse.builder()
                .from(range.from())
                .to(range.to())
                .groupBy(range.groupBy())
                .totalDisputes((long) disputes.size())
                .openDisputes(disputes.stream().filter(d -> OPEN_DISPUTE_STATUSES.contains(safe(d.getStatus()))).count())
                .resolvedDisputes(countByStatus(disputes, DisputeEntity::getStatus, DisputeEntity.STATUS_RESOLVED))
                .overdueStaffSlaDisputes(disputes.stream()
                        .filter(d -> d.getStaffSlaDueAt() != null && d.getStaffSlaDueAt().isBefore(now))
                        .filter(d -> d.getStaffDecidedAt() == null)
                        .count())
                .statusBreakdown(breakdownByCount(disputes, DisputeEntity::getStatus))
                .createdTrend(series(disputes.stream().filter(d -> inRange(d.getCreatedAt(), range)).toList(),
                        DisputeEntity::getCreatedAt, item -> BigDecimal.ZERO, range))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardMembershipResponse membership(LocalDate from, LocalDate to, String groupBy) {
        accessService.requireRole("ADMIN");
        Range range = range(from, to, groupBy);
        Map<Long, MembershipPackageEntity> packages = membershipPackageRepository.findAll().stream()
                .collect(Collectors.toMap(MembershipPackageEntity::getPackageId, Function.identity()));
        List<MembershipPurchaseEntity> purchases = membershipPurchaseRepository.findAll().stream()
                .filter(this::successfulPurchase)
                .filter(p -> inRange(p.getCreatedAt(), range))
                .toList();
        return DashboardMembershipResponse.builder()
                .from(range.from())
                .to(range.to())
                .groupBy(range.groupBy())
                .totalPurchases((long) purchases.size())
                .totalRevenue(sum(purchases.stream().map(MembershipPurchaseEntity::getAmount).toList()))
                .packageBreakdown(breakdownByAmount(purchases,
                        purchase -> packageLabel(packages.get(purchase.getPackageId()), purchase.getPackageId()),
                        MembershipPurchaseEntity::getAmount))
                .purchaseTrend(series(purchases, MembershipPurchaseEntity::getCreatedAt, MembershipPurchaseEntity::getAmount, range))
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardFinanceBreakdownResponse financeBreakdown(LocalDate from, LocalDate to) {
        accessService.requireRole("ADMIN");
        Range range = range(from, to, "month");
        List<WalletTransactionEntity> transactions = walletTransactionRepository.findAll().stream()
                .filter(this::posted)
                .filter(tx -> inRange(tx.getCreatedAt(), range))
                .toList();
        List<WithdrawalRequestEntity> withdrawals = withdrawalRequestRepository.findAll().stream()
                .filter(w -> inRange(firstNonNull(w.getRequestedAt(), w.getCreatedAt()), range))
                .toList();
        SystemWalletEntity wallet = systemWalletRepository.findTopByOrderBySystemWalletIdAsc().orElse(null);
        return DashboardFinanceBreakdownResponse.builder()
                .from(range.from())
                .to(range.to())
                .systemCurrentBalance(wallet == null ? BigDecimal.ZERO : money(wallet.getCurrentBalance()))
                .systemAvailableBalance(wallet == null ? BigDecimal.ZERO : money(wallet.getAvailableBalance()))
                .systemEscrowBalance(wallet == null ? BigDecimal.ZERO : money(wallet.getEscrowBalance()))
                .systemTotalRevenue(wallet == null ? BigDecimal.ZERO : money(wallet.getTotalRevenue()))
                .pendingWithdrawalAmount(sum(withdrawals.stream()
                        .filter(w -> "PENDING".equals(safe(w.getStatus())))
                        .map(WithdrawalRequestEntity::getAmount)
                        .toList()))
                .approvedWithdrawalAmount(sum(withdrawals.stream()
                        .filter(w -> "APPROVED".equals(safe(w.getStatus())))
                        .map(WithdrawalRequestEntity::getAmount)
                        .toList()))
                .grossTransactionVolume(sum(transactions.stream().map(WalletTransactionEntity::getAmount).toList()))
                .transactionTypeBreakdown(breakdownByAmount(transactions, WalletTransactionEntity::getTransactionType, WalletTransactionEntity::getAmount))
                .withdrawalStatusBreakdown(breakdownByAmount(withdrawals, WithdrawalRequestEntity::getStatus, WithdrawalRequestEntity::getAmount))
                .build();
    }

    private Range range(LocalDate from, LocalDate to, String groupBy) {
        LocalDate today = LocalDate.now();
        LocalDate normalizedTo = to == null ? today : to;
        LocalDate normalizedFrom = from == null ? normalizedTo.minusMonths(6).plusDays(1) : from;
        if (normalizedFrom.isAfter(normalizedTo)) {
            LocalDate temp = normalizedFrom;
            normalizedFrom = normalizedTo;
            normalizedTo = temp;
        }
        return new Range(normalizedFrom, normalizedTo, normalizeGroupBy(groupBy));
    }

    private String normalizeGroupBy(String groupBy) {
        if (groupBy == null || groupBy.isBlank()) return "month";
        String normalized = groupBy.trim().toLowerCase(Locale.ROOT);
        if (!Set.of("day", "week", "month").contains(normalized)) return "month";
        return normalized;
    }

    private boolean inRange(LocalDateTime value, Range range) {
        if (value == null) return false;
        LocalDate date = value.toLocalDate();
        return !date.isBefore(range.from()) && !date.isAfter(range.to());
    }

    private <T> List<DashboardTimeSeriesPoint> series(
            List<T> items,
            Function<T, LocalDateTime> timeGetter,
            Function<T, BigDecimal> amountGetter,
            Range range
    ) {
        Map<LocalDate, List<T>> grouped = items.stream()
                .filter(item -> timeGetter.apply(item) != null)
                .collect(Collectors.groupingBy(item -> periodStart(timeGetter.apply(item).toLocalDate(), range.groupBy()),
                        TreeMap::new,
                        Collectors.toList()));
        List<DashboardTimeSeriesPoint> result = new ArrayList<>();
        for (Map.Entry<LocalDate, List<T>> entry : grouped.entrySet()) {
            result.add(DashboardTimeSeriesPoint.builder()
                    .period(periodLabel(entry.getKey(), range.groupBy()))
                    .periodStart(entry.getKey())
                    .count((long) entry.getValue().size())
                    .amount(sum(entry.getValue().stream().map(amountGetter).toList()))
                    .build());
        }
        return result;
    }

    private <T> List<DashboardBreakdownItem> breakdownByCount(List<T> items, Function<T, String> keyGetter) {
        return items.stream()
                .collect(Collectors.groupingBy(item -> normalizeKey(keyGetter.apply(item)), TreeMap::new, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> DashboardBreakdownItem.builder()
                        .key(entry.getKey())
                        .label(entry.getKey())
                        .count(entry.getValue())
                        .amount(BigDecimal.ZERO)
                        .build())
                .toList();
    }

    private <T> List<DashboardBreakdownItem> breakdownByAmount(
            List<T> items,
            Function<T, String> keyGetter,
            Function<T, BigDecimal> amountGetter
    ) {
        Map<String, List<T>> grouped = items.stream()
                .collect(Collectors.groupingBy(item -> normalizeKey(keyGetter.apply(item)), TreeMap::new, Collectors.toList()));
        return grouped.entrySet().stream()
                .map(entry -> DashboardBreakdownItem.builder()
                        .key(entry.getKey())
                        .label(entry.getKey())
                        .count((long) entry.getValue().size())
                        .amount(sum(entry.getValue().stream().map(amountGetter).toList()))
                        .build())
                .toList();
    }

    private LocalDate periodStart(LocalDate date, String groupBy) {
        return switch (groupBy) {
            case "day" -> date;
            case "week" -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            default -> date.withDayOfMonth(1);
        };
    }

    private String periodLabel(LocalDate date, String groupBy) {
        return switch (groupBy) {
            case "day" -> date.toString();
            case "week" -> date + "_week";
            default -> String.format("%04d-%02d", date.getYear(), date.getMonthValue());
        };
    }

    private long pendingProfiles(List<BusinessProfileEntity> businesses, List<ExpertProfileEntity> experts) {
        return businesses.stream().filter(profile -> "Pending".equals(safe(profile.getKybStatus()))).count()
                + experts.stream().filter(profile -> "Pending".equals(safe(profile.getKycStatus()))).count();
    }

    private long countByRole(List<AccountEntity> accounts, String role) {
        return accounts.stream()
                .filter(account -> account.getRole() != null && role.equalsIgnoreCase(safe(account.getRole().getRoleName())))
                .count();
    }

    private <T> long countByStatus(List<T> items, Function<T, String> statusGetter, String status) {
        return items.stream().filter(item -> status.equals(safe(statusGetter.apply(item)))).count();
    }

    private boolean successfulPurchase(MembershipPurchaseEntity purchase) {
        return "SUCCESS".equals(safe(purchase.getStatus()));
    }

    private boolean posted(WalletTransactionEntity tx) {
        return WalletTransactionEntity.STATUS_POSTED.equals(safe(tx.getStatus()));
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String normalizeKey(String value) {
        String normalized = safe(value);
        return normalized.isBlank() ? "UNKNOWN" : normalized;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String packageLabel(MembershipPackageEntity membershipPackage, Long packageId) {
        if (membershipPackage == null) return "PACKAGE_" + packageId;
        if (membershipPackage.getPackageCode() != null && !membershipPackage.getPackageCode().isBlank()) {
            return membershipPackage.getPackageCode();
        }
        return membershipPackage.getPackageName() == null ? "PACKAGE_" + packageId : membershipPackage.getPackageName();
    }

    private LocalDateTime firstNonNull(LocalDateTime first, LocalDateTime second) {
        return first != null ? first : second;
    }

    private record Range(LocalDate from, LocalDate to, String groupBy) {}
}
