/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/payment/WithdrawalRequest.java
 * Day la file gi: File DTO khai bao du lieu request/response di qua API hoac qua service.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

// Note: Annotation nay giup Lombok sinh getter, setter va cac ham tien ich cho du lieu.
@Data
public class WithdrawalRequest {
    private BigDecimal amount;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolder;
}
