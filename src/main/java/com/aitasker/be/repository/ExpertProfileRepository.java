/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/ExpertProfileRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.ExpertProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpertProfileRepository extends JpaRepository<ExpertProfileEntity, Integer> {
    // Note: Hàm `findByAccountId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<ExpertProfileEntity> findByAccountId(Integer accountId);
    // Note: Hàm `findByNationalId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<ExpertProfileEntity> findByNationalId(String nationalId);
}
