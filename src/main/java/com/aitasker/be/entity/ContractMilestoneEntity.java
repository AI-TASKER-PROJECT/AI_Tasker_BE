/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/ContractMilestoneEntity.java
 * Đây là file gì: Entity lưu chi tiết milestone đã chốt trong hợp đồng.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_milestones")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ContractMilestoneEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_milestone_id") private Integer contractMilestoneId;
    @Column(name = "contract_id", nullable = false) private Integer contractId;
    @Column(name = "job_milestone_id", nullable = false) private Integer jobMilestoneId;
    @Column(name = "milestone_name", nullable = false, length = 255) private String milestoneName;
    @Column(name = "description") private String description;
    @Column(name = "original_budget", nullable = false) private BigDecimal originalBudget;
    @Column(name = "final_budget", nullable = false) private BigDecimal finalBudget;
    @Column(name = "order_index", nullable = false) private Integer orderIndex;
    @Column(name = "status", nullable = false, length = 50) private String status;
    @Column(name = "duration") private Integer duration;
    @Column(name = "duration_unit", length = 20) private String durationUnit;
    @Column(name = "criteria_snapshot") private String criteriaSnapshot;
    @Column(name = "deliverable_expectation") private String deliverableExpectation;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    public BigDecimal getDifference() {
        if (originalBudget == null || finalBudget == null) return null;
        return finalBudget.subtract(originalBudget);
    }
}
