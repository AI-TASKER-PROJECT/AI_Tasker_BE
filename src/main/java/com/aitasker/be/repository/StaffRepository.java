/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/StaffRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.StaffEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<StaffEntity, Integer> {
    // Note: Hàm `findByAccountId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<StaffEntity> findByAccountId(Integer accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select staff from StaffEntity staff order by staff.staffId")
    List<StaffEntity> findAllForDisputeRouting();
}
