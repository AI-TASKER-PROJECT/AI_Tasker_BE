/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/AcceptanceCriteriaEntity.java
 * Đây là file gì: Entity ánh xạ tiêu chí nghiệm thu thuộc sở hữu của một milestone.
 * Mục đích note: mỗi tiêu chí có nội dung và thứ tự riêng, không còn dùng catalog toàn hệ thống.
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

    // Note: Milestone sở hữu tiêu chí; xóa milestone sẽ xóa các tiêu chí liên quan.
    @Column(name = "milestone_id", nullable = false) private Integer milestoneId;

    // Note: Nội dung tiêu chí nghiệm thu hiển thị cho business và expert.
    @Column(name = "description", nullable = false) private String description;

    // Note: Thứ tự hiển thị trong milestone.
    @Column(name = "sort_order", nullable = false) private Integer sortOrder;

    // Note: Thời điểm tạo tiêu chí.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;

    // Note: Thời điểm cập nhật tiêu chí gần nhất.
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
