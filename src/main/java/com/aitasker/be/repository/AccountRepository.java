/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/AccountRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.AccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {
    // Note: Hàm `findByEmail` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<AccountEntity> findByEmail(String email);
    // Note: Hàm `findByEmailIgnoreCase` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<AccountEntity> findByEmailIgnoreCase(String email);
    // Note: Hàm `findFirstByRoleRoleNameOrderByAccountIdAsc` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<AccountEntity> findFirstByRoleRoleNameOrderByAccountIdAsc(String roleName);
    List<AccountEntity> findAllByRoleRoleNameOrderByAccountIdAsc(String roleName);
    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Query("select a from AccountEntity a join fetch a.role where lower(a.email) = lower(:email)")
    // Note: Hàm `findByEmailWithRole` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<AccountEntity> findByEmailWithRole(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccountEntity a join fetch a.role where lower(a.email) = lower(:email)")
    Optional<AccountEntity> findByEmailWithRoleForUpdate(@Param("email") String email);
}
