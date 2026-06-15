/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/ContractEntity.java
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
import java.util.List;

// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Entity @Table(name = "contracts")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ContractEntity {
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "contract_id") private Integer contractId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "job_id", nullable = false) private Integer jobId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "business_id", nullable = false) private Integer businessId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "expert_id", nullable = false) private Integer expertId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "proposal_id") private Integer proposalId;
    @Column(name = "contract_title", length = 255) private String contractTitle;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "total_budget", nullable = false) private BigDecimal totalBudget;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "timeline_days", nullable = false) private Integer timelineDays;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "status", nullable = false, length = 50) private String status;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "business_accepted_at") private LocalDateTime businessAcceptedAt;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "expert_accepted_at") private LocalDateTime expertAcceptedAt;
    @Column(name = "business_nda_signed_at") private LocalDateTime businessNdaSignedAt;
    @Column(name = "expert_nda_signed_at") private LocalDateTime expertNdaSignedAt;
    @Column(name = "activated_at") private LocalDateTime activatedAt;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Transient private List<ContractMilestoneEntity> contractMilestones;
}
