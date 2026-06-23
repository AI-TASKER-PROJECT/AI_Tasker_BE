/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/MembershipPackageEntity.java
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
@Table(name = "membership_packages")
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
public class MembershipPackageEntity {
    // Note: Annotation nay danh dau khoa chinh cua entity.
    @Id
    // Note: Annotation nay cau hinh cach database/JPA sinh gia tri khoa chinh.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "package_id")
    private Long packageId;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "role_type", nullable = false, length = 20)
    private String roleType;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "package_code", nullable = false, unique = true, length = 80)
    private String packageCode;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "package_name", nullable = false, length = 120)
    private String packageName;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "badge_duration_days", nullable = false)
    private Integer badgeDurationDays;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "job_post_quota", nullable = false)
    private Integer jobPostQuota;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "proposal_quota", nullable = false)
    private Integer proposalQuota;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "recommend_visibility", nullable = false)
    private Boolean recommendVisibility;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

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
