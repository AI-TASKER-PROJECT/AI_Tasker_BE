/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/WalletTransactionEntity.java
 * Day la file gi: File entity anh xa du lieu nghiep vu voi bang trong database de JPA doc/ghi.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Note: Annotation nay anh xa class Java voi mot bang trong database.
@Entity
// Note: Annotation nay chi ro bang database tuong ung voi entity.
@Table(name = "wallet_transactions")
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
public class WalletTransactionEntity {
    // ─────────────────────────────────────────────────────────────────
    // Escrow transaction types (v2 milestone escrow model)
    // ─────────────────────────────────────────────────────────────────
    public static final String TX_ESCROW_DEPOSIT = "MILESTONE_ESCROW_DEPOSIT";
    public static final String TX_ESCROW_RELEASE = "MILESTONE_ESCROW_RELEASE";
    public static final String TX_ESCROW_REFUND = "MILESTONE_ESCROW_REFUND";
    public static final String TX_ESCROW_SETTLEMENT_PAYOUT = "MILESTONE_ESCROW_SETTLEMENT_PAYOUT";
    public static final String TX_ESCROW_SETTLEMENT_REFUND = "MILESTONE_ESCROW_SETTLEMENT_REFUND";

    // ─────────────────────────────────────────────────────────────────
    // Wallet transaction status (always POSTED per V31 constraint)
    // ─────────────────────────────────────────────────────────────────
    public static final String STATUS_POSTED = "POSTED";

    // Note: Annotation nay danh dau khoa chinh cua entity.
    @Id
    // Note: Annotation nay cau hinh cach database/JPA sinh gia tri khoa chinh.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "id")
    private Long id;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "system_wallet_id", nullable = false)
    private Long systemWalletId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "payment_order_id")
    private Long paymentOrderId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "direction", nullable = false, length = 10)
    private String direction;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "balance_type", nullable = false, length = 20)
    private String balanceType;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "balance_before", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceBefore;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "reference_id")
    private Long referenceId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Note: Annotation nay tu ghi thoi diem tao du lieu khi insert database.
    @CreationTimestamp
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
