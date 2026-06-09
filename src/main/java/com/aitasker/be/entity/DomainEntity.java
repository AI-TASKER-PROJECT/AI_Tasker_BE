/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/DomainEntity.java
 * Đây là file gì: File entity ánh xạ bảng database sang object Java, thể hiện cấu trúc dữ liệu được lưu trữ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// Note: Annotation này ánh xạ class Java với một bảng trong database.
@Entity
// Note: Annotation này chỉ rõ bảng database tương ứng với entity.
@Table(name = "domains")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DomainEntity {
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "domain_id") private Integer domainId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "domain_code", nullable = false, unique = true, length = 50) private String domainCode;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "domain_name", nullable = false, length = 255) private String domainName;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "description") private String description;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "is_active", nullable = false) private Boolean isActive;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "sort_order", nullable = false) private Integer sortOrder;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
