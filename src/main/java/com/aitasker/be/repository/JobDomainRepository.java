/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/JobDomainRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.JobDomainEntity;
import com.aitasker.be.entity.JobDomainId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobDomainRepository extends JpaRepository<JobDomainEntity, JobDomainId> {
    // Note: Hàm `findByIdJobId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    List<JobDomainEntity> findByIdJobId(Integer jobId);
    // Note: Hàm `deleteByIdJobId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    void deleteByIdJobId(Integer jobId);
}
