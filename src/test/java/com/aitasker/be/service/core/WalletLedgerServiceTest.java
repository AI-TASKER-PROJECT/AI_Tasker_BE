package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.entity.SystemWalletEntity;
import com.aitasker.be.entity.WalletTransactionEntity;
import com.aitasker.be.repository.SystemWalletRepository;
import com.aitasker.be.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletLedgerServiceTest {
    @Mock private SystemWalletRepository systemWalletRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private SystemWalletService systemWalletService;

    @InjectMocks private WalletLedgerService walletLedgerService;

    @Test
    void debitAvailable_shouldRejectNegativeResult() {
        SystemWalletEntity wallet = SystemWalletEntity.builder()
                .systemWalletId(1L)
                .accountId(10)
                .roleId(2)
                .walletType("BUSINESS")
                .currentBalance(new BigDecimal("100000"))
                .availableBalance(new BigDecimal("100000"))
                .escrowBalance(BigDecimal.ZERO)
                .holdingBalance(BigDecimal.ZERO)
                .disputedBalance(BigDecimal.ZERO)
                .totalRevenue(BigDecimal.ZERO)
                .currency("VND")
                .lastSyncedAt(LocalDateTime.now())
                .depositedBusinessCount(0)
                .successfulDepositCount(0)
                .build();
        when(systemWalletRepository.findByAccountIdForUpdate(10)).thenReturn(Optional.of(wallet));

        AppException ex = assertThrows(AppException.class, () ->
                walletLedgerService.debitAvailable(
                        10,
                        new BigDecimal("150000"),
                        "CREDIT_PURCHASE",
                        "CREDIT_PURCHASE",
                        10L,
                        "test"
                ));

        assertEquals("INSUFFICIENT_BALANCE", ex.getMessage());
        assertEquals(new BigDecimal("100000"), wallet.getAvailableBalance());
    }

    @Test
    void holdEscrowFromAvailable_shouldReturnExistingOperationWithoutMutatingWallet() {
        SystemWalletEntity wallet = SystemWalletEntity.builder()
                .systemWalletId(1L)
                .accountId(10)
                .availableBalance(new BigDecimal("100000"))
                .escrowBalance(BigDecimal.ZERO)
                .holdingBalance(BigDecimal.ZERO)
                .disputedBalance(BigDecimal.ZERO)
                .currentBalance(new BigDecimal("100000"))
                .build();
        WalletTransactionEntity debit = WalletTransactionEntity.builder()
                .id(1L)
                .accountId(10)
                .transactionType(WalletTransactionEntity.TX_ESCROW_DEPOSIT)
                .direction("DEBIT")
                .balanceType(WalletLedgerService.BALANCE_AVAILABLE)
                .amount(new BigDecimal("20000"))
                .referenceType("MILESTONE")
                .referenceId(20L)
                .operationKey("MILESTONE_ESCROW_DEPOSIT:1:20")
                .operationLeg(WalletLedgerService.LEG_AVAILABLE_DEBIT)
                .description("Deposit milestone escrow")
                .build();
        WalletTransactionEntity hold = WalletTransactionEntity.builder()
                .id(2L)
                .accountId(10)
                .transactionType(WalletTransactionEntity.TX_ESCROW_DEPOSIT)
                .direction("HOLD")
                .balanceType(WalletLedgerService.BALANCE_ESCROW)
                .amount(new BigDecimal("20000"))
                .referenceType("MILESTONE")
                .referenceId(20L)
                .operationKey("MILESTONE_ESCROW_DEPOSIT:1:20")
                .operationLeg(WalletLedgerService.LEG_ESCROW_HOLD)
                .description("Deposit milestone escrow")
                .build();

        when(systemWalletRepository.findByAccountIdForUpdate(10)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepository.findByOperationKeyOrderByCreatedAtAscIdAsc("MILESTONE_ESCROW_DEPOSIT:1:20"))
                .thenReturn(List.of(debit, hold));

        WalletTransactionEntity result = walletLedgerService.holdEscrowFromAvailable(
                10,
                new BigDecimal("20000"),
                WalletTransactionEntity.TX_ESCROW_DEPOSIT,
                "MILESTONE",
                20L,
                "Deposit milestone escrow",
                WalletLedgerService.WalletOperationContext.builder()
                        .operationKey("MILESTONE_ESCROW_DEPOSIT:1:20")
                        .build()
        );

        assertEquals(2L, result.getId());
        assertEquals(new BigDecimal("100000"), wallet.getAvailableBalance());
        verify(systemWalletRepository, never()).save(wallet);
    }

    @Test
    void creditPlatformRevenue_shouldIncreaseAvailableBalanceAndTotalRevenue() {
        SystemWalletEntity platformWallet = SystemWalletEntity.builder()
                .systemWalletId(1L)
                .accountId(1)
                .availableBalance(new BigDecimal("100"))
                .escrowBalance(BigDecimal.ZERO)
                .holdingBalance(BigDecimal.ZERO)
                .disputedBalance(BigDecimal.ZERO)
                .currentBalance(new BigDecimal("100"))
                .totalRevenue(new BigDecimal("80"))
                .build();
        when(systemWalletRepository.findByAccountIdForUpdate(1)).thenReturn(Optional.of(platformWallet));
        when(walletTransactionRepository.findByOperationKeyAndOperationLeg(
                "MEMBERSHIP_PURCHASE:test", WalletLedgerService.LEG_PLATFORM_REVENUE_CREDIT))
                .thenReturn(Optional.empty());
        when(walletTransactionRepository.save(any(WalletTransactionEntity.class))).thenAnswer(invocation -> {
            WalletTransactionEntity tx = invocation.getArgument(0);
            tx.setId(50L);
            return tx;
        });

        WalletTransactionEntity result = walletLedgerService.creditPlatformRevenue(
                1,
                new BigDecimal("30"),
                "MEMBERSHIP_PURCHASE",
                "MEMBERSHIP",
                2L,
                "Business Plus",
                WalletLedgerService.WalletOperationContext.builder()
                        .operationKey("MEMBERSHIP_PURCHASE:test")
                        .operationLeg(WalletLedgerService.LEG_PLATFORM_REVENUE_CREDIT)
                        .build()
        );

        assertEquals(new BigDecimal("130"), platformWallet.getAvailableBalance());
        assertEquals(new BigDecimal("110"), platformWallet.getTotalRevenue());
        assertEquals(new BigDecimal("130"), platformWallet.getCurrentBalance());
        assertEquals("CREDIT", result.getDirection());
        assertEquals(WalletLedgerService.LEG_PLATFORM_REVENUE_CREDIT, result.getOperationLeg());
        verify(systemWalletRepository).save(platformWallet);
    }

    @Test
    void creditPlatformRevenue_whenOperationAlreadyExists_shouldNotCreditAgain() {
        SystemWalletEntity platformWallet = SystemWalletEntity.builder()
                .systemWalletId(1L)
                .accountId(1)
                .availableBalance(new BigDecimal("130"))
                .escrowBalance(BigDecimal.ZERO)
                .holdingBalance(BigDecimal.ZERO)
                .disputedBalance(BigDecimal.ZERO)
                .currentBalance(new BigDecimal("130"))
                .totalRevenue(new BigDecimal("110"))
                .build();
        WalletTransactionEntity existing = WalletTransactionEntity.builder()
                .id(50L)
                .accountId(1)
                .transactionType("MEMBERSHIP_PURCHASE")
                .direction("CREDIT")
                .balanceType(WalletLedgerService.BALANCE_AVAILABLE)
                .amount(new BigDecimal("30"))
                .referenceType("MEMBERSHIP")
                .referenceId(2L)
                .operationKey("MEMBERSHIP_PURCHASE:test")
                .operationLeg(WalletLedgerService.LEG_PLATFORM_REVENUE_CREDIT)
                .description("Business Plus")
                .build();
        when(systemWalletRepository.findByAccountIdForUpdate(1)).thenReturn(Optional.of(platformWallet));
        when(walletTransactionRepository.findByOperationKeyAndOperationLeg(
                "MEMBERSHIP_PURCHASE:test", WalletLedgerService.LEG_PLATFORM_REVENUE_CREDIT))
                .thenReturn(Optional.of(existing));

        WalletTransactionEntity result = walletLedgerService.creditPlatformRevenue(
                1,
                new BigDecimal("30"),
                "MEMBERSHIP_PURCHASE",
                "MEMBERSHIP",
                2L,
                "Business Plus",
                WalletLedgerService.WalletOperationContext.builder()
                        .operationKey("MEMBERSHIP_PURCHASE:test")
                        .operationLeg(WalletLedgerService.LEG_PLATFORM_REVENUE_CREDIT)
                        .build()
        );

        assertSame(existing, result);
        assertEquals(new BigDecimal("130"), platformWallet.getAvailableBalance());
        assertEquals(new BigDecimal("110"), platformWallet.getTotalRevenue());
        verify(systemWalletRepository, never()).save(platformWallet);
        verify(walletTransactionRepository, never()).save(any(WalletTransactionEntity.class));
    }
}
