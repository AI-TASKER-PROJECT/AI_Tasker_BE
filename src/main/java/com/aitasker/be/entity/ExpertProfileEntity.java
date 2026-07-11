/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/ExpertProfileEntity.java
 * Đây là file gì: File entity ánh xạ bảng database sang object Java, thể hiện cấu trúc dữ liệu được lưu trữ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Entity @Table(name = "expert_profiles")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ExpertProfileEntity {
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "expert_id") private Integer expertId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "account_id", nullable = false, unique = true) private Integer accountId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "national_id", nullable = false, unique = true, length = 50) private String nationalId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "portfolio_url", length = 255) private String portfolioUrl;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "years_of_experience") private Integer yearsOfExperience;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "kyc_status", nullable = false, length = 50) private String kycStatus;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "approved_by") private Integer approvedBy;
    @Column(name = "rejection_reason", length = 500) private String rejectionReason;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    // Note: Annotation này đánh dấu dữ liệu chỉ dùng để trả response, không lưu xuống bảng expert_profiles.
    @Transient private String fullName;
    @Transient private String email;
    // Note: Annotation này đánh dấu dữ liệu chỉ dùng để trả response, không lưu xuống bảng expert_profiles.
    @Transient private String phone;
    // Note: Annotation này đánh dấu dữ liệu chỉ dùng để trả response, không lưu xuống bảng expert_profiles.
    @Transient private String title;
    @Transient private BigDecimal averageRating;
}
