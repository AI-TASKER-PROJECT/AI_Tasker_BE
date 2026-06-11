/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/ReviewEntity.java
 * Đây là file gì: File entity ánh xạ bảng database sang object Java, thể hiện cấu trúc dữ liệu được lưu trữ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Note: Annotation này ánh xạ class Java với một bảng trong database.
@Entity
// Note: Annotation này chỉ rõ bảng database tương ứng với entity.
@Table(name = "reviews")
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
public class ReviewEntity {
    // Note: Annotation này đánh dấu khóa chính của entity.
    @Id
    // Note: Annotation này cấu hình cách database/JPA sinh giá trị khóa chính.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "review_id")
    private Integer reviewId;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "contract_id", nullable = false)
    private Integer contractId;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "reviewer_id", nullable = false)
    private Integer reviewerId;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "reviewee_id", nullable = false)
    private Integer revieweeId;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "rating", nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "comment")
    private String comment;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
