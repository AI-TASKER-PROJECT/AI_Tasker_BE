/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/JobEntity.java
 * Đây là file gì: File entity ánh xạ bảng database sang object Java, thể hiện cấu trúc dữ liệu được lưu trữ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Entity @Table(name = "jobs")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class JobEntity {
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "job_id") private Integer jobId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "business_id", nullable = false) private Integer businessId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "title", nullable = false, length = 255) private String title;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "raw_requirements", nullable = false) private String rawRequirements;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "structured_sow") private String structuredSow;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "budget", nullable = false) private BigDecimal budget;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "status", nullable = false, length = 50) private String status;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "planned_duration_value") private Integer plannedDurationValue;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "planned_duration_unit", length = 20) private String plannedDurationUnit;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "is_hot") private Boolean isHot;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "hot_until") private LocalDateTime hotUntil;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "published_at") private LocalDateTime publishedAt;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    // Note: Field này không lưu database, chỉ trả về tổng proposal để giao diện job public hiển thị số liệu.
    @Transient private Long proposalsCount;
}
