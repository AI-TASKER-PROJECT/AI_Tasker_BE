package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_domains")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class JobDomainEntity {
    @EmbeddedId private JobDomainId id;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
}
