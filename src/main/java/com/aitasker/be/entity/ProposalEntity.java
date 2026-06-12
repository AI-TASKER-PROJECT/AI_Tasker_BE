/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/ProposalEntity.java
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
@Entity @Table(name = "proposals")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProposalEntity {
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "proposal_id") private Integer proposalId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "job_id", nullable = false) private Integer jobId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "expert_id", nullable = false) private Integer expertId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "domain_id") private Integer domainId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "skill_id") private Integer skillId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "technical_solution", nullable = false) private String technicalSolution;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "bid_amount", nullable = false) private BigDecimal bidAmount;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "status", nullable = false, length = 50) private String status;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
