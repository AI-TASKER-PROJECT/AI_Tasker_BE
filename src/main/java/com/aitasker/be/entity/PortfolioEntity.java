package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "portfolios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Integer portfolioId;

    @Column(name = "expert_id", nullable = false)
    private Integer expertId;

    @Column(name = "context", nullable = false)
    private String context;

    @Column(name = "data_processing", nullable = false)
    private String dataProcessing;

    @Column(name = "model_architecture", nullable = false)
    private String modelArchitecture;

    @Column(name = "performance_metrics", nullable = false)
    private String performanceMetrics;

    @Column(name = "poc_url")
    private String pocUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
