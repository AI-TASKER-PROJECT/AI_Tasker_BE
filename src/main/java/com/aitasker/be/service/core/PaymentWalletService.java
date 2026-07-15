/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/PaymentWalletService.java
 * Day la file gi: File service chua nghiep vu chinh, dieu phoi repository va kiem tra luat xu ly cua he thong.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.dto.admin.MembershipPackageRequest;
import com.aitasker.be.dto.payment.CreditPurchaseRequest;
import com.aitasker.be.dto.payment.DepositRefundRequest;
import com.aitasker.be.dto.payment.PaymentActionResponse;
import com.aitasker.be.dto.payment.QuotaResponse;
import com.aitasker.be.dto.payment.WalletTransactionHistoryResponse;
import com.aitasker.be.dto.payment.WithdrawalRequest;
import com.aitasker.be.dto.payment.WithdrawalReviewRequest;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.entity.ContractDepositEntity;
import com.aitasker.be.entity.ContractEntity;
import com.aitasker.be.entity.ContractMilestoneEntity;
import com.aitasker.be.entity.ExpertProfileEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.MembershipPackageEntity;
import com.aitasker.be.entity.MembershipPurchaseEntity;
import com.aitasker.be.entity.MilestoneEntity;
import com.aitasker.be.entity.PaymentOrderEntity;
import com.aitasker.be.entity.QuotaUsageLogEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.entity.SystemSettingEntity;
import com.aitasker.be.entity.SystemWalletEntity;
import com.aitasker.be.entity.UserQuotaEntity;
import com.aitasker.be.entity.WalletTransactionEntity;
import com.aitasker.be.entity.WithdrawalRequestEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.repository.BusinessProfileRepository;
import com.aitasker.be.repository.ContractDepositRepository;
import com.aitasker.be.repository.ContractMilestoneRepository;
import com.aitasker.be.repository.ContractRepository;
import com.aitasker.be.repository.ExpertProfileRepository;
import com.aitasker.be.repository.JobRepository;
import com.aitasker.be.repository.MembershipPackageRepository;
import com.aitasker.be.repository.MembershipPurchaseRepository;
import com.aitasker.be.repository.MilestoneRepository;
import com.aitasker.be.repository.PaymentOrderRepository;
import com.aitasker.be.repository.QuotaUsageLogRepository;
import com.aitasker.be.repository.SystemSettingRepository;
import com.aitasker.be.repository.UserQuotaRepository;
import com.aitasker.be.repository.WalletTransactionRepository;
import com.aitasker.be.repository.WithdrawalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

