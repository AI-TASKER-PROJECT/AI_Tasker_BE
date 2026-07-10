/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/BusinessProfileEntity.java
 * Đây là file gì: File entity ánh xạ bảng database sang object Java, thể hiện cấu trúc dữ liệu được lưu trữ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Entity @Table(name = "business_profiles")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BusinessProfileEntity {
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "business_id") private Integer businessId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "account_id", nullable = false, unique = true) private Integer accountId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "tax_code", nullable = false, unique = true, length = 50) private String taxCode;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "company_name", nullable = false, length = 255) private String companyName;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "address") private String address;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "business_license_url", length = 255) private String businessLicenseUrl;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "kyb_status", nullable = false, length = 50) private String kybStatus;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "approved_by") private Integer approvedBy;
    @Column(name = "rejection_reason", length = 500) private String rejectionReason;
    @Column(name = "verified_representative", length = 255) private String verifiedRepresentative;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    // Note: Annotation này đánh dấu dữ liệu chỉ dùng để trả response, không lưu xuống bảng business_profiles.
    @Transient private String fullName;
    @Transient private String email;
    @Transient private String phone;
}
