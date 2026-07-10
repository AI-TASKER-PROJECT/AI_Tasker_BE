/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/AcceptanceCriteriaRepository.java
 * Đây là file gì: Repository truy cập danh mục tiêu chí nghiệm thu của nền tảng.
 * Mục đích note: cung cấp truy vấn danh sách tiêu chí để business chọn cho milestone.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.AcceptanceCriteriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcceptanceCriteriaRepository extends JpaRepository<AcceptanceCriteriaEntity, Integer> {
    List<AcceptanceCriteriaEntity> findByMilestoneIdOrderBySortOrderAscCriteriaIdAsc(Integer milestoneId);

    void deleteByMilestoneId(Integer milestoneId);
}
