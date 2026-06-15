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
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayOSPaymentService {
    private static final String PURPOSE_WALLET_TOPUP = "WALLET_TOPUP";

    private final PayOS payOS;
    private final PayOSProperties payOSProperties;
    private final PaymentOrderRepository paymentOrderRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final AccessService accessService;
    private final WalletLedgerService walletLedgerService;

    @Transactional
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
    public PaymentOrderEntity handleWebhook(Webhook webhook) {
        validateConfig();

        WebhookData data;
        try {
            data = payOS.webhooks().verify(webhook);
        } catch (Exception ex) {
            throw new AppException("WEBHOOK PAYOS KHONG HOP LE");
        }

        if (data == null || data.getOrderCode() == null) {
            throw new AppException("WEBHOOK PAYOS THIEU ORDER CODE");
        }

        return paymentOrderRepository.findByProviderOrderCode(data.getOrderCode())
                .map(paymentOrder -> updatePaymentOrderFromWebhook(paymentOrder, data))
                .orElse(null);
    }

    @Transactional
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

    private PaymentOrderEntity updatePaymentOrderFromWebhook(PaymentOrderEntity paymentOrder, WebhookData data) {
        PaymentStatus previousStatus = paymentOrder.getStatus();
        paymentOrder.setProviderPaymentLinkId(data.getPaymentLinkId());
        paymentOrder.setProviderResponseCode(data.getCode());
        paymentOrder.setProviderTransactionNo(data.getReference());

        if ("00".equals(data.getCode())) {
            paymentOrder.setStatus(PaymentStatus.PAID);
            if (paymentOrder.getPaidAt() == null) {
                paymentOrder.setPaidAt(LocalDateTime.now());
            }
            if (previousStatus != PaymentStatus.PAID) {
                validateWebhookAmount(paymentOrder, data);
                walletLedgerService.postWalletTopup(paymentOrder);
            }
        } else {
            paymentOrder.setStatus(PaymentStatus.FAILED);
        }

        return paymentOrderRepository.save(paymentOrder);
    }

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
            }
        }

        return paymentOrderRepository.save(paymentOrder);
    }

    private void validateCreateRequest(CreateWalletTopupPaymentRequest request) {
        if (request == null) {
            throw new AppException("THONG TIN THANH TOAN KHONG HOP LE");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 2000) {
            throw new AppException("SO TIEN THANH TOAN PHAI LON HON 0");
        }
    }

    private void validateWebhookAmount(PaymentOrderEntity paymentOrder, WebhookData data) {
        if (data.getAmount() == null || paymentOrder.getAmount().compareTo(BigDecimal.valueOf(data.getAmount())) != 0) {
            throw new AppException("SO TIEN WEBHOOK PAYOS KHONG KHOP PAYMENT ORDER");
        }
    }

    private void validateProviderAmount(PaymentOrderEntity paymentOrder, PaymentLink paymentLink) {
        if (paymentLink.getAmount() == null
                || paymentOrder.getAmount().compareTo(BigDecimal.valueOf(paymentLink.getAmount())) != 0) {
            throw new AppException("SO TIEN PAYOS KHONG KHOP PAYMENT ORDER");
        }
    }

    private void validateConfig() {
        if (isBlank(payOSProperties.getClientId())
                || isBlank(payOSProperties.getApiKey())
                || isBlank(payOSProperties.getChecksumKey())
                || isBlank(payOSProperties.getReturnUrl())
                || isBlank(payOSProperties.getCancelUrl())) {
            throw new AppException("CHUA CAU HINH PAYOS");
        }
    }

    private Long resolveCurrentBusinessId(AccountEntity actor) {
        if (!"BUSINESS".equals(actor.getRole().getRoleName())) {
            return null;
        }
        return businessProfileRepository.findByAccountId(actor.getAccountId())
                .map(profile -> profile.getBusinessId().longValue())
                .orElse(null);
    }

    private Long toPayOSAmount(BigDecimal amount) {
        try {
            return amount.setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException ex) {
            throw new AppException("SO TIEN PAYOS PHAI LA SO NGUYEN VND");
        }
    }

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

    private String normalizePayOSDescription(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("\u0111", "d")
                .replace("\u0110", "D")
                .replaceAll("[^A-Za-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Long generateUniqueOrderCode() {
        Long orderCode;
        do {
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
            orderCode = Long.parseLong(System.currentTimeMillis() + String.valueOf(Math.abs(suffix.hashCode() % 1000)));
        } while (paymentOrderRepository.existsByProviderOrderCode(orderCode));
        return orderCode;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

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

    private String resolveProviderResponseCode(PaymentLinkStatus status) {
        if (status == null) {
            return null;
        }
        return status == PaymentLinkStatus.PAID ? "00" : status.name();
    }

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
