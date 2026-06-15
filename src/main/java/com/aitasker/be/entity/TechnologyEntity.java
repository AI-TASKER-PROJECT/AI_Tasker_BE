/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/TechnologyEntity.java
 * Đây là file gì: Entity ánh xạ bảng technologies, lưu danh mục công nghệ do hệ thống cung cấp.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "technologies")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TechnologyEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "technology_id") private Integer technologyId;
    @Column(name = "technology_code", nullable = false, unique = true, length = 50) private String technologyCode;
    @Column(name = "technology_name", nullable = false, unique = true, length = 255) private String technologyName;
    @Column(name = "description") private String description;
    @Column(name = "is_active", nullable = false) private Boolean isActive;
    @Column(name = "sort_order", nullable = false) private Integer sortOrder;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
