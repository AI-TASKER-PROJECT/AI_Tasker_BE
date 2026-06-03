package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "milestones")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MilestoneEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "milestone_id") private Integer milestoneId;
    @Column(name = "contract_id", nullable = false) private Integer contractId;
    @Column(name = "milestone_name", nullable = false, length = 255) private String milestoneName;
    @Column(name = "funds_allocated", nullable = false) private BigDecimal fundsAllocated;
    @Column(name = "order_index", nullable = false) private Integer orderIndex;
    @Column(name = "status", nullable = false, length = 50) private String status;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
