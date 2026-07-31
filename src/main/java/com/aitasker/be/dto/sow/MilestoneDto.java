/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/sow/MilestoneDto.java
 * Đây là file gì: DTO mô tả một milestone do AI generate ra trong luồng tạo SoW.
 * Nhiệm vụ: Chứa tên, mô tả, thời lượng và ngân sách milestone để trả về cho doanh nghiệp kiểm tra.
 */
package com.aitasker.be.dto.sow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// Note: Annotation này giúp Lombok sinh getter, setter và các hàm tiện ích cho DTO.
@Data
// Note: Annotation này tạo builder để khởi tạo DTO rõ ràng trong code hoặc test.
@Builder
// Note: Annotation này tạo constructor rỗng cho Jackson deserialize JSON.
@NoArgsConstructor
// Note: Annotation này tạo constructor đầy đủ field khi cần dựng nhanh DTO.
@AllArgsConstructor
public class MilestoneDto {
    private String name;
    private String description;
    private Integer duration;
    private String durationUnit;
    // Allocation that matches the Business-entered budget and remains backward compatible.
    @Schema(description = "Milestone allocation scaled to the Business-entered budget.")
    private BigDecimal budget;
    // Advisory allocation that matches budgetAssessment.recommendedBudget.
    @Schema(description = "Milestone allocation scaled to the advisory recommended budget.")
    private BigDecimal recommendedBudget;
    private List<String> acceptanceCriteria;
}
