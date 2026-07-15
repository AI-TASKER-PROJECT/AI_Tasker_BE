package com.aitasker.be.service.core;

import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.entity.SystemWalletEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.repository.BusinessProfileRepository;
import com.aitasker.be.repository.ContractRepository;
import com.aitasker.be.repository.ExpertProfileRepository;
import com.aitasker.be.repository.MembershipPurchaseRepository;
import com.aitasker.be.repository.MilestoneRepository;
import com.aitasker.be.repository.SystemWalletRepository;
import com.aitasker.be.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemWalletServiceTest {
    @Mock private AccessService accessService;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private SystemWalletRepository systemWalletRepository;
    @Mock private MembershipPurchaseRepository membershipPurchaseRepository;
    @Mock private BusinessProfileRepository businessProfileRepository;
    @Mock private ExpertProfileRepository expertProfileRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private ContractRepository contractRepository;

    @InjectMocks private SystemWalletService service;

    @Test
    void syncWallet_shouldIncludeSuccessfulMembershipRevenueInAdminTotalRevenue() {
        AccountEntity admin = account(1, 3, "ADMIN");
        SystemWalletEntity adminWallet = wallet(1);

        when(accountRepository.findFirstByRoleRoleNameOrderByAccountIdAsc("ADMIN")).thenReturn(Optional.of(admin));
        when(accountRepository.findAll()).thenReturn(List.of(admin));
        when(systemWalletRepository.findByAccountId(1)).thenReturn(Optional.of(adminWallet));
        when(systemWalletRepository.save(any(SystemWalletEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(systemWalletRepository.findByAccountId(admin.getAccountId())).thenReturn(Optional.of(adminWallet));
        when(transactionRepository.sumSuccessfulCommissionFee()).thenReturn(new BigDecimal("150"));
        when(membershipPurchaseRepository.sumSuccessfulMembershipRevenue()).thenReturn(new BigDecimal("900"));
        when(transactionRepository.calculateHoldingBalance()).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.calculateDisputedBalance()).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.countDepositedBusinesses()).thenReturn(0L);
        when(transactionRepository.countByStatusAndTransactionType("Success", "Deposit")).thenReturn(0L);

        SystemWalletEntity result = service.syncWallet();

        assertEquals(new BigDecimal("1050"), result.getTotalRevenue());
        assertEquals(new BigDecimal("1050"), result.getAvailableBalance());
        assertEquals(new BigDecimal("1050"), result.getCurrentBalance());
    }

    private AccountEntity account(Integer accountId, Integer roleId, String roleName) {
        return AccountEntity.builder()
                .accountId(accountId)
                .role(RoleEntity.builder().roleId(roleId).roleName(roleName).build())
                .build();
    }

    private SystemWalletEntity wallet(Integer accountId) {
        return SystemWalletEntity.builder()
                .accountId(accountId)
                .currentBalance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .escrowBalance(BigDecimal.ZERO)
                .totalRevenue(BigDecimal.ZERO)
                .holdingBalance(BigDecimal.ZERO)
                .disputedBalance(BigDecimal.ZERO)
                .currency("VND")
                .build();
    }
}
