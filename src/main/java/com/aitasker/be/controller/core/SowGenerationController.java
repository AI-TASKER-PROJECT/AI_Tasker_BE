/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/SowGenerationController.java
 * Đây là file gì: File controller tiếp nhận request tạo SoW bằng AI cho job.
 * Nhiệm vụ: Nhận yêu cầu thô từ doanh nghiệp, gọi service AI và trả về SoW/milestone đã được cấu trúc hóa.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.dto.sow.GenerateSowRequest;
import com.aitasker.be.dto.sow.GenerateSowResponse;
import com.aitasker.be.dto.sow.ReallocateSowBudgetRequest;
import com.aitasker.be.dto.sow.ReallocateSowBudgetResponse;
import com.aitasker.be.service.core.AiSowGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Note: Annotation này nhóm API tạo SoW trên Swagger để dễ tìm khi test.
@Tag(name = "SoW Generation", description = "AI generate Statement of Work for jobs")
// Note: Annotation này biến class thành REST controller trả JSON.
@RestController
// Note: Annotation này đặt prefix API cho nhóm tạo SoW bằng AI.
@RequestMapping("/api/jobs")
// Note: Annotation này giúp Lombok sinh constructor cho dependency final.
@RequiredArgsConstructor
public class SowGenerationController {
    private final AiSowGenerationService aiSowGenerationService;

    // Note: Annotation này mô tả API trên Swagger để người test hiểu chức năng generate SoW.
    @Operation(summary = "Generate SoW", description = "Generate structured SoW, milestones, Business-budget allocations, and an advisory AI budget assessment. Business remains the final budget authority.")
    // Note: Annotation này khai báo endpoint POST dùng để tạo SoW từ dữ liệu yêu cầu dự án.
    @PostMapping("/generate-sow")
    // Note: Hàm nhận request đã validate, chuyển qua service AI xử lý và trả response cho client.
    public GenerateSowResponse generateSow(@Valid @RequestBody GenerateSowRequest request) {
        return aiSowGenerationService.generateSow(request);
    }

    @Operation(
            summary = "Reallocate custom SoW budget",
            description = "Proportionally allocate a Business-selected custom whole-VND budget across generated milestones without calling AI or persisting data."
    )
    @PostMapping("/reallocate-sow-budget")
    public ReallocateSowBudgetResponse reallocateSowBudget(
            @Valid @RequestBody ReallocateSowBudgetRequest request) {
        return aiSowGenerationService.reallocateSowBudget(request);
    }
}
