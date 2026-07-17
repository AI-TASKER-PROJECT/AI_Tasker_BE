package com.aitasker.be.service.core;

import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.entity.SystemWalletEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.repository.BusinessProfileRepository;
import com.aitasker.be.repository.ContractRepository;
import com.aitasker.be.repository.ContractMilestoneRepository;
import com.aitasker.be.repository.ExpertProfileRepository;
import com.aitasker.be.repository.MilestoneRepository;
import com.aitasker.be.repository.SystemWalletRepository;
import com.aitasker.be.repository.TransactionRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemWalletServiceTest {
    @Mock private AccessService accessService;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private SystemWalletRepository systemWalletRepository;
    @Mock private BusinessProfileRepository businessProfileRepository;
    @Mock private ExpertProfileRepository expertProfileRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private ContractMilestoneRepository contractMilestoneRepository;

    @InjectMocks private SystemWalletService systemWalletService;

    @Test
    void syncWallet_shouldIncludeCommissionMembershipAndCreditRevenue() {
        AccountEntity admin = AccountEntity.builder()
                .accountId(1)
                .role(RoleEntity.builder().roleId(1).roleName("ADMIN").build())
                .build();
        SystemWalletEntity platformWallet = SystemWalletEntity.builder()
                .systemWalletId(1L)
                .accountId(1)
                .roleId(1)
                .walletType("ADMIN_SYSTEM")
                .currentBalance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .escrowBalance(BigDecimal.ZERO)
                .totalRevenue(BigDecimal.ZERO)
                .holdingBalance(BigDecimal.ZERO)
                .disputedBalance(BigDecimal.ZERO)
                .currency("VND")
                .lastSyncedAt(LocalDateTime.now())
                .depositedBusinessCount(0)
                .successfulDepositCount(0)
                .build();

        when(accountRepository.findFirstByRoleRoleNameOrderByAccountIdAsc("ADMIN"))
                .thenReturn(Optional.of(admin));
        when(accountRepository.findAll()).thenReturn(List.of(admin));
        when(systemWalletRepository.findByAccountId(1)).thenReturn(Optional.of(platformWallet));
        when(systemWalletRepository.findByAccountIdForUpdate(1)).thenReturn(Optional.of(platformWallet));
        when(systemWalletRepository.save(any(SystemWalletEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.latestTransactionId()).thenReturn(9L);
        when(transactionRepository.sumSuccessfulCommissionFee()).thenReturn(new BigDecimal("100"));
        when(walletTransactionRepository.sumPostedPlatformPurchaseRevenue()).thenReturn(new BigDecimal("600"));
        when(transactionRepository.calculateHoldingBalance()).thenReturn(new BigDecimal("20"));
        when(walletTransactionRepository.calculatePostedEscrowBalance()).thenReturn(new BigDecimal("80"));
        when(contractMilestoneRepository.calculateActiveDisputedEscrowBalance()).thenReturn(new BigDecimal("30"));
        when(transactionRepository.countDepositedBusinesses()).thenReturn(2L);
        when(transactionRepository.countByStatusAndTransactionType("Success", "Deposit")).thenReturn(3L);

        SystemWalletEntity result = systemWalletService.syncWallet();

        assertEquals(new BigDecimal("700"), result.getTotalRevenue());
        assertEquals(new BigDecimal("700"), result.getAvailableBalance());
        assertEquals(new BigDecimal("100"), result.getEscrowBalance());
        assertEquals(new BigDecimal("30"), result.getDisputedBalance());
        assertEquals(new BigDecimal("800"), result.getCurrentBalance());
        assertEquals(2, result.getDepositedBusinessCount());
        assertEquals(3, result.getSuccessfulDepositCount());
        verify(walletTransactionRepository).sumPostedPlatformPurchaseRevenue();
        verify(systemWalletRepository).findByAccountIdForUpdate(1);
    }
}
