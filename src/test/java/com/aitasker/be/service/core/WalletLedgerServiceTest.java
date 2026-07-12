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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
