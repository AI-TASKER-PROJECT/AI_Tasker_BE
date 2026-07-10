/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/PaymentOrderEntity.java
 * Day la file gi: File entity anh xa du lieu nghiep vu voi bang trong database de JPA doc/ghi.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Note: Annotation nay anh xa class Java voi mot bang trong database.
@Entity
// Note: Annotation nay chi ro bang database tuong ung voi entity.
@Table(name = "payment_order")
// Note: Annotation nay giup Lombok sinh getter de doc field.
@Getter
// Note: Annotation nay giup Lombok sinh setter de cap nhat field.
@Setter
// Note: Annotation nay giup Lombok tao builder de khoi tao object ro rang hon.
@Builder
// Note: Annotation nay giup Lombok sinh constructor rong cho JPA hoac deserialize du lieu.
@NoArgsConstructor
// Note: Annotation nay giup Lombok sinh constructor nhan day du field.
@AllArgsConstructor
public class PaymentOrderEntity {
    // Note: Annotation nay danh dau khoa chinh cua entity.
    @Id
    // Note: Annotation nay cau hinh cach database/JPA sinh gia tri khoa chinh.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "id")
    private Long id;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "business_id")
    private Long businessId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "account_id")
    private Long accountId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "job_id")
    private Long jobId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "milestone_id")
    private Long milestoneId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // Note: Annotation nay cau hinh cach luu enum xuong database.
    @Enumerated(EnumType.STRING)
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "provider", nullable = false, length = 20)
    private PaymentProvider provider;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "purpose", nullable = false, length = 50)
    private String purpose;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "provider_txn_ref", unique = true, length = 100)
    private String providerTxnRef;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "provider_transaction_no", length = 100)
    private String providerTransactionNo;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "provider_response_code", length = 20)
    private String providerResponseCode;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "provider_secure_hash", columnDefinition = "TEXT")
    private String providerSecureHash;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "provider_order_code")
    private Long providerOrderCode;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "provider_payment_link_id", length = 100)
    private String providerPaymentLinkId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "checkout_url", columnDefinition = "TEXT")
    private String checkoutUrl;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "cancel_url", columnDefinition = "TEXT")
    private String cancelUrl;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "return_url", columnDefinition = "TEXT")
    private String returnUrl;

    // Note: Annotation nay cau hinh cach luu enum xuong database.
    @Enumerated(EnumType.STRING)
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Note: Annotation nay tu ghi thoi diem tao du lieu khi insert database.
    @CreationTimestamp
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Note: Annotation nay tu ghi thoi diem cap nhat du lieu gan nhat.
    @UpdateTimestamp
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
