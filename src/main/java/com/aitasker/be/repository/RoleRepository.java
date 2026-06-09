/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/RoleRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {
    // Note: Hàm `findByRoleName` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<RoleEntity> findByRoleName(String roleName);
    // Note: Hàm `findByRoleNameIgnoreCase` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<RoleEntity> findByRoleNameIgnoreCase(String roleName);
}
