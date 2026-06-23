/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/WalletLedgerService.java
 * Day la file gi: File service chua nghiep vu chinh, dieu phoi repository va kiem tra luat xu ly cua he thong.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
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

// Note: Annotation nay cho Spring quan ly class nhu mot service chua nghiep vu.
@Service
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class WalletLedgerService {
    private static final String WALLET_TRANSACTION_TOPUP = "TOPUP";
    private static final String BALANCE_AVAILABLE = "AVAILABLE";
    private static final String BALANCE_ESCROW = "ESCROW";
    private static final String BALANCE_HOLDING = "HOLDING";

    private final SystemWalletRepository systemWalletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final SystemWalletService systemWalletService;

    @Transactional
    // Note: Ham `postWalletTopup` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public void postWalletTopup(PaymentOrderEntity paymentOrder) {
        if (walletTransactionRepository.existsByPaymentOrderIdAndTransactionType(paymentOrder.getId(), WALLET_TRANSACTION_TOPUP)) {
            return;
        }
        if (paymentOrder.getAccountId() == null) {
            throw new AppException("PAYMENT ORDER CHUA CO ACCOUNT DE NAP VI");
        }

        Integer accountId = Math.toIntExact(paymentOrder.getAccountId());
        creditAvailable(
                accountId,
                paymentOrder.getAmount(),
                WALLET_TRANSACTION_TOPUP,
                "PAYMENT_ORDER",
                paymentOrder.getId(),
                paymentOrder.getDescription(),
                paymentOrder.getId()
        );
    }

    @Transactional
    // Note: Ham `creditAvailable` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public WalletTransactionEntity creditAvailable(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description
    ) {
        return creditAvailable(accountId, amount, transactionType, referenceType, referenceId, description, null);
    }

    @Transactional
    // Note: Ham `debitAvailable` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public WalletTransactionEntity debitAvailable(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description
    ) {
        amount = positiveAmount(amount);
        SystemWalletEntity wallet = walletForAccount(accountId);
        BigDecimal before = balance(wallet, BALANCE_AVAILABLE);
        requireSufficient(before, amount);
        setBalance(wallet, BALANCE_AVAILABLE, before.subtract(amount));
        recomputeCurrentBalance(wallet);
        systemWalletRepository.save(wallet);

        return record(wallet, accountId, null, transactionType, "DEBIT", BALANCE_AVAILABLE,
                amount, before, balance(wallet, BALANCE_AVAILABLE), referenceType, referenceId, description);
    }

    @Transactional
    // Note: Ham `holdEscrowFromAvailable` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public WalletTransactionEntity holdEscrowFromAvailable(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description
    ) {
        return transferFromAvailable(accountId, amount, BALANCE_ESCROW, transactionType, referenceType, referenceId, description);
    }

    @Transactional
    // Note: Ham `holdWithdrawalFromAvailable` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public WalletTransactionEntity holdWithdrawalFromAvailable(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description
    ) {
        return transferFromAvailable(accountId, amount, BALANCE_HOLDING, transactionType, referenceType, referenceId, description);
    }

    @Transactional
    // Note: Ham `releaseEscrowToAvailable` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public WalletTransactionEntity releaseEscrowToAvailable(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description
    ) {
        return releaseToAvailable(accountId, amount, BALANCE_ESCROW, transactionType, referenceType, referenceId, description);
    }

    @Transactional
    // Note: Ham `releaseHoldingToAvailable` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public WalletTransactionEntity releaseHoldingToAvailable(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description
    ) {
        return releaseToAvailable(accountId, amount, BALANCE_HOLDING, transactionType, referenceType, referenceId, description);
    }

    @Transactional
    // Note: Ham `debitEscrow` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public WalletTransactionEntity debitEscrow(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description
    ) {
        return debitFromBalance(accountId, amount, BALANCE_ESCROW, transactionType, referenceType, referenceId, description);
    }

    @Transactional
    // Note: Ham `debitHolding` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public WalletTransactionEntity debitHolding(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description
    ) {
        return debitFromBalance(accountId, amount, BALANCE_HOLDING, transactionType, referenceType, referenceId, description);
    }

    @Transactional(readOnly = true)
    // Note: Ham `availableBalance` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public BigDecimal availableBalance(Integer accountId) {
        SystemWalletEntity wallet = systemWalletRepository.findByAccountId(accountId)
                .orElse(null);
        return wallet == null ? BigDecimal.ZERO : balance(wallet, BALANCE_AVAILABLE);
    }

    // Note: Ham `creditAvailable` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private WalletTransactionEntity creditAvailable(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description,
            Long paymentOrderId
    ) {
        amount = positiveAmount(amount);
        SystemWalletEntity wallet = walletForAccount(accountId);
        BigDecimal before = balance(wallet, BALANCE_AVAILABLE);
        setBalance(wallet, BALANCE_AVAILABLE, before.add(amount));
        recomputeCurrentBalance(wallet);
        systemWalletRepository.save(wallet);

        return record(wallet, accountId, paymentOrderId, transactionType, "CREDIT", BALANCE_AVAILABLE,
                amount, before, balance(wallet, BALANCE_AVAILABLE), referenceType, referenceId, description);
    }

    // Note: Ham `transferFromAvailable` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private WalletTransactionEntity transferFromAvailable(
            Integer accountId,
            BigDecimal amount,
            String targetBalance,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description
    ) {
        amount = positiveAmount(amount);
        SystemWalletEntity wallet = walletForAccount(accountId);

        BigDecimal availableBefore = balance(wallet, BALANCE_AVAILABLE);
        requireSufficient(availableBefore, amount);
        BigDecimal targetBefore = balance(wallet, targetBalance);

        setBalance(wallet, BALANCE_AVAILABLE, availableBefore.subtract(amount));
        setBalance(wallet, targetBalance, targetBefore.add(amount));
        recomputeCurrentBalance(wallet);
        systemWalletRepository.save(wallet);

        record(wallet, accountId, null, transactionType, "DEBIT", BALANCE_AVAILABLE,
                amount, availableBefore, balance(wallet, BALANCE_AVAILABLE), referenceType, referenceId, description);
        return record(wallet, accountId, null, transactionType, "HOLD", targetBalance,
                amount, targetBefore, balance(wallet, targetBalance), referenceType, referenceId, description);
    }

    // Note: Ham `releaseToAvailable` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private WalletTransactionEntity releaseToAvailable(
            Integer accountId,
            BigDecimal amount,
            String sourceBalance,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description
    ) {
        amount = positiveAmount(amount);
        SystemWalletEntity wallet = walletForAccount(accountId);

        BigDecimal sourceBefore = balance(wallet, sourceBalance);
        requireSufficient(sourceBefore, amount);
        BigDecimal availableBefore = balance(wallet, BALANCE_AVAILABLE);

        setBalance(wallet, sourceBalance, sourceBefore.subtract(amount));
        setBalance(wallet, BALANCE_AVAILABLE, availableBefore.add(amount));
        recomputeCurrentBalance(wallet);
        systemWalletRepository.save(wallet);

        record(wallet, accountId, null, transactionType, "RELEASE", sourceBalance,
                amount, sourceBefore, balance(wallet, sourceBalance), referenceType, referenceId, description);
        return record(wallet, accountId, null, transactionType, "CREDIT", BALANCE_AVAILABLE,
                amount, availableBefore, balance(wallet, BALANCE_AVAILABLE), referenceType, referenceId, description);
    }

    // Note: Ham `debitFromBalance` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private WalletTransactionEntity debitFromBalance(
            Integer accountId,
            BigDecimal amount,
            String sourceBalance,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description
    ) {
        amount = positiveAmount(amount);
        SystemWalletEntity wallet = walletForAccount(accountId);
        BigDecimal before = balance(wallet, sourceBalance);
        requireSufficient(before, amount);
        setBalance(wallet, sourceBalance, before.subtract(amount));
        recomputeCurrentBalance(wallet);
        systemWalletRepository.save(wallet);

        return record(wallet, accountId, null, transactionType, "DEBIT", sourceBalance,
                amount, before, balance(wallet, sourceBalance), referenceType, referenceId, description);
    }

    // Note: Ham `walletForAccount` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private SystemWalletEntity walletForAccount(Integer accountId) {
        systemWalletService.ensureWalletByAccountId(accountId);
        return systemWalletRepository.findByAccountIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("CHUA CO VI CHO TAI KHOAN NAY"));
    }

    // Note: Ham `record` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private WalletTransactionEntity record(
            SystemWalletEntity wallet,
            Integer accountId,
            Long paymentOrderId,
            String transactionType,
            String direction,
            String balanceType,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String referenceType,
            Long referenceId,
            String description
    ) {
        return walletTransactionRepository.save(WalletTransactionEntity.builder()
                .systemWalletId(wallet.getSystemWalletId())
                .accountId(accountId)
                .paymentOrderId(paymentOrderId)
                .transactionType(transactionType)
                .direction(direction)
                .balanceType(balanceType)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status("POSTED")
                .referenceType(referenceType)
                .referenceId(referenceId)
                .description(description)
                .build());
    }

    // Note: Ham `positiveAmount` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private BigDecimal positiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new AppException("SO TIEN PHAI LON HON 0");
        }
        return amount;
    }

    // Note: Ham `requireSufficient` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void requireSufficient(BigDecimal balance, BigDecimal amount) {
        if (nonNegativeMoney(balance).compareTo(amount) < 0) {
            throw new AppException("INSUFFICIENT_BALANCE");
        }
    }

    // Note: Ham `balance` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private BigDecimal balance(SystemWalletEntity wallet, String balanceType) {
        return switch (balanceType) {
            case BALANCE_AVAILABLE -> nonNegativeMoney(wallet.getAvailableBalance());
            case BALANCE_ESCROW -> nonNegativeMoney(wallet.getEscrowBalance());
            case BALANCE_HOLDING -> nonNegativeMoney(wallet.getHoldingBalance());
            default -> nonNegativeMoney(wallet.getDisputedBalance());
        };
    }

    // Note: Ham `setBalance` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void setBalance(SystemWalletEntity wallet, String balanceType, BigDecimal value) {
        BigDecimal safe = nonNegativeMoney(value);
        switch (balanceType) {
            case BALANCE_AVAILABLE -> wallet.setAvailableBalance(safe);
            case BALANCE_ESCROW -> wallet.setEscrowBalance(safe);
            case BALANCE_HOLDING -> wallet.setHoldingBalance(safe);
            default -> wallet.setDisputedBalance(safe);
        }
    }

    // Note: Ham `recomputeCurrentBalance` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void recomputeCurrentBalance(SystemWalletEntity wallet) {
        wallet.setCurrentBalance(balance(wallet, BALANCE_AVAILABLE)
                .add(balance(wallet, BALANCE_ESCROW))
                .add(balance(wallet, BALANCE_HOLDING))
                .add(balance(wallet, "DISPUTE")));
    }

    // Note: Ham `nonNegativeMoney` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private BigDecimal nonNegativeMoney(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return value;
    }
}
