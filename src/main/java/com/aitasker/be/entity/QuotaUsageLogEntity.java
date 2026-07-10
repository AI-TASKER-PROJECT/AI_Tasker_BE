/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/QuotaUsageLogEntity.java
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

import java.time.LocalDateTime;

// Note: Annotation nay anh xa class Java voi mot bang trong database.
@Entity
// Note: Annotation nay chi ro bang database tuong ung voi entity.
@Table(name = "quota_usage_logs")
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
public class QuotaUsageLogEntity {
    // Note: Annotation nay danh dau khoa chinh cua entity.
    @Id
    // Note: Annotation nay cau hinh cach database/JPA sinh gia tri khoa chinh.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "quota_usage_id")
    private Long quotaUsageId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "quota_type", nullable = false, length = 20)
    private String quotaType;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "amount", nullable = false)
    private Integer amount;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "balance_before", nullable = false)
    private Integer balanceBefore;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "reference_id")
    private Long referenceId;

    // Note: Annotation nay tu ghi thoi diem tao du lieu khi insert database.
    @CreationTimestamp
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
