/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/payment/WithdrawalReviewRequest.java
 * Day la file gi: File DTO khai bao du lieu request/response di qua API hoac qua service.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.dto.payment;

import lombok.Data;

// Note: Annotation nay giup Lombok sinh getter, setter va cac ham tien ich cho du lieu.
@Data
public class WithdrawalReviewRequest {
    private String adminNote;
}
