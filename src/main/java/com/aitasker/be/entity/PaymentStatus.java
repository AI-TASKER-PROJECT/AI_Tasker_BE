/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/PaymentStatus.java
 * Day la file gi: File enum khai bao tap gia tri co dinh dung chung trong nghiep vu hoac luu database.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.entity;

public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    CANCELLED,
    EXPIRED
}
