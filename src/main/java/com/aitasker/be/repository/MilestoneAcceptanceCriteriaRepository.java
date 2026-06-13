/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/MilestoneAcceptanceCriteriaRepository.java
 * Đây là file gì: Repository truy cập bảng nối milestone_acceptance_criteria.
 * Mục đích note: quản lý danh sách tiêu chí nghiệm thu được chọn cho từng milestone.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.MilestoneAcceptanceCriteriaEntity;
import com.aitasker.be.entity.MilestoneAcceptanceCriteriaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneAcceptanceCriteriaRepository extends JpaRepository<MilestoneAcceptanceCriteriaEntity, MilestoneAcceptanceCriteriaId> {
    // Note: Hàm `findByIdMilestoneId` lấy các tiêu chí đã gắn với một milestone.
    List<MilestoneAcceptanceCriteriaEntity> findByIdMilestoneId(Integer milestoneId);

    // Note: Hàm `deleteByIdMilestoneId` xóa danh sách tiêu chí cũ trước khi gắn lại danh sách mới cho milestone.
    void deleteByIdMilestoneId(Integer milestoneId);
}
