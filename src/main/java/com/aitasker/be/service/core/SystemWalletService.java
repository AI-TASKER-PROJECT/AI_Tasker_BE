package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemWalletService {
    private final AccessService accessService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SystemWalletRepository systemWalletRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final MilestoneRepository milestoneRepository;
    private final ContractRepository contractRepository;

    @Transactional
    public SystemWalletEntity getWalletForAdmin() {
        accessService.requireRole("ADMIN");
        syncWallet();
        AccountEntity admin = accountRepository.findFirstByRoleRoleNameOrderByAccountIdAsc("ADMIN")
                .orElseThrow(() -> new NotFoundException("CHUA CO TAI KHOAN ADMIN DE QUAN LY SYSTEM WALLET"));
        return systemWalletRepository.findByAccountId(admin.getAccountId())
                .orElseThrow(() -> new NotFoundException("CHUA CO VI HE THONG"));
    }

    @Transactional
    public SystemWalletEntity getCurrentWallet() {
        syncWallet();
        AccountEntity actor = accessService.currentAccount();
        return systemWalletRepository.findByAccountId(actor.getAccountId())
                .orElseThrow(() -> new NotFoundException("CHUA CO VI CHO TAI KHOAN NAY"));
    }

    @Transactional
    public SystemWalletEntity syncWallet() {
        AccountEntity admin = accountRepository.findFirstByRoleRoleNameOrderByAccountIdAsc("ADMIN")
                .orElseThrow(() -> new NotFoundException("CHUA CO TAI KHOAN ADMIN DE QUAN LY SYSTEM WALLET"));
        Long latestTransactionId = resolveLatestTransactionId();
        BigDecimal totalRevenue = nonNegativeMoney(transactionRepository.sumSuccessfulCommissionFee());
        BigDecimal holdingBalance = nonNegativeMoney(transactionRepository.calculateHoldingBalance());
        BigDecimal disputedBalance = nonNegativeMoney(transactionRepository.calculateDisputedBalance());

        for (AccountEntity account : accountRepository.findAll()) {
            SystemWalletEntity wallet = systemWalletRepository.findByAccountId(account.getAccountId())
                    .orElseGet(() -> SystemWalletEntity.builder()
                            .accountId(account.getAccountId())
                            .currency("VND")
                            .currentBalance(BigDecimal.ZERO)
                            .availableBalance(BigDecimal.ZERO)
                            .escrowBalance(BigDecimal.ZERO)
                            .totalRevenue(BigDecimal.ZERO)
                            .holdingBalance(BigDecimal.ZERO)
                            .disputedBalance(BigDecimal.ZERO)
                            .depositedBusinessCount(0)
                            .successfulDepositCount(0)
                            .build());

            String role = account.getRole().getRoleName();
            BigDecimal previousCurrentBalance = nonNegativeMoney(wallet.getCurrentBalance());
            BigDecimal previousEscrowBalance = nonNegativeMoney(wallet.getEscrowBalance());
            wallet.setRoleId(account.getRole().getRoleId());
            wallet.setWalletType(walletType(role));
            wallet.setTransactionId(latestTransactionId);
            wallet.setCurrency(wallet.getCurrency() == null || wallet.getCurrency().isBlank() ? "VND" : wallet.getCurrency());
            wallet.setDepositedBusinessCount(0);
            wallet.setSuccessfulDepositCount(0);
            wallet.setTotalRevenue(BigDecimal.ZERO);
            wallet.setHoldingBalance(BigDecimal.ZERO);
            wallet.setDisputedBalance(BigDecimal.ZERO);
            wallet.setEscrowBalance(BigDecimal.ZERO);

            if ("ADMIN".equals(role)) {
                wallet.setDepositedBusinessCount(Math.toIntExact(transactionRepository.countDepositedBusinesses()));
                wallet.setSuccessfulDepositCount(Math.toIntExact(transactionRepository.countByStatusAndTransactionType("Success", "Deposit")));
                wallet.setTotalRevenue(totalRevenue);
                wallet.setHoldingBalance(holdingBalance);
                wallet.setDisputedBalance(disputedBalance);
                wallet.setEscrowBalance(holdingBalance);
                wallet.setAvailableBalance(totalRevenue);
                wallet.setCurrentBalance(totalRevenue.add(holdingBalance));
            } else if ("BUSINESS".equals(role)) {
                BigDecimal deposited = calculateBusinessSuccessfulDeposits(account.getAccountId());
                BigDecimal initialBalance = previousCurrentBalance.add(previousEscrowBalance);
                if (initialBalance.signum() == 0) {
                    initialBalance = BigDecimal.valueOf(200_000_000L);
                }
                wallet.setEscrowBalance(deposited);
                wallet.setCurrentBalance(nonNegativeMoney(initialBalance.subtract(deposited)));
                wallet.setAvailableBalance(wallet.getCurrentBalance());
            } else if ("EXPERT".equals(role)) {
                BigDecimal payout = calculateExpertSuccessfulPayouts(account.getAccountId());
                wallet.setCurrentBalance(payout);
                wallet.setAvailableBalance(payout);
            } else {
                wallet.setCurrentBalance(nonNegativeMoney(wallet.getCurrentBalance()));
                wallet.setAvailableBalance(nonNegativeMoney(wallet.getAvailableBalance()));
            }

            wallet.setLastSyncedAt(LocalDateTime.now());
            systemWalletRepository.save(wallet);
        }

        return systemWalletRepository.findByAccountId(admin.getAccountId())
                .orElseThrow(() -> new NotFoundException("CHUA CO VI HE THONG"));
    }

    private Long resolveLatestTransactionId() {
        Long latest = transactionRepository.latestTransactionId();
        return latest == null || latest == 0 ? null : latest;
    }

    private BigDecimal nonNegativeMoney(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return value;
    }

    private String walletType(String role) {
        return switch (role) {
            case "ADMIN" -> "ADMIN_SYSTEM";
            case "BUSINESS" -> "BUSINESS";
            case "EXPERT" -> "EXPERT";
            default -> "STAFF";
        };
    }

    private BigDecimal calculateBusinessSuccessfulDeposits(Integer accountId) {
        Integer businessId = businessProfileRepository.findByAccountId(accountId)
                .map(BusinessProfileEntity::getBusinessId)
                .orElse(null);
        if (businessId == null) return BigDecimal.ZERO;
        List<Integer> jobIds = jobIdsByBusiness(businessId);
        return transactionRepository.findAll().stream()
                .filter(tx -> "Success".equals(tx.getStatus()) && "Deposit".equals(tx.getTransactionType()))
                .filter(tx -> milestoneRepository.findById(tx.getMilestoneId())
                        .map(MilestoneEntity::getJobId)
                        .filter(jobIds::contains)
                        .isPresent())
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateExpertSuccessfulPayouts(Integer accountId) {
        Integer expertId = expertProfileRepository.findByAccountId(accountId)
                .map(ExpertProfileEntity::getExpertId)
                .orElse(null);
        if (expertId == null) return BigDecimal.ZERO;
        List<Integer> jobIds = contractRepository.findByExpertId(expertId).stream()
                .map(ContractEntity::getJobId)
                .toList();
        return transactionRepository.findAll().stream()
                .filter(tx -> "Success".equals(tx.getStatus()) && "Payout".equals(tx.getTransactionType()))
                .filter(tx -> milestoneRepository.findById(tx.getMilestoneId())
                        .map(MilestoneEntity::getJobId)
                        .filter(jobIds::contains)
                        .isPresent())
                .map(TransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Integer> jobIdsByBusiness(Integer businessId) {
        return contractRepository.findByBusinessId(businessId).stream()
                .map(ContractEntity::getJobId)
                .toList();
    }
}
