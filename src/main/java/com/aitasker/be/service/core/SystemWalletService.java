/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/SystemWalletService.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
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

// Note: Annotation này cho Spring quản lý class như một service chứa nghiệp vụ.
@Service
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
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

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `getWalletForAdmin` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public SystemWalletEntity getWalletForAdmin() {
        accessService.requireRole("ADMIN");
        syncWallet();
        AccountEntity admin = accountRepository.findFirstByRoleRoleNameOrderByAccountIdAsc("ADMIN")
                .orElseThrow(() -> new NotFoundException("CHUA CO TAI KHOAN ADMIN DE QUAN LY SYSTEM WALLET"));
        return systemWalletRepository.findByAccountId(admin.getAccountId())
                .orElseThrow(() -> new NotFoundException("CHUA CO VI HE THONG"));
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `getCurrentWallet` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    public SystemWalletEntity getCurrentWallet() {
        AccountEntity actor = accessService.currentAccount();
        ensureWallet(actor);
        return systemWalletRepository.findByAccountId(actor.getAccountId())
                .orElseThrow(() -> new NotFoundException("CHUA CO VI CHO TAI KHOAN NAY"));
    }

    // Note: Annotation này đảm bảo các thao tác database trong hàm chạy cùng một transaction.
    @Transactional
    // Note: Hàm `syncWallet` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
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
                wallet.setEscrowBalance(deposited);
                wallet.setAvailableBalance(nonNegativeMoney(wallet.getAvailableBalance()));
                wallet.setCurrentBalance(nonNegativeMoney(wallet.getAvailableBalance()).add(wallet.getEscrowBalance()));
            } else if ("EXPERT".equals(role)) {
                wallet.setEscrowBalance(BigDecimal.ZERO);
                wallet.setAvailableBalance(nonNegativeMoney(wallet.getAvailableBalance()));
                wallet.setCurrentBalance(wallet.getAvailableBalance());
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

    @Transactional
    public SystemWalletEntity ensureWallet(AccountEntity account) {
        return systemWalletRepository.findByAccountId(account.getAccountId())
                .orElseGet(() -> systemWalletRepository.save(SystemWalletEntity.builder()
                        .accountId(account.getAccountId())
                        .roleId(account.getRole().getRoleId())
                        .walletType(walletType(account.getRole().getRoleName()))
                        .currency("VND")
                        .currentBalance(BigDecimal.ZERO)
                        .availableBalance(BigDecimal.ZERO)
                        .escrowBalance(BigDecimal.ZERO)
                        .totalRevenue(BigDecimal.ZERO)
                        .holdingBalance(BigDecimal.ZERO)
                        .disputedBalance(BigDecimal.ZERO)
                        .depositedBusinessCount(0)
                        .successfulDepositCount(0)
                        .lastSyncedAt(LocalDateTime.now())
                        .build()));
    }

    @Transactional
    public SystemWalletEntity ensureWalletByAccountId(Integer accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY TAI KHOAN"));
        return ensureWallet(account);
    }

    // Note: Hàm `resolveLatestTransactionId` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private Long resolveLatestTransactionId() {
        Long latest = transactionRepository.latestTransactionId();
        return latest == null || latest == 0 ? null : latest;
    }

    // Note: Hàm `nonNegativeMoney` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private BigDecimal nonNegativeMoney(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return value;
    }

    // Note: Hàm `walletType` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private String walletType(String role) {
        return switch (role) {
            case "ADMIN" -> "ADMIN_SYSTEM";
            case "BUSINESS" -> "BUSINESS";
            case "EXPERT" -> "EXPERT";
            default -> "STAFF";
        };
    }

    // Note: Hàm `calculateBusinessSuccessfulDeposits` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
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

    // Note: Hàm `calculateExpertSuccessfulPayouts` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
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

    // Note: Hàm `jobIdsByBusiness` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private List<Integer> jobIdsByBusiness(Integer businessId) {
        return contractRepository.findByBusinessId(businessId).stream()
                .map(ContractEntity::getJobId)
                .toList();
    }
}
