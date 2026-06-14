package com.aitasker.be.service.core;

import com.aitasker.be.common.VNPayUtil;
import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.config.VNPayProperties;
import com.aitasker.be.dto.payment.CreateVNPayPaymentRequest;
import com.aitasker.be.dto.payment.CreateVNPayPaymentResponse;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.PaymentOrderEntity;
import com.aitasker.be.entity.PaymentProvider;
import com.aitasker.be.entity.PaymentStatus;
import com.aitasker.be.entity.SystemWalletEntity;
import com.aitasker.be.entity.WalletTransactionEntity;
import com.aitasker.be.repository.BusinessProfileRepository;
import com.aitasker.be.repository.PaymentOrderRepository;
import com.aitasker.be.repository.SystemWalletRepository;
import com.aitasker.be.repository.WalletTransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VNPayPaymentService {
    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String PURPOSE_WALLET_TOPUP = "WALLET_TOPUP";
    private static final String WALLET_TRANSACTION_TOPUP = "TOPUP";

    private final VNPayProperties vnPayProperties;
    private final PaymentOrderRepository paymentOrderRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final SystemWalletRepository systemWalletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final SystemWalletService systemWalletService;
    private final AccessService accessService;

    @Transactional
    public CreateVNPayPaymentResponse createPayment(CreateVNPayPaymentRequest request, HttpServletRequest httpRequest) {
        validateCreateRequest(request);
        validateConfig();

        String vnpTxnRef = generateUniqueTxnRef();
        AccountEntity actor = accessService.currentAccount();
        Long accountId = actor.getAccountId().longValue();
        Long businessId = resolveCurrentBusinessId(actor);
        PaymentOrderEntity paymentOrder = PaymentOrderEntity.builder()
                .accountId(accountId)
                .businessId(businessId)
                .jobId(request.getJobId())
                .milestoneId(request.getMilestoneId())
                .amount(request.getAmount())
                .provider(PaymentProvider.VNPAY)
                .purpose(PURPOSE_WALLET_TOPUP)
                .vnpTxnRef(vnpTxnRef)
                .status(PaymentStatus.PENDING)
                .description(request.getDescription())
                .build();
        paymentOrderRepository.save(paymentOrder);

        Map<String, String> params = buildPaymentParams(request, httpRequest, vnpTxnRef);
        String secureHash = VNPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), VNPayUtil.buildHashData(params));
        params.put("vnp_SecureHash", secureHash);

        String paymentUrl = vnPayProperties.getPayUrl() + "?" + VNPayUtil.buildQueryString(params);
        return CreateVNPayPaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .vnpTxnRef(vnpTxnRef)
                .amount(paymentOrder.getAmount())
                .status(paymentOrder.getStatus())
                .build();
    }

    @Transactional
    public PaymentOrderEntity handleReturn(Map<String, String> params) {
        validateConfig();

        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) {
            throw new AppException("VNPAY SECURE HASH KHONG HOP LE");
        }

        String calculatedHash = VNPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), VNPayUtil.buildHashData(params));
        if (!calculatedHash.equalsIgnoreCase(receivedHash)) {
            throw new AppException("CHU KY VNPAY KHONG HOP LE");
        }

        String vnpTxnRef = params.get("vnp_TxnRef");
        PaymentOrderEntity paymentOrder = paymentOrderRepository.findByVnpTxnRef(vnpTxnRef)
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY PAYMENT ORDER"));
        PaymentStatus previousStatus = paymentOrder.getStatus();

        String responseCode = params.get("vnp_ResponseCode");
        paymentOrder.setVnpResponseCode(responseCode);
        paymentOrder.setVnpSecureHash(receivedHash);
        paymentOrder.setVnpTransactionNo(params.get("vnp_TransactionNo"));

        if ("00".equals(responseCode)) {
            paymentOrder.setStatus(PaymentStatus.PAID);
            paymentOrder.setPaidAt(LocalDateTime.now());
            if (previousStatus != PaymentStatus.PAID) {
                postWalletTopup(paymentOrder);
            }
        } else if ("24".equals(responseCode)) {
            paymentOrder.setStatus(PaymentStatus.CANCELLED);
        } else {
            paymentOrder.setStatus(PaymentStatus.FAILED);
        }

        return paymentOrderRepository.save(paymentOrder);
    }

    private void validateCreateRequest(CreateVNPayPaymentRequest request) {
        if (request == null) {
            throw new AppException("THONG TIN THANH TOAN KHONG HOP LE");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("SO TIEN THANH TOAN PHAI LON HON 0");
        }
    }

    private Long resolveCurrentBusinessId(AccountEntity actor) {
        if (!"BUSINESS".equals(actor.getRole().getRoleName())) {
            return null;
        }
        return businessProfileRepository.findByAccountId(actor.getAccountId())
                .map(profile -> profile.getBusinessId().longValue())
                .orElseThrow(() -> new NotFoundException("KHONG TIM THAY BUSINESS PROFILE"));
    }

    private void postWalletTopup(PaymentOrderEntity paymentOrder) {
        if (walletTransactionRepository.existsByPaymentOrderIdAndTransactionType(paymentOrder.getId(), WALLET_TRANSACTION_TOPUP)) {
            return;
        }
        if (paymentOrder.getAccountId() == null) {
            throw new AppException("PAYMENT ORDER CHUA CO ACCOUNT DE NAP VI");
        }

        Integer accountId = Math.toIntExact(paymentOrder.getAccountId());
        systemWalletService.syncWallet();
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

    private void validateConfig() {
        if (isBlank(vnPayProperties.getTmnCode())
                || isBlank(vnPayProperties.getHashSecret())
                || isBlank(vnPayProperties.getPayUrl())
                || isBlank(vnPayProperties.getReturnUrl())) {
            throw new AppException("CHUA CAU HINH VNPAY SANDBOX");
        }
    }

    private Map<String, String> buildPaymentParams(
            CreateVNPayPaymentRequest request,
            HttpServletRequest httpRequest,
            String vnpTxnRef
    ) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        params.put("vnp_Amount", toVnPayAmount(request.getAmount()));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", vnpTxnRef);
        params.put("vnp_OrderInfo", buildOrderInfo(request, vnpTxnRef));
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        params.put("vnp_IpAddr", VNPayUtil.getIpAddress(httpRequest));
        LocalDateTime createDate = LocalDateTime.now();
        params.put("vnp_CreateDate", createDate.format(VNPAY_DATE_FORMAT));
        params.put("vnp_ExpireDate", createDate.plusMinutes(15).format(VNPAY_DATE_FORMAT));
        return params;
    }

    private String buildOrderInfo(CreateVNPayPaymentRequest request, String vnpTxnRef) {
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            return normalizeOrderInfo(request.getDescription());
        }
        return "Thanh toan AI Tasker " + vnpTxnRef;
    }

    private String normalizeOrderInfo(String value) {
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        String cleaned = withoutAccents.replaceAll("[^A-Za-z0-9 .,:\\-]", " ").replaceAll("\\s+", " ").trim();
        return cleaned.isBlank() ? "Thanh toan AI Tasker" : cleaned;
    }

    private String toVnPayAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .toPlainString();
    }

    private String generateUniqueTxnRef() {
        String txnRef;
        do {
            txnRef = "PAY" + LocalDateTime.now().format(VNPAY_DATE_FORMAT)
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (paymentOrderRepository.existsByVnpTxnRef(txnRef));
        return txnRef;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
