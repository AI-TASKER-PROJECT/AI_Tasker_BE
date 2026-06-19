package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.entity.SystemWalletEntity;
import com.aitasker.be.repository.SystemWalletRepository;
import com.aitasker.be.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
