/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/ContractChangeRequestEntity.java
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
@Entity @Table(name = "contract_change_requests")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ContractChangeRequestEntity {
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "request_id") private Integer requestId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "contract_id", nullable = false) private Integer contractId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "requested_by_account_id", nullable = false) private Integer requestedByAccountId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "change_type", nullable = false, length = 50) private String changeType;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "change_summary", nullable = false) private String changeSummary;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "proposed_budget") private BigDecimal proposedBudget;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "proposed_timeline_days") private Integer proposedTimelineDays;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "status", nullable = false, length = 20) private String status;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "reviewed_by_account_id") private Integer reviewedByAccountId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "reviewed_at") private LocalDateTime reviewedAt;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
