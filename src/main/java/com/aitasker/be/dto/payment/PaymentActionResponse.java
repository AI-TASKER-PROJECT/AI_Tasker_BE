/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/payment/PaymentActionResponse.java
 * Day la file gi: File DTO khai bao du lieu request/response di qua API hoac qua service.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

// Note: Annotation nay giup Lombok sinh getter, setter va cac ham tien ich cho du lieu.
@Data
// Note: Annotation nay giup Lombok tao builder de khoi tao object ro rang hon.
@Builder
public class PaymentActionResponse<T> {
    private boolean completed;
    private boolean needTopup;
    private BigDecimal currentBalance;
    private BigDecimal requiredAmount;
    private BigDecimal missingAmount;
    private String redirectUrl;
    private String message;
    private T data;
}
