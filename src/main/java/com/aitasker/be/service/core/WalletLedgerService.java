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
import org.springframework.dao.DataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Note: Annotation nay cho Spring quan ly class nhu mot service chua nghiep vu.
@Service
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class WalletLedgerService {
    private static final String WALLET_TRANSACTION_TOPUP = "TOPUP";
    public static final String BALANCE_AVAILABLE = "AVAILABLE";
    public static final String BALANCE_ESCROW = "ESCROW";
    public static final String BALANCE_HOLDING = "HOLDING";
    public static final String LEG_AVAILABLE_DEBIT = "AVAILABLE_DEBIT";
    public static final String LEG_AVAILABLE_CREDIT = "AVAILABLE_CREDIT";
    public static final String LEG_ESCROW_HOLD = "ESCROW_HOLD";
    public static final String LEG_ESCROW_RELEASE = "ESCROW_RELEASE";
    public static final String LEG_ESCROW_DEBIT = "ESCROW_DEBIT";
    public static final String LEG_HOLDING_HOLD = "HOLDING_HOLD";
    public static final String LEG_HOLDING_RELEASE = "HOLDING_RELEASE";
    public static final String LEG_HOLDING_DEBIT = "HOLDING_DEBIT";
    public static final String LEG_PURCHASER_AVAILABLE_DEBIT = "PURCHASER_AVAILABLE_DEBIT";
    public static final String LEG_PLATFORM_REVENUE_CREDIT = "PLATFORM_REVENUE_CREDIT";

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
                WalletOperationContext.builder()
                        .paymentOrderId(paymentOrder.getId())
                        .operationKey("TOPUP:" + paymentOrder.getId())
                        .operationLeg(LEG_AVAILABLE_CREDIT)
                        .build()
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
        return creditAvailable(accountId, amount, transactionType, referenceType, referenceId, description, WalletOperationContext.empty());
    }

    @Transactional
    public WalletTransactionEntity creditAvailable(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context
    ) {
        return creditAvailableInternal(accountId, amount, transactionType, referenceType, referenceId, description, normalizeContext(context));
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
        return debitAvailable(accountId, amount, transactionType, referenceType, referenceId, description, WalletOperationContext.empty());
    }

    @Transactional
    public WalletTransactionEntity debitAvailable(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context
    ) {
        amount = positiveAmount(amount);
        SystemWalletEntity wallet = walletForAccount(accountId);
        WalletOperationContext normalized = ensureLeg(normalizeContext(context), LEG_AVAILABLE_DEBIT);
        WalletTransactionEntity existing = loadExistingSingleLeg(normalized, accountId, transactionType, "DEBIT",
                BALANCE_AVAILABLE, amount, referenceType, referenceId, description);
        if (existing != null) {
            return existing;
        }
        BigDecimal before = balance(wallet, BALANCE_AVAILABLE);
        requireSufficient(before, amount);
        setBalance(wallet, BALANCE_AVAILABLE, before.subtract(amount));
        recomputeCurrentBalance(wallet);
        systemWalletRepository.save(wallet);

        return saveSingleLegWithRaceGuard(wallet, accountId, transactionType, "DEBIT", BALANCE_AVAILABLE,
                amount, before, balance(wallet, BALANCE_AVAILABLE), referenceType, referenceId, description, normalized);
    }

    @Transactional
    public WalletTransactionEntity creditPlatformRevenue(
            Integer platformAccountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context
    ) {
        amount = positiveAmount(amount);
        SystemWalletEntity wallet = walletForAccount(platformAccountId);
        WalletOperationContext normalized = ensureLeg(normalizeContext(context), LEG_PLATFORM_REVENUE_CREDIT);
        WalletTransactionEntity existing = loadExistingSingleLeg(normalized, platformAccountId, transactionType,
                "CREDIT", BALANCE_AVAILABLE, amount, referenceType, referenceId, description);
        if (existing != null) {
            return existing;
        }

        BigDecimal availableBefore = balance(wallet, BALANCE_AVAILABLE);
        wallet.setAvailableBalance(availableBefore.add(amount));
        wallet.setTotalRevenue(nonNegativeMoney(wallet.getTotalRevenue()).add(amount));
        recomputeCurrentBalance(wallet);
        systemWalletRepository.save(wallet);

        return saveSingleLegWithRaceGuard(wallet, platformAccountId, transactionType, "CREDIT", BALANCE_AVAILABLE,
                amount, availableBefore, balance(wallet, BALANCE_AVAILABLE), referenceType, referenceId,
                description, normalized);
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
        return holdEscrowFromAvailable(accountId, amount, transactionType, referenceType, referenceId, description, WalletOperationContext.empty());
    }

    @Transactional
    public WalletTransactionEntity holdEscrowFromAvailable(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context
    ) {
        return transferFromAvailable(accountId, amount, BALANCE_ESCROW, transactionType, referenceType, referenceId,
                description, normalizeContext(context), LEG_AVAILABLE_DEBIT, LEG_ESCROW_HOLD);
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
        return holdWithdrawalFromAvailable(accountId, amount, transactionType, referenceType, referenceId, description, WalletOperationContext.empty());
    }

    @Transactional
    public WalletTransactionEntity holdWithdrawalFromAvailable(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context
    ) {
        return transferFromAvailable(accountId, amount, BALANCE_HOLDING, transactionType, referenceType, referenceId,
                description, normalizeContext(context), LEG_AVAILABLE_DEBIT, LEG_HOLDING_HOLD);
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
        return releaseEscrowToAvailable(accountId, amount, transactionType, referenceType, referenceId, description, WalletOperationContext.empty());
    }

    @Transactional
    public WalletTransactionEntity releaseEscrowToAvailable(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context
    ) {
        return releaseToAvailable(accountId, amount, BALANCE_ESCROW, transactionType, referenceType, referenceId,
                description, normalizeContext(context), LEG_ESCROW_RELEASE, LEG_AVAILABLE_CREDIT);
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
        return releaseHoldingToAvailable(accountId, amount, transactionType, referenceType, referenceId, description, WalletOperationContext.empty());
    }

    @Transactional
    public WalletTransactionEntity releaseHoldingToAvailable(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context
    ) {
        return releaseToAvailable(accountId, amount, BALANCE_HOLDING, transactionType, referenceType, referenceId,
                description, normalizeContext(context), LEG_HOLDING_RELEASE, LEG_AVAILABLE_CREDIT);
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
        return debitEscrow(accountId, amount, transactionType, referenceType, referenceId, description, WalletOperationContext.empty());
    }

    @Transactional
    public WalletTransactionEntity debitEscrow(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context
    ) {
        return debitFromBalance(accountId, amount, BALANCE_ESCROW, transactionType, referenceType, referenceId,
                description, ensureLeg(normalizeContext(context), LEG_ESCROW_DEBIT));
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
        return debitHolding(accountId, amount, transactionType, referenceType, referenceId, description, WalletOperationContext.empty());
    }

    @Transactional
    public WalletTransactionEntity debitHolding(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context
    ) {
        return debitFromBalance(accountId, amount, BALANCE_HOLDING, transactionType, referenceType, referenceId,
                description, ensureLeg(normalizeContext(context), LEG_HOLDING_DEBIT));
    }

    @Transactional(readOnly = true)
    // Note: Ham `availableBalance` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public BigDecimal availableBalance(Integer accountId) {
        SystemWalletEntity wallet = systemWalletRepository.findByAccountId(accountId)
                .orElse(null);
        return wallet == null ? BigDecimal.ZERO : balance(wallet, BALANCE_AVAILABLE);
    }

    // Note: Ham `creditAvailable` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private WalletTransactionEntity creditAvailableInternal(
            Integer accountId,
            BigDecimal amount,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context
    ) {
        amount = positiveAmount(amount);
        SystemWalletEntity wallet = walletForAccount(accountId);
        WalletOperationContext normalized = ensureLeg(context, LEG_AVAILABLE_CREDIT);
        WalletTransactionEntity existing = loadExistingSingleLeg(normalized, accountId, transactionType, "CREDIT",
                BALANCE_AVAILABLE, amount, referenceType, referenceId, description);
        if (existing != null) {
            return existing;
        }
        BigDecimal before = balance(wallet, BALANCE_AVAILABLE);
        setBalance(wallet, BALANCE_AVAILABLE, before.add(amount));
        recomputeCurrentBalance(wallet);
        systemWalletRepository.save(wallet);

        return saveSingleLegWithRaceGuard(wallet, accountId, transactionType, "CREDIT", BALANCE_AVAILABLE,
                amount, before, balance(wallet, BALANCE_AVAILABLE), referenceType, referenceId, description, normalized);
    }

    // Note: Ham `transferFromAvailable` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private WalletTransactionEntity transferFromAvailable(
            Integer accountId,
            BigDecimal amount,
            String targetBalance,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context,
            String availableLeg,
            String targetLeg
    ) {
        amount = positiveAmount(amount);
        SystemWalletEntity wallet = walletForAccount(accountId);
        WalletOperationContext normalized = normalizeContext(context);
        Map<String, WalletTransactionEntity> existingByLeg = loadExistingOperation(
                normalized, List.of(availableLeg, targetLeg), accountId, transactionType, referenceType, referenceId, description);
        if (!existingByLeg.isEmpty()) {
            return existingByLeg.get(targetLeg);
        }

        BigDecimal availableBefore = balance(wallet, BALANCE_AVAILABLE);
        requireSufficient(availableBefore, amount);
        BigDecimal targetBefore = balance(wallet, targetBalance);

        setBalance(wallet, BALANCE_AVAILABLE, availableBefore.subtract(amount));
        setBalance(wallet, targetBalance, targetBefore.add(amount));
        recomputeCurrentBalance(wallet);
        systemWalletRepository.save(wallet);

        WalletOperationContext debitContext = ensureLeg(normalized, availableLeg);
        WalletOperationContext holdContext = ensureLeg(normalized, targetLeg);
        try {
            saveSingleLeg(wallet, accountId, transactionType, "DEBIT", BALANCE_AVAILABLE,
                    amount, availableBefore, balance(wallet, BALANCE_AVAILABLE), referenceType, referenceId, description, debitContext);
            return saveSingleLeg(wallet, accountId, transactionType, "HOLD", targetBalance,
                    amount, targetBefore, balance(wallet, targetBalance), referenceType, referenceId, description, holdContext);
        } catch (DataIntegrityViolationException ex) {
            return resolveDualLegRace(normalized, availableLeg, targetLeg, accountId, transactionType, referenceType, referenceId, description);
        }
    }

    // Note: Ham `releaseToAvailable` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private WalletTransactionEntity releaseToAvailable(
            Integer accountId,
            BigDecimal amount,
            String sourceBalance,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context,
            String sourceLeg,
            String availableLeg
    ) {
        amount = positiveAmount(amount);
        SystemWalletEntity wallet = walletForAccount(accountId);
        WalletOperationContext normalized = normalizeContext(context);
        Map<String, WalletTransactionEntity> existingByLeg = loadExistingOperation(
                normalized, List.of(sourceLeg, availableLeg), accountId, transactionType, referenceType, referenceId, description);
        if (!existingByLeg.isEmpty()) {
            return existingByLeg.get(availableLeg);
        }

        BigDecimal sourceBefore = balance(wallet, sourceBalance);
        requireSufficient(sourceBefore, amount);
        BigDecimal availableBefore = balance(wallet, BALANCE_AVAILABLE);

        setBalance(wallet, sourceBalance, sourceBefore.subtract(amount));
        setBalance(wallet, BALANCE_AVAILABLE, availableBefore.add(amount));
        recomputeCurrentBalance(wallet);
        systemWalletRepository.save(wallet);

        WalletOperationContext releaseContext = ensureLeg(normalized, sourceLeg);
        WalletOperationContext creditContext = ensureLeg(normalized, availableLeg);
        try {
            saveSingleLeg(wallet, accountId, transactionType, "RELEASE", sourceBalance,
                    amount, sourceBefore, balance(wallet, sourceBalance), referenceType, referenceId, description, releaseContext);
            return saveSingleLeg(wallet, accountId, transactionType, "CREDIT", BALANCE_AVAILABLE,
                    amount, availableBefore, balance(wallet, BALANCE_AVAILABLE), referenceType, referenceId, description, creditContext);
        } catch (DataIntegrityViolationException ex) {
            return resolveDualLegRace(normalized, sourceLeg, availableLeg, accountId, transactionType, referenceType, referenceId, description);
        }
    }

    // Note: Ham `debitFromBalance` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private WalletTransactionEntity debitFromBalance(
            Integer accountId,
            BigDecimal amount,
            String sourceBalance,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context
    ) {
        amount = positiveAmount(amount);
        SystemWalletEntity wallet = walletForAccount(accountId);
        WalletTransactionEntity existing = loadExistingSingleLeg(context, accountId, transactionType, "DEBIT",
                sourceBalance, amount, referenceType, referenceId, description);
        if (existing != null) {
            return existing;
        }
        BigDecimal before = balance(wallet, sourceBalance);
        requireSufficient(before, amount);
        setBalance(wallet, sourceBalance, before.subtract(amount));
        recomputeCurrentBalance(wallet);
        systemWalletRepository.save(wallet);

        return saveSingleLegWithRaceGuard(wallet, accountId, transactionType, "DEBIT", sourceBalance,
                amount, before, balance(wallet, sourceBalance), referenceType, referenceId, description, context);
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
            String description,
            WalletOperationContext context
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
                .contractId(context.contractId())
                .milestoneId(context.milestoneId())
                .operationKey(context.operationKey())
                .operationLeg(context.operationLeg())
                .metadata(context.metadata())
                .description(description)
                .build());
    }

    private WalletTransactionEntity saveSingleLegWithRaceGuard(
            SystemWalletEntity wallet,
            Integer accountId,
            String transactionType,
            String direction,
            String balanceType,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context
    ) {
        try {
            return saveSingleLeg(wallet, accountId, transactionType, direction, balanceType, amount, balanceBefore,
                    balanceAfter, referenceType, referenceId, description, context);
        } catch (DataIntegrityViolationException ex) {
            WalletTransactionEntity existing = loadExistingSingleLeg(context, accountId, transactionType, direction,
                    balanceType, amount, referenceType, referenceId, description);
            if (existing != null) {
                return existing;
            }
            throw ex;
        }
    }

    private WalletTransactionEntity saveSingleLeg(
            SystemWalletEntity wallet,
            Integer accountId,
            String transactionType,
            String direction,
            String balanceType,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context
    ) {
        return record(wallet, accountId, context.paymentOrderId(), transactionType, direction, balanceType, amount,
                balanceBefore, balanceAfter, referenceType, referenceId, description, context);
    }

    private WalletTransactionEntity loadExistingSingleLeg(
            WalletOperationContext context,
            Integer accountId,
            String transactionType,
            String direction,
            String balanceType,
            BigDecimal amount,
            String referenceType,
            Long referenceId,
            String description
    ) {
        if (!hasOperationIdentity(context)) {
            return null;
        }
        return walletTransactionRepository.findByOperationKeyAndOperationLeg(context.operationKey(), context.operationLeg())
                .map(existing -> validateExistingLeg(existing, accountId, transactionType, direction, balanceType, amount,
                        referenceType, referenceId, description, context))
                .orElse(null);
    }

    private Map<String, WalletTransactionEntity> loadExistingOperation(
            WalletOperationContext context,
            List<String> expectedLegs,
            Integer accountId,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description
    ) {
        if (context.operationKey() == null || context.operationKey().isBlank()) {
            return Map.of();
        }
        List<WalletTransactionEntity> persisted = walletTransactionRepository.findByOperationKeyOrderByCreatedAtAscIdAsc(context.operationKey());
        if (persisted.isEmpty()) {
            return Map.of();
        }
        Map<String, WalletTransactionEntity> found = new LinkedHashMap<>();
        for (String leg : expectedLegs) {
            persisted.stream()
                    .filter(existing -> leg.equals(existing.getOperationLeg()))
                    .findFirst()
                    .ifPresent(existing -> found.put(leg, existing));
        }
        if (found.isEmpty()) {
            throw new AppException("WALLET_OPERATION_KEY_CONFLICT");
        }
        if (found.size() != expectedLegs.size()) {
            throw new AppException("INCOMPLETE_WALLET_LEDGER_OPERATION");
        }
        BigDecimal expectedAmount = found.values().stream()
                .findFirst()
                .map(WalletTransactionEntity::getAmount)
                .orElse(BigDecimal.ZERO);
        found.forEach((leg, existing) -> validateExistingLeg(existing, accountId, transactionType,
                expectedDirectionForLeg(leg), expectedBalanceTypeForLeg(leg), expectedAmount,
                referenceType, referenceId, description, ensureLeg(context, leg)));
        return found;
    }

    private WalletTransactionEntity resolveDualLegRace(
            WalletOperationContext context,
            String firstLeg,
            String secondLeg,
            Integer accountId,
            String transactionType,
            String referenceType,
            Long referenceId,
            String description
    ) {
        Map<String, WalletTransactionEntity> existing = loadExistingOperation(context, List.of(firstLeg, secondLeg),
                accountId, transactionType, referenceType, referenceId, description);
        if (existing.isEmpty()) {
            throw new AppException("WALLET_OPERATION_KEY_CONFLICT");
        }
        return existing.get(secondLeg);
    }

    private WalletTransactionEntity validateExistingLeg(
            WalletTransactionEntity existing,
            Integer accountId,
            String transactionType,
            String direction,
            String balanceType,
            BigDecimal amount,
            String referenceType,
            Long referenceId,
            String description,
            WalletOperationContext context
    ) {
        boolean same = Objects.equals(existing.getAccountId(), accountId)
                && Objects.equals(safe(existing.getTransactionType()), safe(transactionType))
                && Objects.equals(safe(existing.getDirection()), safe(direction))
                && Objects.equals(safe(existing.getBalanceType()), safe(balanceType))
                && money(existing.getAmount()).compareTo(money(amount)) == 0
                && Objects.equals(safe(existing.getReferenceType()), safe(referenceType))
                && Objects.equals(existing.getReferenceId(), referenceId)
                && Objects.equals(existing.getContractId(), context.contractId())
                && Objects.equals(existing.getMilestoneId(), context.milestoneId())
                && Objects.equals(safe(existing.getDescription()), safe(description))
                && Objects.equals(safe(existing.getMetadata()), safe(context.metadata()));
        if (!same) {
            throw new AppException("WALLET_OPERATION_KEY_CONFLICT");
        }
        return existing;
    }

    private WalletOperationContext normalizeContext(WalletOperationContext context) {
        return context == null ? WalletOperationContext.empty() : context;
    }

    private WalletOperationContext ensureLeg(WalletOperationContext context, String defaultLeg) {
        if (context == null) {
            return WalletOperationContext.builder().operationLeg(defaultLeg).build();
        }
        if (context.operationLeg() == null || context.operationLeg().isBlank()) {
            return context.withOperationLeg(defaultLeg);
        }
        return context;
    }

    private boolean hasOperationIdentity(WalletOperationContext context) {
        return context != null
                && context.operationKey() != null
                && !context.operationKey().isBlank()
                && context.operationLeg() != null
                && !context.operationLeg().isBlank();
    }

    private String expectedDirectionForLeg(String leg) {
        if (leg == null) {
            return "";
        }
        if (leg.endsWith("_CREDIT")) {
            return "CREDIT";
        }
        if (leg.endsWith("_DEBIT")) {
            return "DEBIT";
        }
        if (leg.endsWith("_HOLD")) {
            return "HOLD";
        }
        if (leg.endsWith("_RELEASE")) {
            return "RELEASE";
        }
        return "";
    }

    private String expectedBalanceTypeForLeg(String leg) {
        if (leg == null) {
            return "";
        }
        if (leg.startsWith("AVAILABLE_")) {
            return BALANCE_AVAILABLE;
        }
        if (leg.startsWith("ESCROW_")) {
            return BALANCE_ESCROW;
        }
        if (leg.startsWith("HOLDING_")) {
            return BALANCE_HOLDING;
        }
        return "";
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

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public record WalletOperationContext(
            Integer contractId,
            Integer milestoneId,
            String metadata,
            String operationKey,
            String operationLeg,
            Long paymentOrderId
    ) {
        public static WalletOperationContext empty() {
            return new WalletOperationContext(null, null, null, null, null, null);
        }

        public WalletOperationContext withOperationLeg(String leg) {
            return new WalletOperationContext(contractId, milestoneId, metadata, operationKey, leg, paymentOrderId);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private Integer contractId;
            private Integer milestoneId;
            private String metadata;
            private String operationKey;
            private String operationLeg;
            private Long paymentOrderId;

            public Builder contractId(Integer contractId) {
                this.contractId = contractId;
                return this;
            }

            public Builder milestoneId(Integer milestoneId) {
                this.milestoneId = milestoneId;
                return this;
            }

            public Builder metadata(String metadata) {
                this.metadata = metadata;
                return this;
            }

            public Builder operationKey(String operationKey) {
                this.operationKey = operationKey;
                return this;
            }

            public Builder operationLeg(String operationLeg) {
                this.operationLeg = operationLeg;
                return this;
            }

            public Builder paymentOrderId(Long paymentOrderId) {
                this.paymentOrderId = paymentOrderId;
                return this;
            }

            public WalletOperationContext build() {
                return new WalletOperationContext(contractId, milestoneId, metadata, operationKey, operationLeg, paymentOrderId);
            }
        }
    }
}
