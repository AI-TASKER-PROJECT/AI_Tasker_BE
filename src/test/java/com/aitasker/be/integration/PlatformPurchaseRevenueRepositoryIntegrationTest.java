package com.aitasker.be.integration;

import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.SystemWalletEntity;
import com.aitasker.be.entity.WalletTransactionEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.repository.WalletTransactionRepository;
import com.aitasker.be.service.core.SystemWalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.config.import=",
        "DB_HOST=127.0.0.1",
        "DB_PORT=5433",
        "DB_NAME=${TEST_DB_NAME:aitasker_db}",
        "DB_USER=aitasker",
        "DB_PASSWORD=aitasker123",
        "APP_JWT_SECRET=8WVkg9Zpwj4NMwCM5PUn+WL9EhUFc1ffvOnTd5P2SMZriHPMdKoX2A1uLY+xYDSCZvJLEj4t8vg8SOdrd4SNBg==",
        "app.jwt.secret=8WVkg9Zpwj4NMwCM5PUn+WL9EhUFc1ffvOnTd5P2SMZriHPMdKoX2A1uLY+xYDSCZvJLEj4t8vg8SOdrd4SNBg==",
        "spring.datasource.url=${TEST_DB_URL:jdbc:postgresql://127.0.0.1:5433/aitasker_db?options=-c%20TimeZone=Asia/Ho_Chi_Minh}",
        "spring.datasource.username=aitasker",
        "spring.datasource.password=aitasker123",
        "rag.ingest-on-startup=false"
})
class PlatformPurchaseRevenueRepositoryIntegrationTest {
    @Autowired private AccountRepository accountRepository;
    @Autowired private SystemWalletService systemWalletService;
    @Autowired private WalletTransactionRepository walletTransactionRepository;

    @Test
    @Transactional
    void aggregate_shouldCountPostedPurchaserDebitsWithoutCountingPlatformCreditCounterpart() {
        AccountEntity admin = accountRepository.findFirstByRoleRoleNameOrderByAccountIdAsc("ADMIN")
                .orElseThrow();
        SystemWalletEntity wallet = systemWalletService.ensureWallet(admin);
        BigDecimal baseline = walletTransactionRepository.sumPostedPlatformPurchaseRevenue();
        String operationPrefix = "platform-revenue-test:" + UUID.randomUUID();

        walletTransactionRepository.save(purchaseLeg(
                wallet, "MEMBERSHIP_PURCHASE", "DEBIT", "AVAILABLE", "100", "POSTED",
                operationPrefix + ":membership", "PURCHASER_AVAILABLE_DEBIT"
        ));
        walletTransactionRepository.save(purchaseLeg(
                wallet, "CREDIT_PURCHASE", "DEBIT", "AVAILABLE", "200", "POSTED",
                operationPrefix + ":credit", "PURCHASER_AVAILABLE_DEBIT"
        ));
        walletTransactionRepository.save(purchaseLeg(
                wallet, "MEMBERSHIP_PURCHASE", "CREDIT", "AVAILABLE", "100", "POSTED",
                operationPrefix + ":membership", "PLATFORM_REVENUE_CREDIT"
        ));
        walletTransactionRepository.save(purchaseLeg(
                wallet, "CREDIT_PURCHASE", "DEBIT", "ESCROW", "400", "POSTED",
                operationPrefix + ":escrow", "PURCHASER_ESCROW_DEBIT"
        ));
        walletTransactionRepository.flush();

        assertEquals(0, baseline.add(new BigDecimal("300"))
                .compareTo(walletTransactionRepository.sumPostedPlatformPurchaseRevenue()));
    }

    private WalletTransactionEntity purchaseLeg(
            SystemWalletEntity wallet,
            String transactionType,
            String direction,
            String balanceType,
            String amount,
            String status,
            String operationKey,
            String operationLeg
    ) {
        BigDecimal value = new BigDecimal(amount);
        return WalletTransactionEntity.builder()
                .systemWalletId(wallet.getSystemWalletId())
                .accountId(wallet.getAccountId())
                .transactionType(transactionType)
                .direction(direction)
                .balanceType(balanceType)
                .amount(value)
                .balanceBefore(BigDecimal.ZERO)
                .balanceAfter(value)
                .status(status)
                .operationKey(operationKey)
                .operationLeg(operationLeg)
                .build();
    }
}
