/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/BusinessProfileRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.BusinessProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfileEntity, Integer> {
    // Note: Hàm `findByAccountId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<BusinessProfileEntity> findByAccountId(Integer accountId);
    boolean existsByTaxCode(String taxCode);

    @Query("SELECT COUNT(b) > 0 FROM BusinessProfileEntity b WHERE b.taxCode = :taxCode AND b.accountId <> :accountId")
    boolean existsByTaxCodeExcludingAccount(@Param("taxCode") String taxCode, @Param("accountId") Integer accountId);
}
