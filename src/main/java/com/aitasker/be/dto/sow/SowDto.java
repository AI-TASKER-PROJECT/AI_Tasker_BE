/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/sow/SowDto.java
 * Đây là file gì: DTO chứa nội dung Statement of Work do AI generate cho job.
 * Nhiệm vụ: Gom các phần chính của SoW như tổng quan, mục tiêu, phạm vi, deliverable, giả định và ngoài phạm vi.
 */
package com.aitasker.be.dto.sow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Note: Annotation này giúp Lombok sinh getter, setter và các hàm tiện ích cho DTO.
@Data
// Note: Annotation này tạo builder để dựng SoW DTO rõ ràng trong code hoặc test.
@Builder
// Note: Annotation này tạo constructor rỗng cho Jackson deserialize JSON.
@NoArgsConstructor
// Note: Annotation này tạo constructor đầy đủ field khi cần dựng nhanh DTO.
@AllArgsConstructor
public class SowDto {
    private String title;
    private String overview;
    private List<String> objectives;
    private List<String> scopeOfWork;
    private List<String> deliverables;
    private List<String> assumptions;
    private List<String> outOfScope;
}
