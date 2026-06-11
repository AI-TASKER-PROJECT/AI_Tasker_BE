/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/JobRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<JobEntity, Integer> {
    // Note: Hàm `findByBusinessId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    List<JobEntity> findByBusinessId(Integer businessId);
    // Note: Hàm `findByBusinessIdOrderByCreatedAtDesc` lấy job của một doanh nghiệp theo thứ tự mới nhất để business kiểm tra job nháp/job đã mở.
    List<JobEntity> findByBusinessIdOrderByCreatedAtDesc(Integer businessId);
    // Note: Hàm `findByStatus` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    List<JobEntity> findByStatus(String status);
    // Note: Hàm `findByStatusOrderByPublishedAtDescCreatedAtDesc` lấy các job public mới nhất cho marketplace của chuyên gia.
    List<JobEntity> findByStatusOrderByPublishedAtDescCreatedAtDesc(String status);
}
