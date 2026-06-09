/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/ProfileController.java
 * Đây là file gì: File controller nhận request HTTP, gọi service phù hợp và trả response cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.entity.ExpertProfileEntity;
import com.aitasker.be.entity.PortfolioEntity;
import com.aitasker.be.service.core.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Note: Annotation này biến class thành REST controller để nhận request và trả JSON.
@RestController
// Note: Annotation này đặt prefix đường dẫn API cho controller hoặc method.
@RequestMapping("/api/v1/profiles")
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/business")
    // Note: Hàm `upsertBusiness` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<BusinessProfileEntity>> upsertBusiness(@RequestBody BusinessProfileEntity request) {
        return ResponseEntity.ok(ApiResponse.success("UPSERT BUSINESS PROFILE SUCCESS", profileService.upsertBusiness(request)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/expert")
    // Note: Hàm `upsertExpert` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<ExpertProfileEntity>> upsertExpert(@RequestBody ExpertProfileEntity request) {
        return ResponseEntity.ok(ApiResponse.success("UPSERT EXPERT PROFILE SUCCESS", profileService.upsertExpert(request)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/approve/{type}/{id}")
    // Note: Hàm `approve` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> approve(@PathVariable String type, @PathVariable Integer id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("APPROVE PROFILE SUCCESS", profileService.approveProfile(type, id, status)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/business")
    public ResponseEntity<ApiResponse<Object>> listBusiness() { return ResponseEntity.ok(ApiResponse.success("LIST BUSINESS PROFILE SUCCESS", profileService.allBusinessProfiles())); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/expert")
    public ResponseEntity<ApiResponse<Object>> listExpert() { return ResponseEntity.ok(ApiResponse.success("LIST EXPERT PROFILE SUCCESS", profileService.allExpertProfiles())); }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/portfolio")
    // Note: Hàm `upsertPortfolio` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<PortfolioEntity>> upsertPortfolio(@RequestBody PortfolioEntity request) {
        return ResponseEntity.ok(ApiResponse.success("UPSERT PORTFOLIO SUCCESS", profileService.upsertPortfolio(request)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/portfolio")
    public ResponseEntity<ApiResponse<Object>> listPortfolio() { return ResponseEntity.ok(ApiResponse.success("LIST PORTFOLIO SUCCESS", profileService.allPortfolios())); }
}
