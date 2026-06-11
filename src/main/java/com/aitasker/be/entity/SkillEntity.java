/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/SkillEntity.java
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
@Table(name = "skills")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SkillEntity {
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "skill_id") private Integer skillId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "skill_code", nullable = false, unique = true, length = 50) private String skillCode;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "skill_name", nullable = false, length = 255) private String skillName;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "description") private String description;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "is_active", nullable = false) private Boolean isActive;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
