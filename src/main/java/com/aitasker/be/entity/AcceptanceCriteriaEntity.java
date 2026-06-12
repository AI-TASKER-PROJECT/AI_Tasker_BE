/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/AcceptanceCriteriaEntity.java
 * Đây là file gì: Entity ánh xạ bảng acceptance_criteria, lưu danh mục tiêu chí nghiệm thu do nền tảng cung cấp.
 * Mục đích note: giải thích các trường dùng để business chọn tiêu chí cho từng milestone.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// Note: Annotation này ánh xạ class Java với bảng acceptance_criteria.
@Entity
// Note: Annotation này chỉ rõ bảng lưu danh mục tiêu chí nghiệm thu của hệ thống.
@Table(name = "acceptance_criteria")
// Note: Lombok sinh getter, setter, builder và constructor để thao tác dữ liệu tiêu chí.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AcceptanceCriteriaEntity {
    // Note: Khóa chính của tiêu chí nghiệm thu.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "criteria_id") private Integer criteriaId;

    // Note: Mã tiêu chí duy nhất, dùng để quản lý dữ liệu seed và tránh trùng.
    @Column(name = "criteria_code", nullable = false, unique = true, length = 100) private String criteriaCode;

    // Note: Nhóm tiêu chí để giao diện có thể phân loại khi business chọn cho milestone.
    @Column(name = "category", length = 100) private String category;

    // Note: Nội dung tiêu chí nghiệm thu hiển thị cho business và expert.
    @Column(name = "description", nullable = false) private String description;

    // Note: Cờ bật/tắt tiêu chí trong danh mục hệ thống.
    @Column(name = "is_active", nullable = false) private Boolean isActive;

    // Note: Thứ tự hiển thị của tiêu chí trên giao diện.
    @Column(name = "sort_order", nullable = false) private Integer sortOrder;

    // Note: Thời điểm tạo tiêu chí.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;

    // Note: Thời điểm cập nhật tiêu chí gần nhất.
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
