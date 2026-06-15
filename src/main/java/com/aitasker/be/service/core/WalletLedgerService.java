package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.entity.PaymentOrderEntity;
import com.aitasker.be.entity.SystemWalletEntity;
import com.aitasker.be.entity.WalletTransactionEntity;
import com.aitasker.be.repository.SystemWalletRepository;
import com.aitasker.be.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletLedgerService {
    private static final String WALLET_TRANSACTION_TOPUP = "TOPUP";

    private final SystemWalletRepository systemWalletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final SystemWalletService systemWalletService;

    @Transactional
    public void postWalletTopup(PaymentOrderEntity paymentOrder) {
        if (walletTransactionRepository.existsByPaymentOrderIdAndTransactionType(paymentOrder.getId(), WALLET_TRANSACTION_TOPUP)) {
            return;
        }
        if (paymentOrder.getAccountId() == null) {
            throw new AppException("PAYMENT ORDER CHUA CO ACCOUNT DE NAP VI");
        }

        Integer accountId = Math.toIntExact(paymentOrder.getAccountId());
        systemWalletService.ensureWalletByAccountId(accountId);
        SystemWalletEntity wallet = systemWalletRepository.findByAccountId(accountId)
                .orElseThrow(() -> new NotFoundException("CHUA CO VI CHO TAI KHOAN NAY"));

        BigDecimal balanceBefore = nonNegativeMoney(wallet.getAvailableBalance());
        BigDecimal balanceAfter = balanceBefore.add(paymentOrder.getAmount());
        wallet.setAvailableBalance(balanceAfter);
        wallet.setCurrentBalance(nonNegativeMoney(wallet.getCurrentBalance()).add(paymentOrder.getAmount()));
        systemWalletRepository.save(wallet);

        walletTransactionRepository.save(WalletTransactionEntity.builder()
                .systemWalletId(wallet.getSystemWalletId())
                .accountId(accountId)
                .paymentOrderId(paymentOrder.getId())
                .transactionType(WALLET_TRANSACTION_TOPUP)
                .direction("CREDIT")
                .balanceType("AVAILABLE")
                .amount(paymentOrder.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status("POSTED")
                .referenceType("PAYMENT_ORDER")
                .referenceId(paymentOrder.getId())
                .description(paymentOrder.getDescription())
                .build());
    }

    private BigDecimal nonNegativeMoney(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return value;
    }
}
