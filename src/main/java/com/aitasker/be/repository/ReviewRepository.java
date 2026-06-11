/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/ReviewRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Integer> {
    // Note: Hàm `findByContractId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    List<ReviewEntity> findByContractId(Integer contractId);
    boolean existsByContractIdAndReviewerId(Integer contractId, Integer reviewerId);
}
