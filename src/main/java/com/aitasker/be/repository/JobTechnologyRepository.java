/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/JobTechnologyRepository.java
 * Đây là file gì: Repository truy cập dữ liệu bảng trung gian job_technologies.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.JobTechnologyEntity;
import com.aitasker.be.entity.JobTechnologyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobTechnologyRepository extends JpaRepository<JobTechnologyEntity, JobTechnologyId> {
    List<JobTechnologyEntity> findByIdJobId(Integer jobId);
    void deleteByIdJobId(Integer jobId);
}
