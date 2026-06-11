/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/SkillRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.SkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<SkillEntity, Integer> {
    // Note: Hàm `findBySkillCode` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<SkillEntity> findBySkillCode(String skillCode);
    boolean existsBySkillCode(String skillCode);
    // Note: Hàm `findByIsActiveTrueOrderBySkillNameAsc` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    List<SkillEntity> findByIsActiveTrueOrderBySkillNameAsc();
}
