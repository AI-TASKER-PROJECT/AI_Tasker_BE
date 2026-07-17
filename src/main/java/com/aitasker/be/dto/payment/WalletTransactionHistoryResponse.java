/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/payment/WalletTransactionHistoryResponse.java
 * Đây là file gì: DTO trả lịch sử giao dịch ví ở dạng dễ hiểu cho giao diện.
 * Mục đích note: giữ mã ledger thô cho lọc/debug nhưng bổ sung tiêu đề, mô tả và ngữ cảnh nghiệp vụ.
 */
package com.aitasker.be.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionHistoryResponse {
    private Long transactionId;
    private Integer accountId;
    private String transactionType;
    private String direction;
    private String balanceType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String status;
    private String referenceType;
    private Long referenceId;
    private String operationKey;
    private String operationLeg;
    private String rawDescription;
    private LocalDateTime createdAt;

    private String title;
    private String description;
    private String actorName;
    private String counterpartyName;
    private Integer businessId;
    private String businessName;
    private Integer expertId;
    private String expertName;
    private Integer jobId;
    private String jobTitle;
    private Integer contractId;
    private String contractTitle;
    private Long withdrawalId;
    private String bankName;
    private String bankAccountHolder;
    private Integer adminId;
    private String adminName;
    private String adminNote;
    private Long paymentOrderId;
    private Long providerOrderCode;
    private Long packageId;
    private String packageName;
}
