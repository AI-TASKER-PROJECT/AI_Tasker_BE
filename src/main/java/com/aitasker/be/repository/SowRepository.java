/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/SowRepository.java
 * Đây là file gì: Repository truy cập dữ liệu bảng sow.
 * Mục đích note: cung cấp hàm tìm SoW theo job để trả kèm dữ liệu job.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.SowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SowRepository extends JpaRepository<SowEntity, Integer> {
    // Note: Hàm `findByJobId` lấy bản SoW gắn với một job để hiển thị chi tiết cấu trúc AI generate.
    Optional<SowEntity> findByJobId(Integer jobId);
}
