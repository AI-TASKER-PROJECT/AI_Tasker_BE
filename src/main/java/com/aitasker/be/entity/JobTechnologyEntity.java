/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/JobTechnologyEntity.java
 * Đây là file gì: Entity bảng trung gian job_technologies, thể hiện job yêu cầu những công nghệ nào.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_technologies")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class JobTechnologyEntity {
    @EmbeddedId private JobTechnologyId id;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
}
