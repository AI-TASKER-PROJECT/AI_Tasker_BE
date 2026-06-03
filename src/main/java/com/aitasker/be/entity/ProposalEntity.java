package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "proposals")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProposalEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proposal_id") private Integer proposalId;
    @Column(name = "job_id", nullable = false) private Integer jobId;
    @Column(name = "expert_id", nullable = false) private Integer expertId;
    @Column(name = "technical_solution", nullable = false) private String technicalSolution;
    @Column(name = "bid_amount", nullable = false) private BigDecimal bidAmount;
    @Column(name = "status", nullable = false, length = 50) private String status;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
