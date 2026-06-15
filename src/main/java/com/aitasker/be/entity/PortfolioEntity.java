/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/PortfolioEntity.java
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
@Table(name = "portfolios")
// Note: Annotation này giúp Lombok sinh getter để đọc field.
@Getter
// Note: Annotation này giúp Lombok sinh setter để cập nhật field.
@Setter
// Note: Annotation này giúp Lombok tạo builder để khởi tạo object rõ ràng hơn.
@Builder
// Note: Annotation này giúp Lombok sinh constructor rỗng cho JPA hoặc deserialize dữ liệu.
@NoArgsConstructor
// Note: Annotation này giúp Lombok sinh constructor nhận đầy đủ field.
@AllArgsConstructor
public class PortfolioEntity {
    // Note: Annotation này đánh dấu khóa chính của entity.
    @Id
    // Note: Annotation này cấu hình cách database/JPA sinh giá trị khóa chính.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "portfolio_id")
    private Integer portfolioId;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "expert_id", nullable = false)
    private Integer expertId;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "domain_ids", nullable = false)
    private String domainIds;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "skill_ids", nullable = false)
    private String skillIds;

    @Column(name = "technology_ids", nullable = false)
    private String technologyIds;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "years_experience", nullable = false)
    private Integer yearsExperience;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "certificates")
    private String certificates;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "self_description", nullable = false)
    private String selfDescription;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @UpdateTimestamp
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
