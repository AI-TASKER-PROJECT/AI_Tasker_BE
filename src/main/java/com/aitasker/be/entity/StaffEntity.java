package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity @Table(name = "staffs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StaffEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id") private Integer staffId;
    @Column(name = "account_id", nullable = false, unique = true) private Integer accountId;
    @Column(name = "specialization", length = 255) private String specialization;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
