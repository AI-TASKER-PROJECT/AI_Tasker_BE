package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "domains")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DomainEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "domain_id") private Integer domainId;
    @Column(name = "domain_code", nullable = false, unique = true, length = 50) private String domainCode;
    @Column(name = "domain_name", nullable = false, length = 255) private String domainName;
    @Column(name = "description") private String description;
    @Column(name = "is_active", nullable = false) private Boolean isActive;
    @Column(name = "sort_order", nullable = false) private Integer sortOrder;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
