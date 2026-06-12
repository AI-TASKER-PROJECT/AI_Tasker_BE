/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/MilestoneAcceptanceCriteriaId.java
 * Đây là file gì: Khóa chính tổng hợp cho bảng nối milestone_acceptance_criteria.
 * Mục đích note: thể hiện một milestone chọn một tiêu chí nghiệm thu từ danh mục nền tảng.
 */
package com.aitasker.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

// Note: Annotation này cho phép class được nhúng làm khóa chính tổng hợp trong entity bảng nối.
@Embeddable
// Note: Lombok sinh getter, setter, constructor và equals/hashCode cần thiết cho khóa JPA.
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class MilestoneAcceptanceCriteriaId implements Serializable {
    // Note: ID milestone được gắn tiêu chí nghiệm thu.
    @Column(name = "milestone_id") private Integer milestoneId;

    // Note: ID tiêu chí nghiệm thu được chọn từ danh mục hệ thống.
    @Column(name = "criteria_id") private Integer criteriaId;
}
