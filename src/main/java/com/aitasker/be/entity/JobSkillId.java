/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/JobSkillId.java
 * Đây là file gì: File entity ánh xạ bảng database sang object Java, thể hiện cấu trúc dữ liệu được lưu trữ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

// Note: Annotation này cho phép class được nhúng làm khóa/phần dữ liệu trong entity khác.
@Embeddable
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class JobSkillId implements Serializable {
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "job_id") private Integer jobId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "skill_id") private Integer skillId;
}
