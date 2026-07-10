/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/CatalogController.java
 * Đây là file gì: File controller nhận request HTTP, gọi service phù hợp và trả response cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.catalog.DomainRequest;
import com.aitasker.be.dto.catalog.JobSkillAssignmentRequest;
import com.aitasker.be.dto.catalog.SkillRequest;
import com.aitasker.be.dto.catalog.TechnologyRequest;
import com.aitasker.be.entity.*;
import com.aitasker.be.service.core.CatalogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Note: Annotation này biến class thành REST controller để nhận request và trả JSON.
@RestController
// Note: Annotation này đặt prefix đường dẫn API cho controller hoặc method.
@RequestMapping("/api/v1")
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalogService;

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/domains")
    @SecurityRequirements
    // Note: Hàm `listDomains` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> listDomains(@RequestParam(defaultValue = "false") Boolean activeOnly) {
        return ResponseEntity.ok(ApiResponse.success("LIST DOMAINS SUCCESS", catalogService.listDomains(activeOnly)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/domains")
    // Note: Hàm `createDomain` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<DomainEntity>> createDomain(@RequestBody DomainRequest request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE DOMAIN SUCCESS", catalogService.createDomain(request)));
    }

    // Note: Annotation này khai báo API cập nhật một phần dữ liệu bằng HTTP PATCH.
    @PatchMapping("/domains/{domainId}")
    // Note: Hàm `updateDomain` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<DomainEntity>> updateDomain(@PathVariable Integer domainId, @RequestBody DomainRequest request) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE DOMAIN SUCCESS", catalogService.updateDomain(domainId, request)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/skills")
    @SecurityRequirements
    // Note: Hàm `listSkills` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> listSkills(@RequestParam(defaultValue = "false") Boolean activeOnly) {
        return ResponseEntity.ok(ApiResponse.success("LIST SKILLS SUCCESS", catalogService.listSkills(activeOnly)));
    }

    @GetMapping("/technologies")
    public ResponseEntity<ApiResponse<Object>> listTechnologies(@RequestParam(defaultValue = "false") Boolean activeOnly) {
        return ResponseEntity.ok(ApiResponse.success("LIST TECHNOLOGIES SUCCESS", catalogService.listTechnologies(activeOnly)));
    }

    @PostMapping("/technologies")
    public ResponseEntity<ApiResponse<TechnologyEntity>> createTechnology(@RequestBody TechnologyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE TECHNOLOGY SUCCESS", catalogService.createTechnology(request)));
    }

    @PatchMapping("/technologies/{technologyId}")
    public ResponseEntity<ApiResponse<TechnologyEntity>> updateTechnology(@PathVariable Integer technologyId, @RequestBody TechnologyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE TECHNOLOGY SUCCESS", catalogService.updateTechnology(technologyId, request)));
    }

    @PostMapping("/skills")
    // Note: Hàm `createSkill` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<SkillEntity>> createSkill(@RequestBody SkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE SKILL SUCCESS", catalogService.createSkill(request)));
    }

    // Note: Annotation này khai báo API cập nhật một phần dữ liệu bằng HTTP PATCH.
    @PatchMapping("/skills/{skillId}")
    // Note: Hàm `updateSkill` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<SkillEntity>> updateSkill(@PathVariable Integer skillId, @RequestBody SkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE SKILL SUCCESS", catalogService.updateSkill(skillId, request)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/jobs/{jobId}/domains")
    // Note: Hàm `listJobDomains` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> listJobDomains(@PathVariable Integer jobId) {
        return ResponseEntity.ok(ApiResponse.success("LIST JOB DOMAINS SUCCESS", catalogService.listJobDomains(jobId)));
    }

    // Note: Annotation này khai báo API cập nhật toàn bộ dữ liệu bằng HTTP PUT.
    @PutMapping("/jobs/{jobId}/domains")
    // Note: Hàm `replaceJobDomains` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> replaceJobDomains(@PathVariable Integer jobId, @RequestBody List<Integer> domainIds) {
        return ResponseEntity.ok(ApiResponse.success("REPLACE JOB DOMAINS SUCCESS", catalogService.replaceJobDomains(jobId, domainIds)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/jobs/{jobId}/skills")
    // Note: Hàm `listJobSkills` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> listJobSkills(@PathVariable Integer jobId) {
        return ResponseEntity.ok(ApiResponse.success("LIST JOB SKILLS SUCCESS", catalogService.listJobSkills(jobId)));
    }

    // Note: Annotation này khai báo API cập nhật toàn bộ dữ liệu bằng HTTP PUT.
    @PutMapping("/jobs/{jobId}/skills")
    // Note: Hàm `replaceJobSkills` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> replaceJobSkills(@PathVariable Integer jobId, @RequestBody List<JobSkillAssignmentRequest> assignments) {
        return ResponseEntity.ok(ApiResponse.success("REPLACE JOB SKILLS SUCCESS", catalogService.replaceJobSkills(jobId, assignments)));
    }

    @GetMapping("/jobs/{jobId}/technologies")
    public ResponseEntity<ApiResponse<Object>> listJobTechnologies(@PathVariable Integer jobId) {
        return ResponseEntity.ok(ApiResponse.success("LIST JOB TECHNOLOGIES SUCCESS", catalogService.listJobTechnologies(jobId)));
    }

    @PutMapping("/jobs/{jobId}/technologies")
    public ResponseEntity<ApiResponse<Object>> replaceJobTechnologies(@PathVariable Integer jobId, @RequestBody List<Integer> technologyIds) {
        return ResponseEntity.ok(ApiResponse.success("REPLACE JOB TECHNOLOGIES SUCCESS", catalogService.replaceJobTechnologies(jobId, technologyIds)));
    }
}
