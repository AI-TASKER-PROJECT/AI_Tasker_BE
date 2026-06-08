/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/ProposalRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.ProposalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProposalRepository extends JpaRepository<ProposalEntity, Integer> {
    boolean existsByJobIdAndExpertId(Integer jobId, Integer expertId);
    // Note: Hàm `findByJobId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    List<ProposalEntity> findByJobId(Integer jobId);
    // Note: Hàm `findByExpertIdOrderByCreatedAtDesc` lấy các proposal của chuyên gia hiện tại để expert theo dõi lịch sử đã nộp.
    List<ProposalEntity> findByExpertIdOrderByCreatedAtDesc(Integer expertId);
}
