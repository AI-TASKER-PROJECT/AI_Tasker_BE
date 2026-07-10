/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/DomainRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.DomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DomainRepository extends JpaRepository<DomainEntity, Integer> {
    // Note: Hàm `findByDomainCode` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<DomainEntity> findByDomainCode(String domainCode);
    boolean existsByDomainCode(String domainCode);
    // Note: Hàm `findByIsActiveTrueOrderBySortOrderAscDomainNameAsc` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    List<DomainEntity> findByIsActiveTrueOrderBySortOrderAscDomainNameAsc();
}
