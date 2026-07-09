/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/MilestoneEntity.java
 * Đây là file gì: File entity ánh xạ bảng database sang object Java, thể hiện cấu trúc dữ liệu được lưu trữ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Entity @Table(name = "milestones")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MilestoneEntity {
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "milestone_id") private Integer milestoneId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "job_id", nullable = false) private Integer jobId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "contract_id") private Integer contractId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @JsonAlias("name")
    @Column(name = "milestone_name", nullable = false, length = 255) private String milestoneName;
    @Column(name = "description") private String description;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @JsonAlias("budget")
    @Column(name = "funds_allocated", nullable = false) private BigDecimal fundsAllocated;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "order_index", nullable = false) private Integer orderIndex;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "status", nullable = false, length = 50) private String status;
    @Column(name = "duration") private Integer duration;
    @Column(name = "duration_unit", length = 20) private String durationUnit;
    @Column(name = "reject_count", nullable = false) private Integer rejectCount;
    @Column(name = "last_rejection_feedback") private String lastRejectionFeedback;
    @Column(name = "escrow_released_at") private LocalDateTime escrowReleasedAt;
    @Column(name = "settlement_source_type", length = 50) private String settlementSourceType;
    @Column(name = "settlement_source_id") private Long settlementSourceId;
    // Note: Nguon settlement dong milestone (spec 13.3.2, migration V50): dispute hoac termination request.
    @Column(name = "resolved_by_dispute_id") private Integer resolvedByDisputeId;
    @Column(name = "resolved_by_termination_request_id") private Long resolvedByTerminationRequestId;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    // Note: Chuỗi tiêu chí từ AI hoặc frontend, dùng khi tạo/cập nhật cả milestone.
    @Transient private List<String> acceptanceCriteria;
    // Note: Bản ghi đã persist có id để frontend sửa/xóa từng tiêu chí.
    @Transient private List<AcceptanceCriteriaEntity> criteria;
}
