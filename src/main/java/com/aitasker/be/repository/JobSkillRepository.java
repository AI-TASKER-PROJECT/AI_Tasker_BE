/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/JobSkillRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.JobSkillEntity;
import com.aitasker.be.entity.JobSkillId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobSkillRepository extends JpaRepository<JobSkillEntity, JobSkillId> {
    // Note: Hàm `findByIdJobId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    List<JobSkillEntity> findByIdJobId(Integer jobId);
    // Note: Hàm `deleteByIdJobId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    void deleteByIdJobId(Integer jobId);
}
