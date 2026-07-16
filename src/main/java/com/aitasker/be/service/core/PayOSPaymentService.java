/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/core/PayOSPaymentService.java
 * Day la file gi: File service chua nghiep vu chinh, dieu phoi repository va kiem tra luat xu ly cua he thong.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.config.PayOSProperties;
import com.aitasker.be.dto.payment.CreatePayOSPaymentResponse;
import com.aitasker.be.dto.payment.CreateWalletTopupPaymentRequest;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.PaymentOrderEntity;
import com.aitasker.be.entity.PaymentProvider;
import com.aitasker.be.entity.PaymentStatus;
import com.aitasker.be.repository.BusinessProfileRepository;
import com.aitasker.be.repository.PaymentOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;
import vn.payos.model.v2.paymentRequests.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Note: Annotation nay cho Spring quan ly class nhu mot service chua nghiep vu.
@Service
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class PayOSPaymentService {
    private static final String PURPOSE_WALLET_TOPUP = "WALLET_TOPUP";

    private final PayOS payOS;
    private final PayOSProperties payOSProperties;
    private final PaymentOrderRepository paymentOrderRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final AccessService accessService;
    private final WalletLedgerService walletLedgerService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Transactional
    // Note: Ham `createPayment` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public CreatePayOSPaymentResponse createPayment(CreateWalletTopupPaymentRequest request) {
        validateCreateRequest(request);
        validateConfig();

        AccountEntity actor = accessService.currentAccount();
        Long orderCode = generateUniqueOrderCode();
        Long amount = toPayOSAmount(request.getAmount());
        Long businessId = resolveCurrentBusinessId(actor);
        String description = buildDescription(request.getDescription(), orderCode);

        PaymentOrderEntity paymentOrder = paymentOrderRepository.save(PaymentOrderEntity.builder()
                .accountId(actor.getAccountId().longValue())
                .businessId(businessId)
                .amount(BigDecimal.valueOf(amount))
                .provider(PaymentProvider.PAYOS)
                .purpose(PURPOSE_WALLET_TOPUP)
                .providerTxnRef("PAYOS" + orderCode)
                .providerOrderCode(orderCode)
                .status(PaymentStatus.PENDING)
                .description(description)
                .returnUrl(payOSProperties.getReturnUrl())
                .cancelUrl(payOSProperties.getCancelUrl())
                .build());

        try {
            CreatePaymentLinkRequest payOSRequest = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(amount)
                    .description(description)
                    .returnUrl(payOSProperties.getReturnUrl())
                    .cancelUrl(payOSProperties.getCancelUrl())
                    .build();

            CreatePaymentLinkResponse payOSResponse = payOS.paymentRequests().create(payOSRequest);
            paymentOrder.setProviderPaymentLinkId(payOSResponse.getPaymentLinkId());
            paymentOrder.setCheckoutUrl(payOSResponse.getCheckoutUrl());
            paymentOrderRepository.save(paymentOrder);

            return CreatePayOSPaymentResponse.builder()
                    .checkoutUrl(payOSResponse.getCheckoutUrl())
                    .qrCode(payOSResponse.getQrCode())
                    .bin(payOSResponse.getBin())
                    .accountNumber(payOSResponse.getAccountNumber())
                    .accountName(payOSResponse.getAccountName())
                    .expiredAt(payOSResponse.getExpiredAt())
                    .orderCode(orderCode)
                    .amount(paymentOrder.getAmount())
                    .status(paymentOrder.getStatus())
                    .build();
        } catch (Exception ex) {
            throw new AppException("KHONG TAO DUOC LINK THANH TOAN PAYOS: " + ex.getMessage());
        }
    }

    @Transactional
    // Note: Ham `syncPaymentStatus` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public PaymentOrderEntity syncPaymentStatus(Long orderCode) {
        validateConfig();

        PaymentOrderEntity paymentOrder = paymentOrderRepository.findByProviderOrderCode(orderCode)
                .orElseThrow(() -> new AppException("KHONG TIM THAY PAYMENT ORDER"));

        try {
            PaymentLink paymentLink = payOS.paymentRequests().get(orderCode);
            return updatePaymentOrderFromProvider(paymentOrder, paymentLink);
        } catch (Exception ex) {
            throw new AppException("KHONG DONG BO DUOC TRANG THAI PAYOS: " + ex.getMessage());
        }
    }

    // Note: Ham `updatePaymentOrderFromProvider` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private PaymentOrderEntity updatePaymentOrderFromProvider(PaymentOrderEntity paymentOrder, PaymentLink paymentLink) {
        PaymentStatus previousStatus = paymentOrder.getStatus();
        paymentOrder.setProviderPaymentLinkId(paymentLink.getId());
        paymentOrder.setProviderResponseCode(resolveProviderResponseCode(paymentLink.getStatus()));
        paymentOrder.setProviderTransactionNo(resolveProviderTransactionNo(paymentLink.getTransactions()));

        PaymentStatus mappedStatus = mapPaymentLinkStatus(paymentLink.getStatus());
        paymentOrder.setStatus(mappedStatus);

        if (mappedStatus == PaymentStatus.PAID) {
            if (paymentOrder.getPaidAt() == null) {
                paymentOrder.setPaidAt(LocalDateTime.now());
            }
            if (previousStatus != PaymentStatus.PAID) {
                validateProviderAmount(paymentOrder, paymentLink);
                walletLedgerService.postWalletTopup(paymentOrder);
                notificationService.notifyWalletTopupSucceeded(
                        Math.toIntExact(paymentOrder.getAccountId()),
                        paymentOrder.getProviderOrderCode(),
                        paymentOrder.getAmount()
                );
            }
        }

        if (previousStatus != mappedStatus && isTerminalStatus(mappedStatus)) {
            auditLogService.record(
                    mappedStatus == PaymentStatus.PAID
                            ? AuditLogService.ACTION_WALLET_TOPUP_SUCCEEDED
                            : AuditLogService.ACTION_WALLET_TOPUP_FAILED,
                    "payment_order",
                    String.valueOf(paymentOrder.getId()),
                    Math.toIntExact(paymentOrder.getAccountId())
            );
        }

        return paymentOrderRepository.save(paymentOrder);
    }

    private boolean isTerminalStatus(PaymentStatus status) {
        return status == PaymentStatus.PAID
                || status == PaymentStatus.FAILED
                || status == PaymentStatus.CANCELLED
                || status == PaymentStatus.EXPIRED;
    }

    // Note: Ham `validateCreateRequest` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void validateCreateRequest(CreateWalletTopupPaymentRequest request) {
        if (request == null) {
            throw new AppException("THONG TIN THANH TOAN KHONG HOP LE");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.valueOf(2000)) < 0) {
            throw new AppException("SO TIEN THANH TOAN PHAI IT NHAT LA 2000");
        }
    }

    // Note: Ham `validateProviderAmount` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void validateProviderAmount(PaymentOrderEntity paymentOrder, PaymentLink paymentLink) {
        if (paymentLink.getAmount() == null
                || paymentOrder.getAmount().compareTo(BigDecimal.valueOf(paymentLink.getAmount())) != 0) {
            throw new AppException("SO TIEN PAYOS KHONG KHOP PAYMENT ORDER");
        }
    }

    // Note: Ham `validateConfig` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private void validateConfig() {
        if (isBlank(payOSProperties.getClientId())
                || isBlank(payOSProperties.getApiKey())
                || isBlank(payOSProperties.getChecksumKey())
                || isBlank(payOSProperties.getReturnUrl())
                || isBlank(payOSProperties.getCancelUrl())) {
            throw new AppException("CHUA CAU HINH PAYOS");
        }
    }

    // Note: Ham `resolveCurrentBusinessId` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Long resolveCurrentBusinessId(AccountEntity actor) {
        if (!"BUSINESS".equals(actor.getRole().getRoleName())) {
            return null;
        }
        return businessProfileRepository.findByAccountId(actor.getAccountId())
                .map(profile -> profile.getBusinessId().longValue())
                .orElse(null);
    }

    // Note: Ham `toPayOSAmount` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Long toPayOSAmount(BigDecimal amount) {
        try {
            return amount.setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException ex) {
            throw new AppException("SO TIEN PAYOS PHAI LA SO NGUYEN VND");
        }
    }

    // Note: Ham `buildDescription` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String buildDescription(String description, Long orderCode) {
        String value = description == null || description.isBlank()
                ? "Nap vi " + orderCode
                : description;
        String normalized = normalizePayOSDescription(value);
        if (normalized.isBlank()) {
            normalized = "Nap vi " + orderCode;
        }
        return normalized.length() > 25 ? normalized.substring(0, 25).trim() : normalized;
    }

    // Note: Ham `normalizePayOSDescription` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String normalizePayOSDescription(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("\u0111", "d")
                .replace("\u0110", "D")
                .replaceAll("[^A-Za-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // Note: Ham `generateUniqueOrderCode` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private Long generateUniqueOrderCode() {
        Long orderCode;
        do {
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
            orderCode = Long.parseLong(System.currentTimeMillis() + String.valueOf(Math.abs(suffix.hashCode() % 1000)));
        } while (paymentOrderRepository.existsByProviderOrderCode(orderCode));
        return orderCode;
    }

    // Note: Ham `isBlank` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // Note: Ham `mapPaymentLinkStatus` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private PaymentStatus mapPaymentLinkStatus(PaymentLinkStatus status) {
        if (status == null) {
            return PaymentStatus.PENDING;
        }
        return switch (status) {
            case PAID -> PaymentStatus.PAID;
            case CANCELLED -> PaymentStatus.CANCELLED;
            case EXPIRED -> PaymentStatus.EXPIRED;
            case FAILED -> PaymentStatus.FAILED;
            case PENDING, PROCESSING, UNDERPAID -> PaymentStatus.PENDING;
        };
    }

    // Note: Ham `resolveProviderResponseCode` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String resolveProviderResponseCode(PaymentLinkStatus status) {
        if (status == null) {
            return null;
        }
        return status == PaymentLinkStatus.PAID ? "00" : status.name();
    }

    // Note: Ham `resolveProviderTransactionNo` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String resolveProviderTransactionNo(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return null;
        }
        return transactions.stream()
                .map(Transaction::getReference)
                .filter(reference -> reference != null && !reference.isBlank())
                .findFirst()
                .orElse(null);
    }
}
