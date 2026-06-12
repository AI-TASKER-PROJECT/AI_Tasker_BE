/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/MilestoneAcceptanceCriteriaEntity.java
 * Đây là file gì: Entity bảng nối giữa milestone và tiêu chí nghiệm thu.
 * Mục đích note: lưu các tiêu chí nền tảng mà business chọn cho từng milestone.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// Note: Annotation này ánh xạ class Java với bảng nối milestone_acceptance_criteria.
@Entity
// Note: Annotation này chỉ rõ bảng database lưu quan hệ chọn tiêu chí cho milestone.
@Table(name = "milestone_acceptance_criteria")
// Note: Lombok sinh getter, setter, builder và constructor để thao tác dữ liệu bảng nối.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MilestoneAcceptanceCriteriaEntity {
    // Note: Khóa chính tổng hợp gồm milestone_id và criteria_id.
    @EmbeddedId private MilestoneAcceptanceCriteriaId id;

    // Note: Thời điểm tiêu chí được gắn vào milestone.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
}
