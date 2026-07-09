/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/ContractDepositEntity.java
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
@Table(name = "contract_deposits")
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
public class ContractDepositEntity {
    // Note: Annotation nay danh dau khoa chinh cua entity.
    @Id
    // Note: Annotation nay cau hinh cach database/JPA sinh gia tri khoa chinh.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "deposit_id")
    private Long depositId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "contract_id", nullable = false)
    private Integer contractId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "business_id", nullable = false)
    private Integer businessId;

    @Column(name = "owner_account_id")
    private Integer ownerAccountId;

    @Column(name = "owner_role", nullable = false, length = 20)
    private String ownerRole;

    @Column(name = "required_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal requiredPercentage;

    @Column(name = "required_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal requiredAmount;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "deposit_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal depositAmount;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "held_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal heldAmount;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "refunded_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal refundedAmount;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "resolved_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal resolvedAmount;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "hold_transaction_id")
    private Long holdTransactionId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "refund_transaction_id")
    private Long refundTransactionId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "admin_id")
    private Integer adminId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "penalty_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal penaltyAmount;
    @Column(name = "penalty_beneficiary_account_id")
    private Integer penaltyBeneficiaryAccountId;
    @Column(name = "penalty_transaction_id")
    private Long penaltyTransactionId;
    @Column(name = "resolution_type", length = 50)
    private String resolutionType;
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

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
