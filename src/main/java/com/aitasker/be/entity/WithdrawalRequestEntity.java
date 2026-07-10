/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/WithdrawalRequestEntity.java
 * Day la file gi: File entity anh xa du lieu nghiep vu voi bang trong database de JPA doc/ghi.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Note: Annotation nay anh xa class Java voi mot bang trong database.
@Entity
// Note: Annotation nay chi ro bang database tuong ung voi entity.
@Table(name = "withdrawal_requests")
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
public class WithdrawalRequestEntity {
    // Note: Annotation nay danh dau khoa chinh cua entity.
    @Id
    // Note: Annotation nay cau hinh cach database/JPA sinh gia tri khoa chinh.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "withdrawal_id")
    private Long withdrawalId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "bank_name", nullable = false, length = 120)
    private String bankName;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "bank_account_number", nullable = false, length = 80)
    private String bankAccountNumber;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "bank_account_holder", nullable = false, length = 160)
    private String bankAccountHolder;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "hold_transaction_id")
    private Long holdTransactionId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "review_transaction_id")
    private Long reviewTransactionId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "admin_id")
    private Integer adminId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // Note: Annotation nay tu ghi thoi diem tao du lieu khi insert database.
    @CreationTimestamp
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Note: Annotation nay tu ghi thoi diem cap nhat du lieu gan nhat.
    @UpdateTimestamp
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
