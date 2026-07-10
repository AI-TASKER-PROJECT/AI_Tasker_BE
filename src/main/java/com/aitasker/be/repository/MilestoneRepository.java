/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/MilestoneRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;
import com.aitasker.be.entity.MilestoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MilestoneRepository extends JpaRepository<MilestoneEntity, Integer> {
    // Note: Hàm `findByContractIdOrderByOrderIndexAsc` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    List<MilestoneEntity> findByContractIdOrderByOrderIndexAsc(Integer contractId);
    boolean existsByContractIdAndOrderIndex(Integer contractId, Integer orderIndex);
    // Note: Hàm `findByJobIdOrderByOrderIndexAsc` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    List<MilestoneEntity> findByJobIdOrderByOrderIndexAsc(Integer jobId);
    boolean existsByJobIdAndOrderIndex(Integer jobId, Integer orderIndex);
}
