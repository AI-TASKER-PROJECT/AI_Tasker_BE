/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/TransactionEntity.java
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
@Entity @Table(name = "transactions")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionEntity {
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "transaction_id") private Long transactionId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "milestone_id", nullable = false) private Integer milestoneId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "amount", nullable = false) private BigDecimal amount;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "commission_fee", nullable = false) private BigDecimal commissionFee;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "transaction_type", nullable = false, length = 50) private String transactionType;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "status", nullable = false, length = 50) private String status;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
