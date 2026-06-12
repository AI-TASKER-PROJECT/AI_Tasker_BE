/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/AcceptanceCriteriaRepository.java
 * Đây là file gì: Repository truy cập danh mục tiêu chí nghiệm thu của nền tảng.
 * Mục đích note: cung cấp truy vấn danh sách tiêu chí để business chọn cho milestone.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.AcceptanceCriteriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcceptanceCriteriaRepository extends JpaRepository<AcceptanceCriteriaEntity, Integer> {
    // Note: Hàm `findByIsActiveTrueOrderBySortOrderAscCriteriaIdAsc` lấy tiêu chí đang bật theo thứ tự hiển thị.
    List<AcceptanceCriteriaEntity> findByIsActiveTrueOrderBySortOrderAscCriteriaIdAsc();

    // Note: Hàm `findAllByOrderBySortOrderAscCriteriaIdAsc` lấy toàn bộ tiêu chí cho admin hoặc tài liệu nội bộ.
    List<AcceptanceCriteriaEntity> findAllByOrderBySortOrderAscCriteriaIdAsc();

    // Note: Hàm `findByCriteriaCode` hỗ trợ kiểm tra trùng mã tiêu chí khi quản trị dữ liệu nền tảng.
    Optional<AcceptanceCriteriaEntity> findByCriteriaCode(String criteriaCode);
}
