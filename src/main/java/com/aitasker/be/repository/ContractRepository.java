/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/ContractRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;
import com.aitasker.be.entity.ContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ContractRepository extends JpaRepository<ContractEntity, Integer> {
    // Note: Hàm `findByBusinessId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    List<ContractEntity> findByBusinessId(Integer businessId);
    // Note: Hàm `findByExpertId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    List<ContractEntity> findByExpertId(Integer expertId);
    // Note: Hàm `findByJobId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<ContractEntity> findByJobId(Integer jobId);
}