// Note: Annotation nay cho Spring quan ly class nhu mot service chua nghiep vu.
@Service
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class PaymentWalletService {
    public static final String ACTION_PURCHASE_MEMBERSHIP = "Mua gói thành viên";
    public static final String ACTION_PURCHASE_CREDIT = "Mua lượt sử dụng";
    public static final String ACTION_CONSUME_QUOTA = "Sử dụng quota";
    public static final String ACTION_PAY_CONTRACT_DEPOSIT = "Trả tiền ký quỹ hợp đồng";
    public static final String ACTION_REFUND_CONTRACT_DEPOSIT = "Xử lý hoàn ký quỹ hợp đồng";
    public static final String ACTION_CREATE_WITHDRAWAL = "Tạo yêu cầu rút tiền";
    public static final String ACTION_APPROVE_WITHDRAWAL = "Duyệt yêu cầu rút tiền";
    public static final String ACTION_REJECT_WITHDRAWAL = "Từ chối yêu cầu rút tiền";

    private static final String ROLE_BUSINESS = "BUSINESS";
    private static final String ROLE_EXPERT = "EXPERT";
    private static final String QUOTA_JOB_POST = "JOB_POST";
    private static final String QUOTA_PROPOSAL = "PROPOSAL";
    private static final String TOPUP_ENDPOINT = "/api/payments/payos/create";
    private static final String TIER_PREMIUM = "PREMIUM";
    private static final String TIER_PLUS = "PLUS";
    private static final String TIER_STANDARD = "STANDARD";
    private static final String TIER_BASIC = "BASIC";
    private static final int INITIAL_FREE_QUOTA = 3;

    private final AccessService accessService;
    private final SystemWalletService systemWalletService;
    private final WalletLedgerService walletLedgerService;
    private final AuditLogService auditLogService;
    private final MembershipPackageRepository membershipPackageRepository;
    private final MembershipPurchaseRepository membershipPurchaseRepository;
    private final UserQuotaRepository userQuotaRepository;
    private final QuotaUsageLogRepository quotaUsageLogRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final ExpertProfileRepository expertProfileRepository;
    private final ContractRepository contractRepository;
    private final ContractDepositRepository contractDepositRepository;
    private final ContractMilestoneRepository contractMilestoneRepository;
    private final JobRepository jobRepository;
    private final MilestoneRepository milestoneRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    // Note: Ham `listPackagesForCurrentRole` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public List<MembershipPackageEntity> listPackagesForCurrentRole() {
        AccountEntity actor = accessService.currentAccount();
        String role = actor.getRole().getRoleName();
        if (!ROLE_BUSINESS.equals(role) && !ROLE_EXPERT.equals(role)) {
            throw new AppException("INVALID_ROLE");
        }
        return membershipPackageRepository.findByRoleTypeAndIsActiveTrueOrderByPriceAsc(role);
    }

    @Transactional(readOnly = true)
    public List<MembershipPackageEntity> listMembershipPackagesForAdmin(Boolean activeOnly) {
        accessService.requireRole("ADMIN");
        if (Boolean.TRUE.equals(activeOnly)) {
            return membershipPackageRepository.findByIsActiveTrueOrderByRoleTypeAscPriceAscPackageNameAsc();
        }
        return membershipPackageRepository.findAll();
    }

    @Transactional
    public MembershipPackageEntity createMembershipPackage(MembershipPackageRequest request) {
        accessService.requireRole("ADMIN");
        AccountEntity actor = accessService.currentAccount();
        validateMembershipPackageRequest(request, true);
        String code = normalizePackageCode(request.getPackageCode());
        if (membershipPackageRepository.findByPackageCodeIgnoreCase(code).isPresent()) {
            throw new AppException("PACKAGE CODE DA TON TAI");
        }
        MembershipPackageEntity entity = MembershipPackageEntity.builder()
                .roleType(normalizePackageRole(request.getRoleType()))
                .packageCode(code)
                .packageName(request.getPackageName().trim())
                .price(money(request.getPrice()))
                .badgeDurationDays(request.getBadgeDurationDays())
                .jobPostQuota(nonNegativeInt(request.getJobPostQuota()))
                .proposalQuota(nonNegativeInt(request.getProposalQuota()))
                .recommendVisibility(request.getRecommendVisibility() != null && request.getRecommendVisibility())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();
        MembershipPackageEntity saved = membershipPackageRepository.save(entity);
        auditLogService.record("MEMBERSHIP_PACKAGE_CREATED", "membership_packages", String.valueOf(saved.getPackageId()), actor.getAccountId());
        return saved;
    }

    @Transactional
    public MembershipPackageEntity updateMembershipPackage(Long packageId, MembershipPackageRequest request) {
        accessService.requireRole("ADMIN");
        AccountEntity actor = accessService.currentAccount();
        validateMembershipPackageRequest(request, false);
        MembershipPackageEntity entity = membershipPackageRepository.findById(packageId)
                .orElseThrow(() -> new NotFoundException("PACKAGE_NOT_FOUND"));
        if (request.getRoleType() != null && !request.getRoleType().isBlank()) {
            entity.setRoleType(normalizePackageRole(request.getRoleType()));
        }
        if (request.getPackageCode() != null && !request.getPackageCode().isBlank()) {
            String code = normalizePackageCode(request.getPackageCode());
            membershipPackageRepository.findByPackageCodeIgnoreCase(code)
                    .filter(existing -> !existing.getPackageId().equals(packageId))
                    .ifPresent(existing -> { throw new AppException("PACKAGE CODE DA TON TAI"); });
            entity.setPackageCode(code);
        }
        if (request.getPackageName() != null && !request.getPackageName().isBlank()) {
            entity.setPackageName(request.getPackageName().trim());
        }
        if (request.getPrice() != null) entity.setPrice(money(request.getPrice()));
        if (request.getBadgeDurationDays() != null) {
            if (request.getBadgeDurationDays() <= 0) throw new AppException("BADGE DURATION DAYS KHONG HOP LE");
            entity.setBadgeDurationDays(request.getBadgeDurationDays());
        }
        if (request.getJobPostQuota() != null) entity.setJobPostQuota(nonNegativeInt(request.getJobPostQuota()));
        if (request.getProposalQuota() != null) entity.setProposalQuota(nonNegativeInt(request.getProposalQuota()));
        if (request.getRecommendVisibility() != null) entity.setRecommendVisibility(request.getRecommendVisibility());
        if (request.getIsActive() != null) entity.setIsActive(request.getIsActive());
        MembershipPackageEntity saved = membershipPackageRepository.save(entity);
        auditLogService.record("MEMBERSHIP_PACKAGE_UPDATED", "membership_packages", String.valueOf(saved.getPackageId()), actor.getAccountId());
        return saved;
    }

    @Transactional
    public MembershipPackageEntity deleteMembershipPackage(Long packageId) {
        accessService.requireRole("ADMIN");
        AccountEntity actor = accessService.currentAccount();
        MembershipPackageEntity entity = membershipPackageRepository.findById(packageId)
                .orElseThrow(() -> new NotFoundException("PACKAGE_NOT_FOUND"));
        entity.setIsActive(false);
        MembershipPackageEntity saved = membershipPackageRepository.save(entity);
        auditLogService.record("MEMBERSHIP_PACKAGE_DELETED", "membership_packages", String.valueOf(saved.getPackageId()), actor.getAccountId());
        return saved;
    }

    @Transactional
    // Note: Ham `purchaseMembership` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public PaymentActionResponse<MembershipPurchaseEntity> purchaseMembership(Long packageId) {
        AccountEntity actor = requireApprovedBusinessOrExpert();
        String role = actor.getRole().getRoleName();
        MembershipPackageEntity membershipPackage = membershipPackageRepository.findByPackageIdAndIsActiveTrue(packageId)
                .orElseThrow(() -> new NotFoundException("PACKAGE_NOT_FOUND"));
        if (!role.equals(membershipPackage.getRoleType())) {
            throw new AppException("INVALID_ROLE");
        }

        BigDecimal price = money(membershipPackage.getPrice());
        BigDecimal available = walletLedgerService.availableBalance(actor.getAccountId());
        if (available.compareTo(price) < 0) {
            return insufficient(available, price, "INSUFFICIENT_BALANCE");
        }

        WalletTransactionEntity walletTransaction = walletLedgerService.debitAvailable(
                actor.getAccountId(),
                price,
                "MEMBERSHIP_PURCHASE",
                "MEMBERSHIP",
                membershipPackage.getPackageId(),
                membershipPackage.getPackageName()
        );

        UserQuotaEntity quota = ensureQuotaForAccountForUpdate(actor);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime badgeStart = quota.getBadgeExpiredAt() != null && quota.getBadgeExpiredAt().isAfter(now)
                ? quota.getBadgeExpiredAt()
                : now;
        LocalDateTime badgeEnd = badgeStart.plusDays(membershipPackage.getBadgeDurationDays());
        quota.setBadgeExpiredAt(badgeEnd);
        if (isPremiumPackage(membershipPackage)) {
            LocalDateTime premiumStart = quota.getPremiumExpiredAt() != null && quota.getPremiumExpiredAt().isAfter(now)
                    ? quota.getPremiumExpiredAt()
                    : now;
            quota.setPremiumExpiredAt(premiumStart.plusDays(membershipPackage.getBadgeDurationDays()));
        }
        userQuotaRepository.save(quota);

        MembershipPurchaseEntity purchase = membershipPurchaseRepository.save(MembershipPurchaseEntity.builder()
                .accountId(actor.getAccountId())
                .packageId(membershipPackage.getPackageId())
                .amount(price)
                .status("SUCCESS")
                .badgeStartAt(badgeStart)
                .badgeEndAt(badgeEnd)
                .walletTransactionId(walletTransaction.getId())
                .build());

        if (nonNegativeInt(membershipPackage.getJobPostQuota()) > 0) {
            grantQuota(actor.getAccountId(), QUOTA_JOB_POST, membershipPackage.getJobPostQuota(), "MEMBERSHIP", purchase.getPurchaseId());
        }
        if (nonNegativeInt(membershipPackage.getProposalQuota()) > 0) {
            grantQuota(actor.getAccountId(), QUOTA_PROPOSAL, membershipPackage.getProposalQuota(), "MEMBERSHIP", purchase.getPurchaseId());
        }

        systemWalletService.syncWallet();
        auditLogService.record(ACTION_PURCHASE_MEMBERSHIP, "membership_purchases",
                String.valueOf(purchase.getPurchaseId()), actor.getAccountId());
        return completed(purchase, "MEMBERSHIP_PURCHASE_SUCCESS");
    }

    @Transactional
    // Note: Ham `purchaseJobPostCredits` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public PaymentActionResponse<UserQuotaEntity> purchaseJobPostCredits(CreditPurchaseRequest request) {
        AccountEntity actor = requireApprovedRole(ROLE_BUSINESS);
        int quantity = requirePositiveQuantity(request);
        BigDecimal requiredAmount = settingAmount("credit.job_post.price_vnd", BigDecimal.valueOf(200))
                .multiply(BigDecimal.valueOf(quantity));
        BigDecimal available = walletLedgerService.availableBalance(actor.getAccountId());
        if (available.compareTo(requiredAmount) < 0) {
            return insufficient(available, requiredAmount, "INSUFFICIENT_BALANCE");
        }
        WalletTransactionEntity tx = walletLedgerService.debitAvailable(
                actor.getAccountId(),
                requiredAmount,
                "CREDIT_PURCHASE",
                "CREDIT_PURCHASE",
                actor.getAccountId().longValue(),
                "Buy job-post credits: " + quantity
        );
        tx.setReferenceId(tx.getId());
        walletTransactionRepository.save(tx);
        UserQuotaEntity quota = grantQuota(actor.getAccountId(), QUOTA_JOB_POST, quantity, "CREDIT_PURCHASE", tx.getId());
        auditLogService.record(ACTION_PURCHASE_CREDIT, "wallet_transactions", String.valueOf(tx.getId()), actor.getAccountId());
        return completed(quota, "CREDIT_PURCHASE_SUCCESS");
    }

    @Transactional
    // Note: Ham `purchaseProposalCredits` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public PaymentActionResponse<UserQuotaEntity> purchaseProposalCredits(CreditPurchaseRequest request) {
        AccountEntity actor = requireApprovedRole(ROLE_EXPERT);
        int quantity = requirePositiveQuantity(request);
        BigDecimal requiredAmount = settingAmount("credit.proposal.price_vnd", BigDecimal.valueOf(100))
                .multiply(BigDecimal.valueOf(quantity));
        BigDecimal available = walletLedgerService.availableBalance(actor.getAccountId());
        if (available.compareTo(requiredAmount) < 0) {
            return insufficient(available, requiredAmount, "INSUFFICIENT_BALANCE");
        }
        WalletTransactionEntity tx = walletLedgerService.debitAvailable(
                actor.getAccountId(),
                requiredAmount,
                "CREDIT_PURCHASE",
                "CREDIT_PURCHASE",
                actor.getAccountId().longValue(),
                "Buy proposal credits: " + quantity
        );
        tx.setReferenceId(tx.getId());
        walletTransactionRepository.save(tx);
        UserQuotaEntity quota = grantQuota(actor.getAccountId(), QUOTA_PROPOSAL, quantity, "CREDIT_PURCHASE", tx.getId());
        auditLogService.record(ACTION_PURCHASE_CREDIT, "wallet_transactions", String.valueOf(tx.getId()), actor.getAccountId());
        return completed(quota, "CREDIT_PURCHASE_SUCCESS");
    }

    @Transactional
    // Note: Ham `currentQuota` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public QuotaResponse currentQuota() {
        AccountEntity actor = requireApprovedBusinessOrExpert();
        UserQuotaEntity quota = ensureQuotaForAccountForUpdate(actor);
        ActivePackage activePackage = resolveActivePackage(actor, quota, LocalDateTime.now());
        return QuotaResponse.from(
                quota,
                isPremiumActive(quota),
                activePackage.code(),
                activePackage.name()
        );
    }

    @Transactional
    // Note: Ham `ensureQuotaForAccount` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public void ensureQuotaForAccount(AccountEntity account) {
        ensureQuotaForAccountForUpdate(account);
    }

    @Transactional
    // Note: Ham `consumeJobPostCredit` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public void consumeJobPostCredit(AccountEntity account, Long jobId) {
        int remainingBalance = consumeQuota(account, QUOTA_JOB_POST, "JOB", jobId);
        String jobTitle = jobId == null ? null : jobRepository.findById(toInt(jobId))
                .map(JobEntity::getTitle)
                .orElse(null);
        notificationService.notifyJobPostQuotaConsumed(
                account.getAccountId(),
                account.getAccountId(),
                jobId,
                jobTitle,
                remainingBalance
        );
    }

    @Transactional
    // Note: Ham `consumeProposalCredit` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public void consumeProposalCredit(AccountEntity account, Long proposalId) {
        consumeQuota(account, QUOTA_PROPOSAL, "PROPOSAL", proposalId);
    }

    @Transactional(readOnly = true)
    // Note: Ham `requirePremiumRecommendationAccess` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public void requirePremiumRecommendationAccess(Long jobPostingId) {
        AccountEntity actor = requireApprovedRole(ROLE_BUSINESS);
        BusinessProfileEntity business = businessProfileRepository.findByAccountId(actor.getAccountId())
                .orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
        JobEntity job = jobRepository.findById(toInt(jobPostingId))
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB"));
        if (!business.getBusinessId().equals(job.getBusinessId())) {
            throw new AppException("BAN KHONG CO QUYEN XEM RECOMMENDATION JOB NAY");
        }
        UserQuotaEntity quota = userQuotaRepository.findByAccountId(actor.getAccountId())
                .orElseThrow(() -> new AppException("PREMIUM_REQUIRED"));
        if (!isPremiumActive(quota)) {
            throw new AppException("PREMIUM_REQUIRED");
        }
    }

    @Transactional
    // Note: Ham `payContractDeposit` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public PaymentActionResponse<ContractDepositEntity> payContractDeposit(Integer contractId) {
        AccountEntity actor = requireApprovedRole(ROLE_BUSINESS);
        BusinessProfileEntity business = businessProfileRepository.findByAccountId(actor.getAccountId())
                .orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("CONTRACT_NOT_FOUND"));
        if (!business.getBusinessId().equals(contract.getBusinessId())) {
            throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        }
        if (!"PENDING".equals(contract.getStatus())) {
            throw new AppException("CONTRACT_INVALID_STATUS");
        }
        return fundContractDeposit(contract, actor, ROLE_BUSINESS, business.getBusinessId(), new BigDecimal("20.00"));
    }

    @Transactional
    public PaymentActionResponse<ContractDepositEntity> payExpertContractDeposit(Integer contractId) {
        AccountEntity actor = requireApprovedRole(ROLE_EXPERT);
        ExpertProfileEntity expert = expertProfileRepository.findByAccountId(actor.getAccountId())
                .orElseThrow(() -> new NotFoundException("CHUA CO EXPERT PROFILE"));
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("CONTRACT_NOT_FOUND"));
        if (!expert.getExpertId().equals(contract.getExpertId())) {
            throw new AppException("BAN KHONG THUOC CONTRACT NAY");
        }
        return fundContractDeposit(contract, actor, ROLE_EXPERT, contract.getBusinessId(), new BigDecimal("10.00"));
    }

    private PaymentActionResponse<ContractDepositEntity> fundContractDeposit(
            ContractEntity contract,
            AccountEntity actor,
            String ownerRole,
            Integer businessId,
            BigDecimal requiredPercentage
    ) {
        Integer contractId = contract.getContractId();
        if (!ContractEntity.STATUS_PENDING.equals(contract.getStatus())) {
            throw new AppException("CONTRACT_INVALID_STATUS");
        }
        BigDecimal depositAmount = percentageAmount(contract, requiredPercentage);
        Optional<ContractDepositEntity> existing = contractDepositRepository
                .findByContractIdAndOwnerRoleForUpdate(contractId, ownerRole);
        if (existing.filter(deposit -> "HELD".equals(deposit.getStatus())
                && money(deposit.getHeldAmount()).compareTo(depositAmount) == 0).isPresent()) {
        boolean activated = activateContractAfterBothDeposits(contract, LocalDateTime.now(), actor.getAccountId());
        if (activated) {
            notifyBothParticipants(contract, actor.getAccountId(), "CONTRACT_ACTIVATED_AFTER_DUAL_DEPOSIT",
                    "Hợp đồng đã được kích hoạt", "Hai bên đã hoàn tất ký quỹ, hợp đồng bắt đầu thực thi.");
        }
            return completed(existing.get(), "CONTRACT_DEPOSIT_ALREADY_HELD");
        }
        if (existing.filter(deposit -> !"UNPAID".equals(deposit.getStatus())).isPresent()) {
            throw new AppException("CONTRACT_DEPOSIT_ALREADY_RESOLVED");
        }
        BigDecimal available = walletLedgerService.availableBalance(actor.getAccountId());
        if (available.compareTo(depositAmount) < 0) {
            return insufficient(available, depositAmount, "INSUFFICIENT_BALANCE");
        }

        ContractDepositEntity deposit = existing
                .orElseGet(() -> ContractDepositEntity.builder()
                        .contractId(contractId)
                        .businessId(businessId)
                        .ownerAccountId(actor.getAccountId())
                        .ownerRole(ownerRole)
                        .requiredPercentage(requiredPercentage)
                        .requiredAmount(depositAmount)
                        .depositAmount(depositAmount)
                        .heldAmount(BigDecimal.ZERO)
                        .refundedAmount(BigDecimal.ZERO)
                        .resolvedAmount(BigDecimal.ZERO)
                        .penaltyAmount(BigDecimal.ZERO)
                        .status("UNPAID")
                        .build());

        String txType = ROLE_BUSINESS.equals(ownerRole)
                ? "CONTRACT_SECURITY_DEPOSIT_HOLD" : "EXPERT_CONTRACT_DEPOSIT_HOLD";
        WalletTransactionEntity tx = walletLedgerService.holdEscrowFromAvailable(
                actor.getAccountId(),
                depositAmount,
                txType,
                "CONTRACT_DEPOSIT",
                Long.valueOf(contractId),
                ownerRole + " contract deposit",
                WalletLedgerService.WalletOperationContext.builder()
                        .contractId(contractId)
                        .metadata("{\"ownerRole\":\"" + ownerRole + "\",\"requiredPercentage\":" + requiredPercentage + "}")
                        .operationKey("CONTRACT_DEPOSIT_HOLD:" + ensurePersistedDepositId(deposit))
                        .build()
        );
        LocalDateTime now = LocalDateTime.now();
        deposit.setOwnerAccountId(actor.getAccountId());
        deposit.setOwnerRole(ownerRole);
        deposit.setRequiredPercentage(requiredPercentage);
        deposit.setRequiredAmount(depositAmount);
        deposit.setDepositAmount(depositAmount);
        deposit.setHeldAmount(depositAmount);
        deposit.setRefundedAmount(BigDecimal.ZERO);
        deposit.setResolvedAmount(BigDecimal.ZERO);
        deposit.setPenaltyAmount(BigDecimal.ZERO);
        deposit.setStatus("HELD");
        deposit.setHoldTransactionId(tx.getId());
        deposit.setPaidAt(now);
        ContractDepositEntity savedDeposit = contractDepositRepository.save(deposit);

        boolean activated = activateContractAfterBothDeposits(contract, now, actor.getAccountId());
        auditLogService.record(ROLE_EXPERT.equals(ownerRole) ? "EXPERT_CONTRACT_DEPOSIT_HELD" : "BUSINESS_CONTRACT_DEPOSIT_HELD",
                "contract_deposits",
                String.valueOf(savedDeposit.getDepositId()), actor.getAccountId());
        notifyDepositHeld(contract, actor.getAccountId(), ownerRole);
        if (activated) {
            notifyBothParticipants(contract, actor.getAccountId(), "CONTRACT_ACTIVATED_AFTER_DUAL_DEPOSIT",
                    "Hợp đồng đã được kích hoạt", "Hai bên đã hoàn tất ký quỹ, hợp đồng bắt đầu thực thi.");
        }
        return completed(savedDeposit, "CONTRACT_DEPOSIT_HELD");
    }

    @Transactional
    // Note: Ham `refundContractDeposit` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public ContractDepositEntity refundContractDeposit(Integer contractId, DepositRefundRequest request) {
        AccountEntity admin = accessService.currentAccount();
        accessService.requireRole("ADMIN");
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("CONTRACT_NOT_FOUND"));
        if (!List.of(ContractEntity.STATUS_COMPLETED, ContractEntity.STATUS_TERMINATED).contains(contract.getStatus())) {
            throw new AppException("CONTRACT_INVALID_STATUS");
        }
        ContractDepositEntity deposit = contractDepositRepository.findByContractIdAndOwnerRoleForUpdate(contractId, ROLE_BUSINESS)
                .orElseThrow(() -> new NotFoundException("CONTRACT_DEPOSIT_NOT_FOUND"));
        if (!List.of("HELD", "PARTIALLY_REFUNDED").contains(deposit.getStatus())) {
            throw new AppException("DEPOSIT_INVALID_STATUS");
        }

        // Spec 4.1 / 9.7 / 11.8 / invariant 15: hoan ky quy hop dong la nhi phan -
        // hoac hoan 100% held_amount, hoac giu 0%. Khong cho hoan mot phan tuy y (chong tich thu ngam).
        BigDecimal heldAmount = money(deposit.getHeldAmount());
        BigDecimal requested = request == null ? null : request.getRefundAmount();
        BigDecimal refundAmount;
        if (requested == null) {
            // Mac dinh luong happy path (9.7 / 11.8): hoan toan bo.
            refundAmount = heldAmount;
        } else {
            refundAmount = money(requested);
            if (refundAmount.compareTo(BigDecimal.ZERO) != 0 && refundAmount.compareTo(heldAmount) != 0) {
                throw new AppException("REFUND_MUST_BE_FULL_OR_ZERO");
            }
        }
        Integer businessAccountId = businessProfileRepository.findById(contract.getBusinessId())
                .map(BusinessProfileEntity::getAccountId)
                .orElseThrow(() -> new NotFoundException("CHUA CO BUSINESS PROFILE"));

        WalletTransactionEntity finalTx = null;
        if (refundAmount.signum() > 0) {
            finalTx = walletLedgerService.releaseEscrowToAvailable(
                    businessAccountId,
                    refundAmount,
                    "CONTRACT_SECURITY_DEPOSIT_REFUND",
                    "CONTRACT_DEPOSIT",
                    deposit.getDepositId(),
                    "Refund contract security deposit",
                    WalletLedgerService.WalletOperationContext.builder()
                            .contractId(contractId)
                            .operationKey("CONTRACT_DEPOSIT_REFUND:" + deposit.getDepositId() + ":STANDARD_REFUND")
                            .build()
            );
        }
        BigDecimal resolvedAmount = heldAmount.subtract(refundAmount);
        if (resolvedAmount.signum() > 0) {
            finalTx = walletLedgerService.debitEscrow(
                    businessAccountId,
                    resolvedAmount,
                    "CONTRACT_SECURITY_DEPOSIT_RESOLVED",
                    "CONTRACT_DEPOSIT",
                    deposit.getDepositId(),
                    "Admin resolved contract security deposit",
                    WalletLedgerService.WalletOperationContext.builder()
                            .contractId(contractId)
                            .operationKey("CONTRACT_DEPOSIT_REFUND:" + deposit.getDepositId() + ":ADMIN_RESOLVED")
                            .build()
            );
        }

        deposit.setHeldAmount(BigDecimal.ZERO);
        deposit.setRefundedAmount(refundAmount);
        deposit.setResolvedAmount(resolvedAmount);
        deposit.setRefundTransactionId(finalTx == null ? null : finalTx.getId());
        deposit.setAdminId(admin.getAccountId());
        deposit.setAdminNote(request == null ? null : request.getAdminNote());
        deposit.setRefundedAt(LocalDateTime.now());
        deposit.setStatus(refundStatus(refundAmount, resolvedAmount));
        ContractDepositEntity saved = contractDepositRepository.save(deposit);

        boolean closed = closeWhenBothDepositsResolved(contract, admin.getAccountId());
        auditLogService.record("CONTRACT_DEPOSIT_REFUNDED", "contract_deposits",
                String.valueOf(saved.getDepositId()), admin.getAccountId());
        notifyBusiness(contract, admin.getAccountId(), "CONTRACT_DEPOSIT_REFUNDED",
                "Ký quỹ hợp đồng đã được hoàn", "Ký quỹ bảo đảm của doanh nghiệp đã được xử lý hoàn/trừ theo quyết định.");
        if (closed) {
            notifyBothParticipants(contract, admin.getAccountId(), "CONTRACT_CLOSED",
                    "Hợp đồng đã đóng", "Hợp đồng đã đóng sau khi hoàn tất xử lý ký quỹ.");
        }
        return saved;
    }

    @Transactional
    public List<ContractDepositEntity> refundParticipantDeposits(Integer contractId, DepositRefundRequest request) {
        AccountEntity admin = accessService.currentAccount();
        accessService.requireRole("ADMIN");
        return refundParticipantDepositsInternal(
                contractId, admin.getAccountId(), admin.getAccountId(), request == null ? null : request.getAdminNote());
    }

    @Transactional
    public List<ContractDepositEntity> autoRefundParticipantDeposits(Integer contractId, Integer operationalActorAccountId) {
        return refundParticipantDepositsInternal(contractId, operationalActorAccountId, null, null);
    }

    private List<ContractDepositEntity> refundParticipantDepositsInternal(
            Integer contractId, Integer operationalActorAccountId, Integer adminId, String adminNote) {
        ContractEntity contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("CONTRACT_NOT_FOUND"));
        if (!List.of(ContractEntity.STATUS_COMPLETED, ContractEntity.STATUS_TERMINATED).contains(contract.getStatus())) {
            throw new AppException("CONTRACT_INVALID_STATUS");
        }
        List<ContractDepositEntity> deposits = contractDepositRepository.findByContractIdOrderByOwnerRoleAsc(contractId);
        if (deposits.size() < 2) throw new AppException("EXPERT_CONTRACT_DEPOSIT_NOT_HELD");
        LocalDateTime now = LocalDateTime.now();
        for (ContractDepositEntity deposit : deposits) {
            if (!"HELD".equals(deposit.getStatus())) continue;
            BigDecimal held = money(deposit.getHeldAmount());
            WalletTransactionEntity tx = walletLedgerService.releaseEscrowToAvailable(
                    deposit.getOwnerAccountId(), held,
                    ROLE_EXPERT.equals(deposit.getOwnerRole())
                            ? "EXPERT_CONTRACT_DEPOSIT_REFUND" : "CONTRACT_SECURITY_DEPOSIT_REFUND",
                    "CONTRACT_DEPOSIT", deposit.getDepositId(), "Refund participant contract deposit",
                    WalletLedgerService.WalletOperationContext.builder()
                            .contractId(contractId)
                            .operationKey("CONTRACT_DEPOSIT_REFUND:" + deposit.getDepositId() + ":STANDARD_REFUND")
                            .build());
            deposit.setHeldAmount(BigDecimal.ZERO);
            deposit.setRefundedAmount(money(deposit.getRefundedAmount()).add(held));
            deposit.setRefundTransactionId(tx.getId());
            deposit.setAdminId(adminId);
            deposit.setAdminNote(adminNote);
            deposit.setRefundedAt(now);
            deposit.setResolvedAt(now);
            deposit.setResolutionType("STANDARD_REFUND");
            deposit.setStatus("REFUNDED");
            contractDepositRepository.save(deposit);
        }
        boolean closed = closeWhenBothDepositsResolved(contract, operationalActorAccountId);
        auditLogService.record("PARTICIPANT_DEPOSITS_REFUNDED", "contracts",
                String.valueOf(contractId), operationalActorAccountId);
        notifyBothParticipants(contract, operationalActorAccountId, "PARTICIPANT_DEPOSITS_REFUNDED",
                "Ký quỹ hai bên đã được hoàn", "Ký quỹ tham gia hợp đồng của hai bên đã được xử lý hoàn.");
        if (closed) {
            notifyBothParticipants(contract, operationalActorAccountId, "CONTRACT_CLOSED",
                    "Hợp đồng đã đóng", "Hợp đồng đã đóng sau khi hoàn tất xử lý ký quỹ.");
        }
        return contractDepositRepository.findByContractIdOrderByOwnerRoleAsc(contractId);
    }

    @Transactional
    public ContractEntity immediateTerminateContract(ContractEntity contract, AccountEntity initiator, String initiatingRole) {
        ContractDepositEntity businessDeposit = contractDepositRepository
                .findByContractIdAndOwnerRoleForUpdate(contract.getContractId(), ROLE_BUSINESS)
                .filter(item -> "HELD".equals(item.getStatus()))
                .orElseThrow(() -> new AppException("BUSINESS_CONTRACT_DEPOSIT_NOT_HELD"));
        ContractDepositEntity expertDeposit = contractDepositRepository
                .findByContractIdAndOwnerRoleForUpdate(contract.getContractId(), ROLE_EXPERT)
                .filter(item -> "HELD".equals(item.getStatus()))
                .orElseThrow(() -> new AppException("EXPERT_CONTRACT_DEPOSIT_NOT_HELD"));
        Integer businessAccountId = businessDeposit.getOwnerAccountId();
        Integer expertAccountId = expertDeposit.getOwnerAccountId();
        BigDecimal penalty = percentageAmount(contract, new BigDecimal("10.00"));

        for (ContractMilestoneEntity item : contractMilestoneRepository
                .findByContractIdOrderByOrderIndexAsc(contract.getContractId())) {
            if (ContractMilestoneEntity.STATUS_COMPLETED.equals(item.getStatus())) continue;
            if (List.of(ContractMilestoneEntity.STATUS_DEPOSITED, ContractMilestoneEntity.STATUS_IN_PROGRESS,
                    ContractMilestoneEntity.STATUS_OVERDUE).contains(item.getStatus())
                    && item.getEscrowReleasedAt() == null) {
                WalletTransactionEntity refund = walletLedgerService.releaseEscrowToAvailable(
                        businessAccountId, item.getFinalBudget(), WalletTransactionEntity.TX_ESCROW_REFUND,
                        "MILESTONE", item.getJobMilestoneId().longValue(), "Immediate termination milestone refund",
                        WalletLedgerService.WalletOperationContext.builder()
                                .contractId(contract.getContractId())
                                .milestoneId(item.getJobMilestoneId())
                                .operationKey("MILESTONE_ESCROW_REFUND:" + contract.getContractId() + ":" + item.getJobMilestoneId() + ":IMMEDIATE_TERMINATION")
                                .metadata("{\"settlementSourceType\":\"IMMEDIATE_TERMINATION\"}")
                                .build());
                item.setEscrowReleasedAt(LocalDateTime.now());
                item.setSettlementSourceType("IMMEDIATE_TERMINATION");
                item.setSettlementSourceId(contract.getContractId().longValue());
            }
            item.setStatus(ContractMilestoneEntity.STATUS_CANCELLED);
            contractMilestoneRepository.save(item);
            milestoneRepository.findById(item.getJobMilestoneId()).ifPresent(live -> {
                live.setStatus(ContractMilestoneEntity.STATUS_CANCELLED);
                live.setEscrowReleasedAt(item.getEscrowReleasedAt());
                live.setSettlementSourceType(item.getSettlementSourceType());
                live.setSettlementSourceId(item.getSettlementSourceId());
                milestoneRepository.save(live);
            });
        }

        if (ROLE_BUSINESS.equals(initiatingRole)) {
            settleDepositPenalty(businessDeposit, expertAccountId, penalty, contract.getContractId());
            refundDepositRemainder(businessDeposit, contract.getContractId());
            refundDepositRemainder(expertDeposit, contract.getContractId());
        } else {
            if (money(expertDeposit.getHeldAmount()).compareTo(penalty) != 0) {
                throw new AppException("EXPERT_CONTRACT_DEPOSIT_NOT_HELD");
            }
            settleDepositPenalty(expertDeposit, businessAccountId, penalty, contract.getContractId());
            refundDepositRemainder(businessDeposit, contract.getContractId());
        }
        LocalDateTime now = LocalDateTime.now();
        contract.setStatus(ContractEntity.STATUS_CLOSED);
        contract.setTerminatedAt(now);
        contract.setUpdatedAt(now);
        ContractEntity saved = contractRepository.save(contract);
        JobEntity job = jobRepository.findById(contract.getJobId())
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB CUA CONTRACT"));
        job.setStatus("CLOSED");
        jobRepository.save(job);
        auditLogService.record("IMMEDIATE_TERMINATION_PENALTY_SETTLED", "contracts",
                String.valueOf(contract.getContractId()), initiator.getAccountId());
        auditLogService.record("CONTRACT_CLOSED", "contracts",
                String.valueOf(contract.getContractId()), initiator.getAccountId());
        return saved;
    }

    private void settleDepositPenalty(
            ContractDepositEntity deposit, Integer beneficiaryAccountId, BigDecimal penalty, Integer contractId) {
        if (money(deposit.getHeldAmount()).compareTo(penalty) < 0) {
            throw new AppException("CONTRACT_DEPOSIT_ALREADY_RESOLVED");
        }
        WalletTransactionEntity debit = walletLedgerService.debitEscrow(
                deposit.getOwnerAccountId(), penalty, "IMMEDIATE_TERMINATION_PENALTY",
                "CONTRACT_DEPOSIT", deposit.getDepositId(), "Immediate termination penalty",
                WalletLedgerService.WalletOperationContext.builder()
                        .contractId(contractId)
                        .metadata("{\"settlementSourceType\":\"IMMEDIATE_TERMINATION\",\"initiatingRole\":\""
                                + deposit.getOwnerRole() + "\",\"penaltyPercentage\":10,\"penaltyAmount\":" + penalty + "}")
                        .operationKey("CONTRACT_DEPOSIT_PENALTY:" + deposit.getDepositId() + ":IMMEDIATE_TERMINATION")
                        .build());
        WalletTransactionEntity credit = walletLedgerService.creditAvailable(
                beneficiaryAccountId, penalty, "IMMEDIATE_TERMINATION_COMPENSATION",
                "CONTRACT_DEPOSIT", deposit.getDepositId(), "Immediate termination compensation",
                WalletLedgerService.WalletOperationContext.builder()
                        .contractId(contractId)
                        .metadata(debit.getMetadata())
                        .operationKey("CONTRACT_DEPOSIT_PENALTY:" + deposit.getDepositId() + ":IMMEDIATE_TERMINATION")
                        .operationLeg("BENEFICIARY_AVAILABLE_CREDIT")
                        .build());
        deposit.setHeldAmount(money(deposit.getHeldAmount()).subtract(penalty));
        deposit.setResolvedAmount(money(deposit.getResolvedAmount()).add(penalty));
        deposit.setPenaltyAmount(penalty);
        deposit.setPenaltyBeneficiaryAccountId(beneficiaryAccountId);
        deposit.setPenaltyTransactionId(credit.getId());
        deposit.setResolutionType("IMMEDIATE_TERMINATION_PENALTY");
        contractDepositRepository.save(deposit);
    }

    private void refundDepositRemainder(ContractDepositEntity deposit, Integer contractId) {
        BigDecimal remainder = money(deposit.getHeldAmount());
        if (remainder.signum() > 0) {
            WalletTransactionEntity refund = walletLedgerService.releaseEscrowToAvailable(
                    deposit.getOwnerAccountId(), remainder,
                    ROLE_EXPERT.equals(deposit.getOwnerRole())
                            ? "EXPERT_CONTRACT_DEPOSIT_REFUND" : "CONTRACT_SECURITY_DEPOSIT_REFUND",
                    "CONTRACT_DEPOSIT", deposit.getDepositId(), "Immediate termination deposit refund",
                    WalletLedgerService.WalletOperationContext.builder()
                            .contractId(contractId)
                            .operationKey("CONTRACT_DEPOSIT_REFUND:" + deposit.getDepositId() + ":IMMEDIATE_TERMINATION_REFUND")
                            .build());
            deposit.setRefundTransactionId(refund.getId());
            deposit.setRefundedAmount(money(deposit.getRefundedAmount()).add(remainder));
        }
        deposit.setHeldAmount(BigDecimal.ZERO);
        deposit.setStatus(deposit.getPenaltyAmount() != null && deposit.getPenaltyAmount().signum() > 0
                ? "PENALTY_SETTLED" : "REFUNDED");
        deposit.setResolvedAt(LocalDateTime.now());
        if (deposit.getResolutionType() == null) deposit.setResolutionType("IMMEDIATE_TERMINATION_REFUND");
        contractDepositRepository.save(deposit);
    }

    @Transactional
    // Note: Ham `createWithdrawalRequest` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public PaymentActionResponse<WithdrawalRequestEntity> createWithdrawalRequest(WithdrawalRequest request) {
        AccountEntity actor = requireApprovedBusinessOrExpert();
        validateWithdrawalRequest(request);
        BigDecimal amount = money(request.getAmount());
        BigDecimal available = walletLedgerService.availableBalance(actor.getAccountId());
        if (available.compareTo(amount) < 0) {
            return insufficient(available, amount, "INSUFFICIENT_BALANCE");
        }
        SystemWalletEntity wallet = systemWalletService.ensureWalletByAccountId(actor.getAccountId());
        WithdrawalRequestEntity withdrawal = withdrawalRequestRepository.save(WithdrawalRequestEntity.builder()
                .accountId(actor.getAccountId())
                .walletId(wallet.getSystemWalletId())
                .amount(amount)
                .bankName(request.getBankName().trim())
                .bankAccountNumber(request.getBankAccountNumber().trim())
                .bankAccountHolder(request.getBankAccountHolder().trim())
                .status("PENDING")
                .requestedAt(LocalDateTime.now())
                .build());
        WalletTransactionEntity holdTx = walletLedgerService.holdWithdrawalFromAvailable(
                actor.getAccountId(),
                amount,
                "WITHDRAW_HOLD",
                "WITHDRAW_REQUEST",
                withdrawal.getWithdrawalId(),
                "Withdrawal request",
                WalletLedgerService.WalletOperationContext.builder()
                        .operationKey("WITHDRAWAL:" + withdrawal.getWithdrawalId() + ":HOLD")
                        .build()
        );
        withdrawal.setHoldTransactionId(holdTx.getId());
        withdrawalRequestRepository.save(withdrawal);
        auditLogService.record(ACTION_CREATE_WITHDRAWAL, "withdrawal_requests",
                String.valueOf(withdrawal.getWithdrawalId()), actor.getAccountId());
        notifyAdminsWithdrawalReviewRequested(actor, withdrawal);
        return completed(withdrawal, "WITHDRAWAL_REQUEST_CREATED");
    }

    @Transactional(readOnly = true)
    // Note: Ham `listMyWithdrawalRequests` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public List<WithdrawalRequestEntity> listMyWithdrawalRequests() {
        AccountEntity actor = requireApprovedBusinessOrExpert();
        return withdrawalRequestRepository.findByAccountIdOrderByRequestedAtDesc(actor.getAccountId());
    }

    @Transactional(readOnly = true)
    // Note: Ham `listWithdrawalRequestsForAdmin` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public List<WithdrawalRequestEntity> listWithdrawalRequestsForAdmin() {
        accessService.requireRole("ADMIN");
        return withdrawalRequestRepository.findAllByOrderByRequestedAtDesc();
    }

    @Transactional
    // Note: Ham `approveWithdrawal` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public WithdrawalRequestEntity approveWithdrawal(Long withdrawalId, WithdrawalReviewRequest request) {
        AccountEntity admin = accessService.currentAccount();
        accessService.requireRole("ADMIN");
        WithdrawalRequestEntity withdrawal = pendingWithdrawal(withdrawalId);
        WalletTransactionEntity tx = walletLedgerService.debitHolding(
                withdrawal.getAccountId(),
                withdrawal.getAmount(),
                "WITHDRAW_APPROVED",
                "WITHDRAW_REQUEST",
                withdrawalId,
                "Withdrawal approved after manual transfer",
                WalletLedgerService.WalletOperationContext.builder()
                        .operationKey("WITHDRAWAL:" + withdrawalId + ":APPROVE")
                        .build()
        );
        withdrawal.setStatus("APPROVED");
        withdrawal.setAdminId(admin.getAccountId());
        withdrawal.setAdminNote(request == null ? null : request.getAdminNote());
        withdrawal.setReviewedAt(LocalDateTime.now());
        withdrawal.setReviewTransactionId(tx.getId());
        WithdrawalRequestEntity saved = withdrawalRequestRepository.save(withdrawal);
        auditLogService.record(ACTION_APPROVE_WITHDRAWAL, "withdrawal_requests",
                String.valueOf(withdrawalId), admin.getAccountId());
        notificationService.notifyWithdrawalApproved(
                saved.getAccountId(),
                admin.getAccountId(),
                saved.getWithdrawalId(),
                saved.getAmount()
        );
        return saved;
    }

    @Transactional
    // Note: Ham `rejectWithdrawal` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public WithdrawalRequestEntity rejectWithdrawal(Long withdrawalId, WithdrawalReviewRequest request) {
        AccountEntity admin = accessService.currentAccount();
        accessService.requireRole("ADMIN");
        WithdrawalRequestEntity withdrawal = pendingWithdrawal(withdrawalId);
        WalletTransactionEntity tx = walletLedgerService.releaseHoldingToAvailable(
                withdrawal.getAccountId(),
                withdrawal.getAmount(),
                "WITHDRAW_REJECTED",
                "WITHDRAW_REQUEST",
                withdrawalId,
                "Withdrawal rejected",
                WalletLedgerService.WalletOperationContext.builder()
                        .operationKey("WITHDRAWAL:" + withdrawalId + ":REJECT")
                        .build()
        );
        withdrawal.setStatus("REJECTED");
        withdrawal.setAdminId(admin.getAccountId());
        withdrawal.setAdminNote(request == null ? null : request.getAdminNote());
        withdrawal.setReviewedAt(LocalDateTime.now());
        withdrawal.setReviewTransactionId(tx.getId());
        WithdrawalRequestEntity saved = withdrawalRequestRepository.save(withdrawal);
        auditLogService.record(ACTION_REJECT_WITHDRAWAL, "withdrawal_requests",
                String.valueOf(withdrawalId), admin.getAccountId());
        notificationService.notifyWithdrawalRejected(
                saved.getAccountId(),
                admin.getAccountId(),
                saved.getWithdrawalId(),
                saved.getAmount(),
                saved.getAdminNote()
        );
        return saved;
    }

    @Transactional(readOnly = true)
    // Note: Ham `listCurrentWalletTransactions` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public List<WalletTransactionHistoryResponse> listCurrentWalletTransactions() {
        AccountEntity actor = accessService.currentAccount();
        List<WalletTransactionEntity> allTx = walletTransactionRepository
                .findByAccountIdOrderByCreatedAtDesc(actor.getAccountId());

        Set<String> withdrawalTypes = Set.of("WITHDRAW_HOLD", "WITHDRAW_APPROVED", "WITHDRAW_REJECTED");
        Map<Long, WithdrawalHistoryEntry> latestByWithdrawalId = new LinkedHashMap<>();
        List<WalletTransactionEntity> nonWithdrawalTx = new ArrayList<>();

        for (WalletTransactionEntity tx : allTx) {
            if (withdrawalTypes.contains(safe(tx.getTransactionType()))) {
                withdrawalForTransaction(tx).ifPresent(withdrawal ->
                        latestByWithdrawalId.putIfAbsent(
                                withdrawal.getWithdrawalId(),
                                new WithdrawalHistoryEntry(tx, withdrawal)
                        ));
            } else {
                nonWithdrawalTx.add(tx);
            }
        }

        List<WalletTransactionHistoryResponse> result = new ArrayList<>();
        for (WithdrawalHistoryEntry entry : latestByWithdrawalId.values()) {
            result.add(buildWithdrawalHistoryResponse(entry.transaction(), entry.withdrawal(), actor));
        }

        result.addAll(buildConsolidatedWalletHistory(nonWithdrawalTx, actor));

        result.sort(Comparator.comparing(WalletTransactionHistoryResponse::getCreatedAt).reversed());
        return result;
    }

    @Transactional(readOnly = true)
    public List<WalletTransactionHistoryResponse> listPlatformWalletTransactions() {
        accessService.requireRole("ADMIN");
        return walletTransactionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(this::isPlatformHistoryEventRow)
                .map(tx -> toWalletHistory(tx, null))
                .toList();
    }

    private WalletTransactionHistoryResponse toWalletHistory(WalletTransactionEntity tx, AccountEntity currentActor) {
        WalletTransactionHistoryResponse.WalletTransactionHistoryResponseBuilder builder = WalletTransactionHistoryResponse.builder()
                .transactionId(tx.getId())
                .accountId(tx.getAccountId())
                .transactionType(tx.getTransactionType())
                .direction(tx.getDirection())
                .balanceType(tx.getBalanceType())
                .amount(tx.getAmount())
                .balanceBefore(tx.getBalanceBefore())
                .balanceAfter(tx.getBalanceAfter())
                .status(tx.getStatus())
                .referenceType(tx.getReferenceType())
                .referenceId(tx.getReferenceId())
                .operationKey(tx.getOperationKey())
                .rawDescription(tx.getDescription())
                .createdAt(tx.getCreatedAt());
        AccountEntity walletOwner = currentActor != null && Objects.equals(currentActor.getAccountId(), tx.getAccountId())
                ? currentActor
                : accountRepository.findById(tx.getAccountId()).orElse(null);
        String actorName = displayAccount(walletOwner, tx.getAccountId());
        builder.actorName(actorName);

        return switch (safe(tx.getTransactionType())) {
            case "TOPUP" -> describeTopup(builder, tx, actorName);
            case "MEMBERSHIP_PURCHASE" -> describeMembership(builder, tx, actorName);
            case "CREDIT_PURCHASE" -> describeCreditPurchase(builder, tx, actorName);
            case "CONTRACT_SECURITY_DEPOSIT_HOLD", "CONTRACT_SECURITY_DEPOSIT_REFUND", "CONTRACT_SECURITY_DEPOSIT_RESOLVED" ->
                    describeContractDeposit(builder, tx, actorName);
            case "WITHDRAW_HOLD", "WITHDRAW_APPROVED", "WITHDRAW_REJECTED" ->
                    describeWithdrawal(builder, tx, actorName);
            default -> builder
                    .title(defaultTransactionTitle(tx))
                    .description(cleanLedgerDescription(tx))
                    .build();
        };
    }

    private WalletTransactionHistoryResponse describeTopup(
            WalletTransactionHistoryResponse.WalletTransactionHistoryResponseBuilder builder,
            WalletTransactionEntity tx,
            String actorName
    ) {
        Optional<PaymentOrderEntity> order = paymentOrder(tx);
        order.ifPresent(paymentOrder -> builder
                .paymentOrderId(paymentOrder.getId())
                .providerOrderCode(paymentOrder.getProviderOrderCode())
                .description(actorName + " đã nạp " + formatAmount(tx.getAmount()) + " VND vào ví. Mã thanh toán: "
                        + safeNumber(paymentOrder.getProviderOrderCode()) + "."));
        return builder
                .title(actorName + " đã nạp tiền vào ví")
                .description(order.isPresent()
                        ? actorName + " đã nạp " + formatAmount(tx.getAmount()) + " VND vào ví. Mã thanh toán: "
                                + safeNumber(order.get().getProviderOrderCode()) + "."
                        : actorName + " đã nạp " + formatAmount(tx.getAmount()) + " VND vào ví.")
                .build();
    }

    private WalletTransactionHistoryResponse describeMembership(
            WalletTransactionHistoryResponse.WalletTransactionHistoryResponseBuilder builder,
            WalletTransactionEntity tx,
            String actorName
    ) {
        Optional<MembershipPurchaseEntity> purchase = membershipPurchaseRepository.findByWalletTransactionId(tx.getId());
        Long packageId = purchase.map(MembershipPurchaseEntity::getPackageId).orElse(tx.getReferenceId());
        Optional<MembershipPackageEntity> membershipPackage = packageId == null ? Optional.empty() : membershipPackageRepository.findById(packageId);
        String packageName = membershipPackage.map(MembershipPackageEntity::getPackageName)
                .orElseGet(() -> nonBlank(tx.getDescription(), "gói thành viên"));
        builder.packageId(packageId).packageName(packageName);
        String description = purchase
                .map(item -> actorName + " thanh toán " + formatAmount(tx.getAmount()) + " VND để mua gói "
                        + packageName + ". Thời hạn từ " + item.getBadgeStartAt() + " đến " + item.getBadgeEndAt() + ".")
                .orElse(actorName + " thanh toán " + formatAmount(tx.getAmount()) + " VND để mua gói " + packageName + ".");
        return builder
                .title(actorName + " đã mua gói " + packageName)
                .description(description)
                .build();
    }

    private WalletTransactionHistoryResponse describeCreditPurchase(
            WalletTransactionHistoryResponse.WalletTransactionHistoryResponseBuilder builder,
            WalletTransactionEntity tx,
            String actorName
    ) {
        String description = cleanLedgerDescription(tx);
        String title = actorName + " đã mua lượt sử dụng";
        if (safe(tx.getDescription()).contains("job-post")) {
            title = actorName + " đã mua " + creditQuantity(tx) + " lượt đăng job";
        } else if (safe(tx.getDescription()).contains("proposal")) {
            title = actorName + " đã mua " + creditQuantity(tx) + " lượt nộp proposal";
        }
        return builder.title(title).description(description).build();
    }

    private WalletTransactionHistoryResponse describeContractDeposit(
            WalletTransactionHistoryResponse.WalletTransactionHistoryResponseBuilder builder,
            WalletTransactionEntity tx,
            String actorName
    ) {
        Optional<ContractDepositEntity> deposit = contractDepositForTransaction(tx);
        Optional<ContractEntity> contract = contractForDepositTransaction(tx, deposit);
        contract.ifPresent(item -> attachContractContext(builder, item));
        deposit.ifPresent(item -> builder.adminId(item.getAdminId())
                .adminName(item.getAdminId() == null ? null : displayAccount(accountRepository.findById(item.getAdminId()).orElse(null), item.getAdminId()))
                .adminNote(item.getAdminNote()));

        String contractTitle = contract.map(this::displayContract).orElse("hợp đồng");
        String businessName = contract.map(item -> displayBusiness(item.getBusinessId())).orElse(actorName);
        String expertName = contract.map(item -> displayExpert(item.getExpertId())).orElse(null);
        String counterparty = expertName == null ? null : expertName;
        builder.counterpartyName(counterparty);

        return switch (safe(tx.getTransactionType())) {
            case "CONTRACT_SECURITY_DEPOSIT_REFUND" -> builder
                    .title("Hoàn tiền ký quỹ cho " + businessName)
                    .description(adminDisplay(deposit) + " đã hoàn " + formatAmount(tx.getAmount())
                            + " VND từ ký quỹ của " + contractTitle + detailAdminNote(deposit))
                    .build();
            case "CONTRACT_SECURITY_DEPOSIT_RESOLVED" -> builder
                    .title("Giữ lại tiền ký quỹ của " + businessName)
                    .description(adminDisplay(deposit) + " đã xử lý giữ lại " + formatAmount(tx.getAmount())
                            + " VND từ ký quỹ của " + contractTitle + detailAdminNote(deposit))
                    .build();
            default -> builder
                    .title(businessName + " đã ký quỹ cho " + contractTitle)
                    .description(businessName + " đã ký quỹ " + formatAmount(tx.getAmount()) + " VND cho "
                            + contractTitle + (expertName == null ? "." : " với chuyên gia " + expertName + "."))
                    .build();
        };
    }

    private WalletTransactionHistoryResponse describeWithdrawal(
            WalletTransactionHistoryResponse.WalletTransactionHistoryResponseBuilder builder,
            WalletTransactionEntity tx,
            String actorName
    ) {
        Optional<WithdrawalRequestEntity> withdrawal = withdrawalForTransaction(tx);
        String requesterName = withdrawal
                .map(item -> displayAccount(accountRepository.findById(item.getAccountId()).orElse(null), item.getAccountId()))
                .orElse(actorName);
        withdrawal.ifPresent(item -> builder
                .withdrawalId(item.getWithdrawalId())
                .bankName(item.getBankName())
                .bankAccountHolder(item.getBankAccountHolder())
                .adminId(item.getAdminId())
                .adminName(item.getAdminId() == null ? null : displayAccount(accountRepository.findById(item.getAdminId()).orElse(null), item.getAdminId()))
                .adminNote(item.getAdminNote()));
        String bankText = withdrawal
                .map(item -> " Ngân hàng: " + item.getBankName() + ", chủ tài khoản: " + item.getBankAccountHolder() + ".")
                .orElse("");
        String adminNote = withdrawal
                .map(item -> item.getAdminNote() == null || item.getAdminNote().isBlank() ? "" : " Lý do: " + item.getAdminNote().trim() + ".")
                .orElse("");
        String adminName = withdrawal
                .map(item -> item.getAdminId() == null ? "Admin" : displayAccount(accountRepository.findById(item.getAdminId()).orElse(null), item.getAdminId()))
                .orElse("Admin");
        return switch (safe(tx.getTransactionType())) {
            case "WITHDRAW_APPROVED" -> builder
                    .title("Yêu cầu rút tiền của " + requesterName + " đã được duyệt")
                    .description(adminName + " đã duyệt rút " + formatAmount(tx.getAmount()) + " VND cho "
                            + requesterName + "." + bankText)
                    .build();
            case "WITHDRAW_REJECTED" -> builder
                    .title("Yêu cầu rút tiền của " + requesterName + " bị từ chối")
                    .description(adminName + " đã từ chối yêu cầu rút " + formatAmount(tx.getAmount())
                            + " VND của " + requesterName + "." + bankText + adminNote)
                    .build();
            default -> builder
                    .title(requesterName + " đã tạo yêu cầu rút tiền")
                    .description("Hệ thống đã tạm giữ " + formatAmount(tx.getAmount()) + " VND cho yêu cầu rút tiền của "
                            + requesterName + "." + bankText)
                    .build();
        };
    }

    private WalletTransactionHistoryResponse buildWithdrawalHistoryResponse(
            WalletTransactionEntity tx,
            WithdrawalRequestEntity withdrawal,
            AccountEntity currentActor
    ) {
        String actorName = displayAccount(currentActor, tx.getAccountId());
        String adminName = withdrawal.getAdminId() == null ? "Admin"
                : displayAccount(accountRepository.findById(withdrawal.getAdminId()).orElse(null), withdrawal.getAdminId());
        String bankText = " Ngân hàng: " + withdrawal.getBankName()
                + ", chủ tài khoản: " + withdrawal.getBankAccountHolder() + ".";

        WalletTransactionHistoryResponse.WalletTransactionHistoryResponseBuilder builder =
                WalletTransactionHistoryResponse.builder()
                        .transactionId(tx.getId())
                        .accountId(tx.getAccountId())
                        .amount(withdrawal.getAmount())
                        .status(withdrawal.getStatus())
                        .referenceType("WITHDRAW_REQUEST")
                        .referenceId(withdrawal.getWithdrawalId())
                        .actorName(actorName)
                        .withdrawalId(withdrawal.getWithdrawalId())
                        .bankName(withdrawal.getBankName())
                        .bankAccountHolder(withdrawal.getBankAccountHolder())
                        .adminId(withdrawal.getAdminId())
                        .adminName(withdrawal.getAdminId() == null ? null : adminName)
                        .adminNote(withdrawal.getAdminNote())
                        .createdAt("PENDING".equals(withdrawal.getStatus())
                                ? withdrawal.getRequestedAt()
                                : withdrawal.getReviewedAt());

        return switch (safe(withdrawal.getStatus())) {
            case "APPROVED" -> builder
                    .transactionType("WITHDRAW_APPROVED")
                    .direction("DEBIT")
                    .balanceType("HOLDING")
                    .title("Rút tiền thành công")
                    .description("Đã rút " + formatAmount(withdrawal.getAmount()) + " VND về" + bankText
                            + " Được duyệt bởi " + adminName + ".")
                    .build();
            case "REJECTED" -> {
                String adminNote = withdrawal.getAdminNote() == null || withdrawal.getAdminNote().isBlank()
                        ? "" : " Lý do: " + withdrawal.getAdminNote().trim() + ".";
                yield builder
                        .transactionType("WITHDRAW_REJECTED")
                        .direction("RELEASE")
                        .balanceType("HOLDING")
                        .title("Yêu cầu rút tiền bị từ chối")
                        .description("Yêu cầu rút " + formatAmount(withdrawal.getAmount()) + " VND bị từ chối bởi "
                                + adminName + "." + adminNote)
                        .build();
            }
            case "CANCELLED" -> builder
                    .transactionType("WITHDRAW_HOLD")
                    .direction("DEBIT")
                    .balanceType("AVAILABLE")
                    .title("Yêu cầu rút tiền đã hủy")
                    .description("Yêu cầu rút " + formatAmount(withdrawal.getAmount()) + " VND đã bị hủy.")
                    .build();
            default -> builder
                    .transactionType("WITHDRAW_HOLD")
                    .direction("DEBIT")
                    .balanceType("AVAILABLE")
                    .title("Yêu cầu rút tiền đang chờ duyệt")
                    .description("Yêu cầu rút " + formatAmount(withdrawal.getAmount()) + " VND về" + bankText
                            + " Đang chờ quản trị viên duyệt.")
                    .build();
        };
    }

    private Optional<PaymentOrderEntity> paymentOrder(WalletTransactionEntity tx) {
        if (tx.getPaymentOrderId() != null) {
            return paymentOrderRepository.findById(tx.getPaymentOrderId());
        }
        if ("PAYMENT_ORDER".equals(tx.getReferenceType()) && tx.getReferenceId() != null) {
            return paymentOrderRepository.findById(tx.getReferenceId());
        }
        return Optional.empty();
    }

    private Optional<ContractDepositEntity> contractDepositForTransaction(WalletTransactionEntity tx) {
        if ("CONTRACT_SECURITY_DEPOSIT_HOLD".equals(tx.getTransactionType())) {
            return contractDepositRepository.findByHoldTransactionId(tx.getId());
        }
        Optional<ContractDepositEntity> byTransaction = contractDepositRepository.findByRefundTransactionId(tx.getId());
        if (byTransaction.isPresent()) {
            return byTransaction;
        }
        if (tx.getReferenceId() != null && !"CONTRACT_SECURITY_DEPOSIT_HOLD".equals(tx.getTransactionType())) {
            return contractDepositRepository.findById(tx.getReferenceId());
        }
        return Optional.empty();
    }

    private Optional<ContractEntity> contractForDepositTransaction(WalletTransactionEntity tx, Optional<ContractDepositEntity> deposit) {
        if ("CONTRACT_SECURITY_DEPOSIT_HOLD".equals(tx.getTransactionType()) && tx.getReferenceId() != null) {
            return contractRepository.findById(toInt(tx.getReferenceId()));
        }
        return deposit.flatMap(item -> contractRepository.findById(item.getContractId()));
    }

    private Optional<WithdrawalRequestEntity> withdrawalForTransaction(WalletTransactionEntity tx) {
        if ("WITHDRAW_HOLD".equals(tx.getTransactionType())) {
            Optional<WithdrawalRequestEntity> byHold = withdrawalRequestRepository.findByHoldTransactionId(tx.getId());
            if (byHold.isPresent()) {
                return byHold;
            }
        }
        Optional<WithdrawalRequestEntity> byReview = withdrawalRequestRepository.findByReviewTransactionId(tx.getId());
        if (byReview.isPresent()) {
            return byReview;
        }
        if (tx.getReferenceId() != null && !"WITHDRAW_HOLD".equals(tx.getTransactionType())) {
            return withdrawalRequestRepository.findById(tx.getReferenceId());
        }
        return Optional.empty();
    }

    private List<WalletTransactionHistoryResponse> buildConsolidatedWalletHistory(
            List<WalletTransactionEntity> transactions,
            AccountEntity currentActor
    ) {
        List<WalletTransactionHistoryResponse> result = new ArrayList<>();
        Set<Long> consumed = new HashSet<>();
        for (WalletTransactionEntity tx : transactions) {
            if (tx.getId() != null && consumed.contains(tx.getId())) {
                continue;
            }
            List<WalletTransactionEntity> group = resolveWalletOperationGroup(tx);
            group.stream()
                    .map(WalletTransactionEntity::getId)
                    .filter(Objects::nonNull)
                    .forEach(consumed::add);
            WalletTransactionEntity representative = selectRepresentativeTransaction(group);
            WalletTransactionHistoryResponse item = toWalletHistory(representative, currentActor);
            item.setCreatedAt(group.stream()
                    .map(WalletTransactionEntity::getCreatedAt)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElse(item.getCreatedAt()));
            if (representative.getOperationKey() == null || representative.getOperationKey().isBlank()) {
                item.setOperationKey(null);
            }
            result.add(item);
        }
        return result;
    }

    private List<WalletTransactionEntity> resolveWalletOperationGroup(WalletTransactionEntity tx) {
        if (tx.getOperationKey() != null && !tx.getOperationKey().isBlank()) {
            List<WalletTransactionEntity> operationRows = walletTransactionRepository
                    .findByOperationKeyOrderByCreatedAtAscIdAsc(tx.getOperationKey())
                    .stream()
                    .filter(row -> Objects.equals(row.getAccountId(), tx.getAccountId()))
                    .toList();
            if (!operationRows.isEmpty()) {
                return operationRows;
            }
        }
        if (!supportsLegacyGrouping(tx) || tx.getCreatedAt() == null) {
            return List.of(tx);
        }
        List<WalletTransactionEntity> candidates = walletTransactionRepository.findLegacyOperationWindow(
                tx.getAccountId(),
                tx.getReferenceType(),
                tx.getReferenceId(),
                tx.getTransactionType(),
                tx.getCreatedAt().minusSeconds(5),
                tx.getCreatedAt().plusSeconds(5));
        if (isKnownLegacyOperationShape(candidates)) {
            return candidates;
        }
        return List.of(tx);
    }

    private WalletTransactionEntity selectRepresentativeTransaction(List<WalletTransactionEntity> group) {
        return group.stream()
                .filter(item -> "CREDIT".equals(item.getDirection()) && "AVAILABLE".equals(item.getBalanceType()))
                .findFirst()
                .or(() -> group.stream().filter(item -> "HOLD".equals(item.getDirection())).findFirst())
                .orElse(group.get(0));
    }

    private boolean supportsLegacyGrouping(WalletTransactionEntity tx) {
        return tx.getReferenceType() != null
                && tx.getReferenceId() != null
                && Set.of(
                WalletTransactionEntity.TX_ESCROW_DEPOSIT,
                WalletTransactionEntity.TX_ESCROW_REFUND,
                "CONTRACT_SECURITY_DEPOSIT_HOLD",
                "CONTRACT_SECURITY_DEPOSIT_REFUND",
                "EXPERT_CONTRACT_DEPOSIT_REFUND",
                "WITHDRAW_REJECTED")
                .contains(safe(tx.getTransactionType()));
    }

    private boolean isKnownLegacyOperationShape(List<WalletTransactionEntity> rows) {
        if (rows.size() != 2) {
            return false;
        }
        Set<String> directions = rows.stream()
                .map(row -> safe(row.getDirection()) + ":" + safe(row.getBalanceType()))
                .collect(Collectors.toSet());
        return directions.equals(Set.of("DEBIT:AVAILABLE", "HOLD:ESCROW"))
                || directions.equals(Set.of("RELEASE:ESCROW", "CREDIT:AVAILABLE"))
                || directions.equals(Set.of("DEBIT:AVAILABLE", "HOLD:HOLDING"))
                || directions.equals(Set.of("RELEASE:HOLDING", "CREDIT:AVAILABLE"));
    }

    private void attachContractContext(WalletTransactionHistoryResponse.WalletTransactionHistoryResponseBuilder builder, ContractEntity contract) {
        String businessName = displayBusiness(contract.getBusinessId());
        String expertName = displayExpert(contract.getExpertId());
        builder.contractId(contract.getContractId())
                .contractTitle(displayContract(contract))
                .businessId(contract.getBusinessId())
                .businessName(businessName)
                .expertId(contract.getExpertId())
                .expertName(expertName)
                .jobId(contract.getJobId())
                .jobTitle(jobRepository.findById(contract.getJobId()).map(JobEntity::getTitle).orElse(null));
    }

    private String displayBusiness(Integer businessId) {
        return businessId == null ? "doanh nghiệp" : businessProfileRepository.findById(businessId)
                .map(BusinessProfileEntity::getCompanyName)
                .filter(name -> !name.isBlank())
                .orElse("doanh nghiệp");
    }

    private String displayExpert(Integer expertId) {
        if (expertId == null) {
            return "chuyên gia";
        }
        return expertProfileRepository.findById(expertId)
                .flatMap(profile -> accountRepository.findById(profile.getAccountId()))
                .map(AccountEntity::getFullName)
                .filter(name -> !name.isBlank())
                .orElse("chuyên gia");
    }

    private String displayContract(ContractEntity contract) {
        if (contract.getContractTitle() != null && !contract.getContractTitle().isBlank()) {
            return "hợp đồng \"" + contract.getContractTitle().trim() + "\"";
        }
        return "hợp đồng giữa " + displayBusiness(contract.getBusinessId()) + " và " + displayExpert(contract.getExpertId());
    }

    private String displayAccount(AccountEntity account, Integer accountId) {
        if (account != null && account.getFullName() != null && !account.getFullName().isBlank()) {
            return account.getFullName().trim();
        }
        if (account != null && account.getEmail() != null && !account.getEmail().isBlank()) {
            return account.getEmail().trim();
        }
        return accountId == null ? "người dùng" : "người dùng";
    }

    private String defaultTransactionTitle(WalletTransactionEntity tx) {
        return switch (safe(tx.getDirection())) {
            case "CREDIT" -> "Ví được cộng tiền";
            case "DEBIT" -> "Ví bị trừ tiền";
            case "HOLD" -> "Ví tạm giữ tiền";
            case "RELEASE" -> "Ví được giải tỏa tiền";
            default -> "Giao dịch ví";
        };
    }

    private String cleanLedgerDescription(WalletTransactionEntity tx) {
        String description = safe(tx.getDescription());
        if (description.startsWith("Buy job-post credits:")) {
            String quantity = description.substring("Buy job-post credits:".length()).trim();
            return displayAccount(accountRepository.findById(tx.getAccountId()).orElse(null), tx.getAccountId())
                    + " thanh toán " + formatAmount(tx.getAmount()) + " VND để mua " + quantity + " lượt đăng job.";
        }
        if (description.startsWith("Buy proposal credits:")) {
            String quantity = description.substring("Buy proposal credits:".length()).trim();
            return displayAccount(accountRepository.findById(tx.getAccountId()).orElse(null), tx.getAccountId())
                    + " thanh toán " + formatAmount(tx.getAmount()) + " VND để mua " + quantity + " lượt nộp proposal.";
        }
        if ("Contract security deposit".equals(description)) {
            return "Ký quỹ bảo đảm thực hiện hợp đồng.";
        }
        if ("Refund contract security deposit".equals(description)) {
            return "Hoàn tiền ký quỹ hợp đồng.";
        }
        if ("Admin resolved contract security deposit".equals(description)) {
            return "Admin xử lý giữ lại tiền ký quỹ hợp đồng.";
        }
        if ("Withdrawal request".equals(description)) {
            return "Tạo yêu cầu rút tiền.";
        }
        if ("Withdrawal approved after manual transfer".equals(description)) {
            return "Yêu cầu rút tiền đã được duyệt sau khi chuyển khoản thủ công.";
        }
        if ("Withdrawal rejected".equals(description)) {
            return "Yêu cầu rút tiền bị từ chối.";
        }
        return description.isBlank() ? "Giao dịch ví." : description;
    }

    private String detailAdminNote(Optional<ContractDepositEntity> deposit) {
        return deposit.map(ContractDepositEntity::getAdminNote)
                .filter(note -> !note.isBlank())
                .map(note -> " Ghi chú admin: " + note.trim() + ".")
                .orElse(".");
    }

    private String adminDisplay(Optional<ContractDepositEntity> deposit) {
        return deposit
                .map(ContractDepositEntity::getAdminId)
                .filter(Objects::nonNull)
                .map(adminId -> displayAccount(accountRepository.findById(adminId).orElse(null), adminId))
                .orElse("Admin");
    }

    private boolean isPlatformHistoryEventRow(WalletTransactionEntity tx) {
        String type = safe(tx.getTransactionType());
        String direction = safe(tx.getDirection());
        String balanceType = safe(tx.getBalanceType());
        if ("CONTRACT_SECURITY_DEPOSIT_HOLD".equals(type) || "WITHDRAW_HOLD".equals(type)) {
            return "HOLD".equals(direction);
        }
        if ("CONTRACT_SECURITY_DEPOSIT_REFUND".equals(type) || "WITHDRAW_REJECTED".equals(type)) {
            return "CREDIT".equals(direction) && "AVAILABLE".equals(balanceType);
        }
        return true;
    }

    private String creditQuantity(WalletTransactionEntity tx) {
        String description = safe(tx.getDescription());
        if (description.startsWith("Buy job-post credits:")) {
            return description.substring("Buy job-post credits:".length()).trim();
        }
        if (description.startsWith("Buy proposal credits:")) {
            return description.substring("Buy proposal credits:".length()).trim();
        }
        return "nhiều";
    }

    private String formatAmount(BigDecimal amount) {
        return amount == null ? "0" : amount.stripTrailingZeros().toPlainString();
    }

    private String safeNumber(Long value) {
        return value == null ? "chưa có" : String.valueOf(value);
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private Long ensurePersistedDepositId(ContractDepositEntity deposit) {
        if (deposit.getDepositId() != null) {
            return deposit.getDepositId();
        }
        return contractDepositRepository.save(deposit).getDepositId();
    }

    // Note: Ham `requireApprovedBusinessOrExpert` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private AccountEntity requireApprovedBusinessOrExpert() {
        AccountEntity actor = accessService.currentAccount();
        String role = actor.getRole().getRoleName();
        if (!ROLE_BUSINESS.equals(role) && !ROLE_EXPERT.equals(role)) {
            throw new AppException("INVALID_ROLE");
        }
        accessService.requireApprovedAccount();
        return actor;
    }

    // Note: Ham `requireApprovedRole` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private AccountEntity requireApprovedRole(String role) {
        accessService.requireRole(role);
        accessService.requireApprovedAccount();
        return accessService.currentAccount();
    }

    // Note: Ham `ensureQuotaForAccountForUpdate` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private UserQuotaEntity ensureQuotaForAccountForUpdate(AccountEntity account) {
        return userQuotaRepository.findByAccountIdForUpdate(account.getAccountId())
                .orElseGet(() -> {
                    String role = account.getRole().getRoleName();
                    int initialJobPostQuota = ROLE_BUSINESS.equals(role) ? INITIAL_FREE_QUOTA : 0;
                    int initialProposalQuota = ROLE_EXPERT.equals(role) ? INITIAL_FREE_QUOTA : 0;
                    UserQuotaEntity created = userQuotaRepository.save(UserQuotaEntity.builder()
                            .accountId(account.getAccountId())
                            .jobPostQuotaBalance(initialJobPostQuota)
                            .proposalQuotaBalance(initialProposalQuota)
                            .build());
                    if (initialJobPostQuota > 0) {
                        quotaUsageLogRepository.save(QuotaUsageLogEntity.builder()
                                .accountId(account.getAccountId())
                                .quotaType(QUOTA_JOB_POST)
                                .actionType("GRANT")
                                .amount(initialJobPostQuota)
                                .balanceBefore(0)
                                .balanceAfter(initialJobPostQuota)
                                .referenceType("INITIAL_BUSINESS_GRANT")
                                .referenceId(account.getAccountId().longValue())
                                .build());
                    }
                    if (initialProposalQuota > 0) {
                        quotaUsageLogRepository.save(QuotaUsageLogEntity.builder()
                                .accountId(account.getAccountId())
                                .quotaType(QUOTA_PROPOSAL)
                                .actionType("GRANT")
                                .amount(initialProposalQuota)
                                .balanceBefore(0)
                                .balanceAfter(initialProposalQuota)
                                .referenceType("INITIAL_EXPERT_GRANT")
                                .referenceId(account.getAccountId().longValue())
                                .build());
                    }
                    return created;
                });
    }

    // Note: Ham `grantQuota` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private UserQuotaEntity grantQuota(Integer accountId, String quotaType, Integer amount, String referenceType, Long referenceId) {
        if (amount == null || amount <= 0) {
            throw new AppException("QUOTA AMOUNT KHONG HOP LE");
        }
        AccountEntity account = accountForQuota(accountId);
        UserQuotaEntity quota = ensureQuotaForAccountForUpdate(account);
        int before = quotaBalance(quota, quotaType);
        int after = before + amount;
        setQuotaBalance(quota, quotaType, after);
        UserQuotaEntity saved = userQuotaRepository.save(quota);
        quotaUsageLogRepository.save(QuotaUsageLogEntity.builder()
                .accountId(accountId)
                .quotaType(quotaType)
                .actionType("GRANT")
                .amount(amount)
                .balanceBefore(before)
                .balanceAfter(after)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build());
        return saved;
    }

    // Note: Ham `consumeQuota` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private int consumeQuota(AccountEntity account, String quotaType, String referenceType, Long referenceId) {
        UserQuotaEntity quota = ensureQuotaForAccountForUpdate(account);
        int before = quotaBalance(quota, quotaType);
        if (before <= 0) {
            throw new AppException("QUOTA_EXHAUSTED");
        }
        int after = before - 1;
        setQuotaBalance(quota, quotaType, after);
        userQuotaRepository.save(quota);
        quotaUsageLogRepository.save(QuotaUsageLogEntity.builder()
                .accountId(account.getAccountId())
                .quotaType(quotaType)
                .actionType("CONSUME")
                .amount(1)
                .balanceBefore(before)
                .balanceAfter(after)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build());
        auditLogService.record(ACTION_CONSUME_QUOTA, "quota_usage_logs",
                referenceId == null ? String.valueOf(account.getAccountId()) : String.valueOf(referenceId),
                account.getAccountId());
        return after;
    }

    private void notifyAdminsWithdrawalReviewRequested(AccountEntity actor, WithdrawalRequestEntity withdrawal) {
        accountRepository.findAllByRoleRoleNameOrderByAccountIdAsc("ADMIN")
                .forEach(admin -> notificationService.notifyWithdrawalReviewRequested(
                        admin.getAccountId(),
                        actor.getAccountId(),
                        withdrawal.getWithdrawalId(),
                        withdrawal.getAmount()
                ));
    }

    // Note: Ham `quotaBalance` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private int quotaBalance(UserQuotaEntity quota, String quotaType) {
        return QUOTA_JOB_POST.equals(quotaType)
                ? nonNegativeInt(quota.getJobPostQuotaBalance())
                : nonNegativeInt(quota.getProposalQuotaBalance());
    }

    // Note: Ham `setQuotaBalance` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void setQuotaBalance(UserQuotaEntity quota, String quotaType, int value) {
        if (QUOTA_JOB_POST.equals(quotaType)) {
            quota.setJobPostQuotaBalance(value);
        } else {
            quota.setProposalQuotaBalance(value);
        }
    }

    // Note: Ham `accountForQuota` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private AccountEntity accountForQuota(Integer accountId) {
        AccountEntity current = accessService.currentAccount();
        if (current.getAccountId().equals(accountId)) {
            return current;
        }
        return businessProfileRepository.findByAccountId(accountId)
                .map(profile -> accountStub(accountId, ROLE_BUSINESS))
                .orElseGet(() -> expertProfileRepository.findByAccountId(accountId)
                        .map(profile -> accountStub(accountId, ROLE_EXPERT))
                        .orElseThrow(() -> new NotFoundException("KHONG TIM THAY TAI KHOAN")));
    }

    // Note: Ham `accountStub` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private AccountEntity accountStub(Integer accountId, String role) {
        return AccountEntity.builder()
                .accountId(accountId)
                .role(RoleEntity.builder().roleName(role).build())
                .build();
    }

    // Note: Ham `activateContractAfterDeposit` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private boolean activateContractAfterBothDeposits(ContractEntity contract, LocalDateTime now, Integer actorAccountId) {
        if (ContractEntity.STATUS_ACTIVE.equals(contract.getStatus())) {
            return false;
        }
        boolean businessHeld = contractDepositRepository.findByContractIdAndOwnerRole(contract.getContractId(), ROLE_BUSINESS)
                .filter(deposit -> "HELD".equals(deposit.getStatus())).isPresent();
        boolean expertHeld = contractDepositRepository.findByContractIdAndOwnerRole(contract.getContractId(), ROLE_EXPERT)
                .filter(deposit -> "HELD".equals(deposit.getStatus())).isPresent();
        if (!businessHeld || !expertHeld) {
            contract.setStatus(ContractEntity.STATUS_PENDING);
            contractRepository.save(contract);
            return false;
        }
        contract.setStatus("ACTIVE");
        if (contract.getActivatedAt() == null) {
            contract.setActivatedAt(now);
        }
        contractRepository.save(contract);
        applyContractMilestoneBudgets(contract);
        JobEntity job = jobRepository.findById(contract.getJobId())
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY JOB CUA CONTRACT"));
        job.setBudget(contract.getTotalBudget());
        job.setStatus("IN_PROGRESS");
        jobRepository.save(job);
        auditLogService.record("CONTRACT_ACTIVATED_AFTER_DUAL_DEPOSIT", "contracts",
                String.valueOf(contract.getContractId()), actorAccountId);
        return true;
    }

    private void notifyDepositHeld(ContractEntity contract, Integer actorAccountId, String ownerRole) {
        String type = ROLE_EXPERT.equals(ownerRole) ? "EXPERT_CONTRACT_DEPOSIT_HELD" : "BUSINESS_CONTRACT_DEPOSIT_HELD";
        String title = ROLE_EXPERT.equals(ownerRole) ? "Chuyên gia đã ký quỹ" : "Doanh nghiệp đã ký quỹ";
        String message = ROLE_EXPERT.equals(ownerRole)
                ? "Chuyên gia đã hoàn tất ký quỹ 10% cho hợp đồng."
                : "Doanh nghiệp đã hoàn tất ký quỹ 20% cho hợp đồng.";
        notifyBothParticipants(contract, actorAccountId, type, title, message);
    }

    private void notifyBothParticipants(ContractEntity contract, Integer actorAccountId, String type, String title, String message) {
        notifyBusiness(contract, actorAccountId, type, title, message);
        expertProfileRepository.findById(contract.getExpertId())
                .ifPresent(expert -> notificationService.notifyContractEvent(
                        expert.getAccountId(), actorAccountId, type, title, message, contract.getContractId()));
    }

    private void notifyBusiness(ContractEntity contract, Integer actorAccountId, String type, String title, String message) {
        businessProfileRepository.findById(contract.getBusinessId())
                .ifPresent(business -> notificationService.notifyContractEvent(
                        business.getAccountId(), actorAccountId, type, title, message, contract.getContractId()));
    }

    // Note: Ham `applyContractMilestoneBudgets` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void applyContractMilestoneBudgets(ContractEntity contract) {
        for (ContractMilestoneEntity contractMilestone : contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(contract.getContractId())) {
            MilestoneEntity milestone = milestoneRepository.findById(contractMilestone.getJobMilestoneId())
                    .orElseThrow(() -> new NotFoundException("KHONG TIM THAY MILESTONE CUA CONTRACT"));
            milestone.setContractId(contract.getContractId());
            milestone.setFundsAllocated(contractMilestone.getFinalBudget());
            milestoneRepository.save(milestone);
        }
    }

    // Note: Ham `pendingWithdrawal` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private WithdrawalRequestEntity pendingWithdrawal(Long withdrawalId) {
        WithdrawalRequestEntity withdrawal = withdrawalRequestRepository.findByIdForUpdate(withdrawalId)
                .orElseThrow(() -> new NotFoundException("WITHDRAWAL_NOT_FOUND"));
        if (!"PENDING".equals(withdrawal.getStatus())) {
            throw new AppException("WITHDRAWAL_INVALID_STATUS");
        }
        return withdrawal;
    }

    // Note: Ham `validateWithdrawalRequest` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void validateWithdrawalRequest(WithdrawalRequest request) {
        if (request == null || request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new AppException("WITHDRAWAL_AMOUNT_INVALID");
        }
        if (isBlank(request.getBankName()) || isBlank(request.getBankAccountNumber()) || isBlank(request.getBankAccountHolder())) {
            throw new AppException("THONG TIN NGAN HANG KHONG HOP LE");
        }
    }

    // Note: Ham `requirePositiveQuantity` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private int requirePositiveQuantity(CreditPurchaseRequest request) {
        if (request == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new AppException("SO LUONG CREDIT KHONG HOP LE");
        }
        return request.getQuantity();
    }

    // Note: Ham `settingAmount` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private BigDecimal settingAmount(String key, BigDecimal fallback) {
        return systemSettingRepository.findById(key)
                .filter(setting -> Boolean.TRUE.equals(setting.getIsActive()))
                .map(SystemSettingEntity::getSettingValue)
                .map(value -> {
                    try {
                        return new BigDecimal(value.trim());
                    } catch (Exception ex) {
                        return fallback;
                    }
                })
                .orElse(fallback);
    }

    // Note: Ham `depositAmount` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private BigDecimal depositAmount(ContractEntity contract) {
        return percentageAmount(contract, new BigDecimal("20.00"));
    }

    private BigDecimal percentageAmount(ContractEntity contract, BigDecimal percentage) {
        return money(contract.getTotalBudget())
                .multiply(percentage)
                .divide(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean closeWhenBothDepositsResolved(ContractEntity contract, Integer actorAccountId) {
        List<ContractDepositEntity> deposits = contractDepositRepository
                .findByContractIdOrderByOwnerRoleAsc(contract.getContractId());
        boolean bothResolved = Set.of(ROLE_BUSINESS, ROLE_EXPERT).stream().allMatch(role ->
                deposits.stream().anyMatch(deposit -> role.equals(deposit.getOwnerRole())
                        && money(deposit.getHeldAmount()).signum() == 0));
        if (bothResolved && !ContractEntity.STATUS_CLOSED.equals(contract.getStatus())) {
            contract.setStatus(ContractEntity.STATUS_CLOSED);
            contractRepository.save(contract);
            auditLogService.record("CONTRACT_CLOSED", "contracts",
                    String.valueOf(contract.getContractId()), actorAccountId);
            return true;
        }
        return false;
    }

    // Note: Ham `refundStatus` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String refundStatus(BigDecimal refundAmount, BigDecimal resolvedAmount) {
        if (refundAmount.signum() > 0 && resolvedAmount.signum() == 0) {
            return "REFUNDED";
        }
        if (refundAmount.signum() > 0) {
            return "PARTIALLY_REFUNDED";
        }
        return "ADMIN_RESOLVED";
    }

    // Note: Ham `isPremiumActive` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private boolean isPremiumActive(UserQuotaEntity quota) {
        return quota.getPremiumExpiredAt() != null && quota.getPremiumExpiredAt().isAfter(LocalDateTime.now());
    }

    // Note: Ham `resolveActivePackage` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private ActivePackage resolveActivePackage(AccountEntity actor, UserQuotaEntity quota, LocalDateTime now) {
        List<MembershipPurchaseEntity> activePurchases = membershipPurchaseRepository.findByAccountIdOrderByCreatedAtDesc(actor.getAccountId()).stream()
                .filter(purchase -> "SUCCESS".equals(purchase.getStatus()))
                .filter(purchase -> purchase.getBadgeEndAt() != null && purchase.getBadgeEndAt().isAfter(now))
                .toList();
        if (activePurchases.isEmpty()) {
            return basicPackage();
        }
        Map<Long, MembershipPackageEntity> packagesById = membershipPackageRepository.findAllById(
                        activePurchases.stream()
                                .map(MembershipPurchaseEntity::getPackageId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet())
                ).stream()
                .collect(Collectors.toMap(MembershipPackageEntity::getPackageId, Function.identity()));
        return activePurchases.stream()
                .map(purchase -> packagesById.get(purchase.getPackageId()))
                .filter(Objects::nonNull)
                .filter(pkg -> !isPremiumPackage(pkg) || isPremiumActive(quota))
                .max(Comparator
                        .comparingInt(this::packagePriority)
                        .thenComparing(MembershipPackageEntity::getPackageName, Comparator.nullsLast(String::compareTo)))
                .map(pkg -> new ActivePackage(packageTier(pkg), pkg.getPackageName()))
                .orElseGet(this::basicPackage);
    }

    // Note: Ham `basicPackage` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private ActivePackage basicPackage() {
        return new ActivePackage(TIER_BASIC, "Basic");
    }

    // Note: Ham `isPremiumPackage` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private boolean isPremiumPackage(MembershipPackageEntity membershipPackage) {
        return TIER_PREMIUM.equals(packageTier(membershipPackage));
    }

    // Note: Ham `packagePriority` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private int packagePriority(MembershipPackageEntity membershipPackage) {
        return switch (packageTier(membershipPackage)) {
            case TIER_PREMIUM -> 4;
            case TIER_PLUS -> 3;
            case TIER_STANDARD -> 2;
            default -> 1;
        };
    }

    // Note: Ham `packageTier` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String packageTier(MembershipPackageEntity membershipPackage) {
        if (membershipPackage == null || membershipPackage.getPackageCode() == null) {
            return TIER_BASIC;
        }
        String code = membershipPackage.getPackageCode().trim().toUpperCase();
        if (code.endsWith("_" + TIER_PREMIUM) || TIER_PREMIUM.equals(code)) return TIER_PREMIUM;
        if (code.endsWith("_" + TIER_PLUS) || TIER_PLUS.equals(code)) return TIER_PLUS;
        if (code.endsWith("_" + TIER_STANDARD) || TIER_STANDARD.equals(code)) return TIER_STANDARD;
        return TIER_BASIC;
    }

    private record ActivePackage(String code, String name) {}

    private record WithdrawalHistoryEntry(
            WalletTransactionEntity transaction,
            WithdrawalRequestEntity withdrawal
    ) {}

    // Note: Ham `money` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void validateMembershipPackageRequest(MembershipPackageRequest request, boolean creating) {
        if (request == null) throw new AppException("BODY REQUEST KHONG HOP LE");
        if (creating && (request.getRoleType() == null || request.getRoleType().isBlank())) throw new AppException("ROLE TYPE KHONG DUOC DE TRONG");
        if (creating && (request.getPackageCode() == null || request.getPackageCode().isBlank())) throw new AppException("PACKAGE CODE KHONG DUOC DE TRONG");
        if (creating && (request.getPackageName() == null || request.getPackageName().isBlank())) throw new AppException("PACKAGE NAME KHONG DUOC DE TRONG");
        if (creating && request.getPrice() == null) throw new AppException("PRICE KHONG DUOC DE TRONG");
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) < 0) throw new AppException("PRICE KHONG HOP LE");
        if (creating && request.getBadgeDurationDays() == null) throw new AppException("BADGE DURATION DAYS KHONG DUOC DE TRONG");
        if (request.getBadgeDurationDays() != null && request.getBadgeDurationDays() <= 0) throw new AppException("BADGE DURATION DAYS KHONG HOP LE");
        if (request.getJobPostQuota() != null && request.getJobPostQuota() < 0) throw new AppException("JOB POST QUOTA KHONG HOP LE");
        if (request.getProposalQuota() != null && request.getProposalQuota() < 0) throw new AppException("PROPOSAL QUOTA KHONG HOP LE");
    }

    private String normalizePackageCode(String value) {
        if (value == null || value.isBlank()) throw new AppException("PACKAGE CODE KHONG DUOC DE TRONG");
        return value.trim().replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "").toUpperCase();
    }

    private String normalizePackageRole(String value) {
        if (value == null || value.isBlank()) throw new AppException("ROLE TYPE KHONG DUOC DE TRONG");
        String role = value.trim().toUpperCase();
        if (!ROLE_BUSINESS.equals(role) && !ROLE_EXPERT.equals(role)) {
            throw new AppException("ROLE TYPE KHONG HOP LE");
        }
        return role;
    }

    // Note: Ham `nonNegativeInt` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private int nonNegativeInt(Integer value) {
        return value == null || value < 0 ? 0 : value;
    }

    // Note: Ham `toInt` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Integer toInt(Long value) {
        try {
            return Math.toIntExact(value);
        } catch (ArithmeticException ex) {
            throw new NotFoundException("KHONG TIM THAY JOB");
        }
    }

    // Note: Ham `isBlank` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // Note: Ham `insufficient` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private <T> PaymentActionResponse<T> insufficient(BigDecimal current, BigDecimal required, String message) {
        return PaymentActionResponse.<T>builder()
                .completed(false)
                .needTopup(true)
                .currentBalance(current)
                .requiredAmount(required)
                .missingAmount(required.subtract(current).max(BigDecimal.ZERO))
                .redirectUrl(TOPUP_ENDPOINT)
                .message(message)
                .build();
    }

    // Note: Ham `completed` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private <T> PaymentActionResponse<T> completed(T data, String message) {
        return PaymentActionResponse.<T>builder()
                .completed(true)
                .needTopup(false)
                .message(message)
                .data(data)
                .build();
    }
}
