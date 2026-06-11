/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/JobSkillEntity.java
 * Đây là file gì: File entity ánh xạ bảng database sang object Java, thể hiện cấu trúc dữ liệu được lưu trữ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// Note: Annotation này ánh xạ class Java với một bảng trong database.
@Entity
// Note: Annotation này chỉ rõ bảng database tương ứng với entity.
@Table(name = "job_skills")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class JobSkillEntity {
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @EmbeddedId private JobSkillId id;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "required_level", length = 50) private String requiredLevel;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "is_mandatory", nullable = false) private Boolean isMandatory;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "min_years_experience") private Integer minYearsExperience;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
}
