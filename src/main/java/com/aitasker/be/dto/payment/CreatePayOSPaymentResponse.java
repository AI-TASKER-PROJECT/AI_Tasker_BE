/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/payment/CreatePayOSPaymentResponse.java
 * Day la file gi: File DTO khai bao du lieu request/response di qua API hoac qua service.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.dto.payment;

import com.aitasker.be.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

// Note: Annotation nay giup Lombok sinh getter, setter va cac ham tien ich cho du lieu.
@Data
// Note: Annotation nay giup Lombok tao builder de khoi tao object ro rang hon.
@Builder
public class CreatePayOSPaymentResponse {
    private String checkoutUrl;
    private String qrCode;
    private String bin;
    private String accountNumber;
    private String accountName;
    private Long expiredAt;
    private Long orderCode;
    private BigDecimal amount;
    private PaymentStatus status;
}
