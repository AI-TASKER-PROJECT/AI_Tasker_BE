/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/JobTechnologyId.java
 * Đây là file gì: Khóa chính ghép cho bảng job_technologies.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class JobTechnologyId implements Serializable {
    @Column(name = "job_id") private Integer jobId;
    @Column(name = "technology_id") private Integer technologyId;
}
