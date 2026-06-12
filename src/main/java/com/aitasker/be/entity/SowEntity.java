/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/SowEntity.java
 * Đây là file gì: Entity ánh xạ bảng sow, lưu cấu trúc Statement of Work do AI generate hoặc doanh nghiệp xác nhận cho từng job.
 * Mục đích note: giải thích các field chính để đọc hiểu dữ liệu SoW trong flow tạo job.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// Note: Annotation này ánh xạ class Java với bảng sow trong database.
@Entity
// Note: Annotation này chỉ rõ bảng database lưu cấu trúc SoW theo job.
@Table(name = "sow")
// Note: Lombok sinh getter, setter, builder và constructor để entity dùng được với JPA và request/response JSON.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SowEntity {
    // Note: Khóa chính của bảng sow.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sow_id") private Integer sowId;

    // Note: Job mà bản SoW này phục vụ.
    @Column(name = "job_id", nullable = false, unique = true) private Integer jobId;

    // Note: Tiêu đề SoW do AI generate hoặc doanh nghiệp chỉnh lại.
    @Column(name = "title", nullable = false, length = 255) private String title;

    // Note: Tổng quan ngắn gọn về bài toán và định hướng triển khai.
    @Column(name = "overview") private String overview;

    // Note: Danh sách mục tiêu, lưu dạng text JSON để giữ đúng cấu trúc AI trả về.
    @Column(name = "objectives") private String objectives;

    // Note: Danh sách phạm vi công việc, lưu dạng text JSON để frontend đọc lại được theo mảng.
    @Column(name = "scope_of_work") private String scopeOfWork;

    // Note: Danh sách sản phẩm bàn giao, đặt tên theo yêu cầu database là deliverable.
    @Column(name = "deliverable") private String deliverable;

    // Note: Các giả định khi thực hiện dự án.
    @Column(name = "assumptions") private String assumptions;

    // Note: Các hạng mục không nằm trong phạm vi thực hiện.
    @Column(name = "out_of_scope") private String outOfScope;

    // Note: Thời điểm tạo bản SoW.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;

    // Note: Thời điểm cập nhật bản SoW gần nhất.
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
