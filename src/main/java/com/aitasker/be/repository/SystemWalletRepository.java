/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/SystemWalletRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.SystemWalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface SystemWalletRepository extends JpaRepository<SystemWalletEntity, Long> {
    // Note: Hàm `findTopByOrderBySystemWalletIdAsc` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<SystemWalletEntity> findTopByOrderBySystemWalletIdAsc();
    // Note: Hàm `findByAccountId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<SystemWalletEntity> findByAccountId(Integer accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from SystemWalletEntity w where w.accountId = :accountId")
    Optional<SystemWalletEntity> findByAccountIdForUpdate(Integer accountId);
}
