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
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // Note: Annotation này khai báo API upload file bằng multipart/form-data.
    @PostMapping(value = "/business/license-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // Note: Hàm `uploadBusinessLicense` nhận file giấy phép kinh doanh, gọi service upload Firebase và trả về storage path.
    public ResponseEntity<ApiResponse<String>> uploadBusinessLicense(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("UPLOAD BUSINESS LICENSE SUCCESS", profileService.uploadBusinessLicense(file)));
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
    public ResponseEntity<ApiResponse<Object>> approve(@PathVariable String type, @PathVariable Integer id, @RequestParam String status, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("APPROVE PROFILE SUCCESS", profileService.approveProfile(type, id, status, reason)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/business")
    public ResponseEntity<ApiResponse<Object>> listBusiness() { return ResponseEntity.ok(ApiResponse.success("LIST BUSINESS PROFILE SUCCESS", profileService.allBusinessProfiles())); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/business/{businessId}")
    @SecurityRequirements
    // Note: Hàm `getBusinessById` trả hồ sơ doanh nghiệp theo businessId để expert xem trang cá nhân của business.
    public ResponseEntity<ApiResponse<BusinessProfileEntity>> getBusinessById(@PathVariable Integer businessId) {
        return ResponseEntity.ok(ApiResponse.success("GET BUSINESS PROFILE BY ID SUCCESS", profileService.businessProfileById(businessId)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/business/me")
    // Note: Hàm `myBusiness` trả hồ sơ KYB của chính doanh nghiệp đang đăng nhập để reload trang vẫn thấy dữ liệu mới.
    public ResponseEntity<ApiResponse<BusinessProfileEntity>> myBusiness() {
        return ResponseEntity.ok(ApiResponse.success("GET MY BUSINESS PROFILE SUCCESS", profileService.currentBusinessProfile()));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/business/by-job/{jobId}")
    @SecurityRequirements
    // Note: Hàm `businessByJob` trả hồ sơ doanh nghiệp theo job để chuyên gia xem thông tin bên đăng dự án.
    public ResponseEntity<ApiResponse<BusinessProfileEntity>> businessByJob(@PathVariable Integer jobId) {
        return ResponseEntity.ok(ApiResponse.success("GET BUSINESS PROFILE BY JOB SUCCESS", profileService.businessProfileByJob(jobId)));
    }

    @GetMapping("/expert")
    public ResponseEntity<ApiResponse<Object>> listExpert() { return ResponseEntity.ok(ApiResponse.success("LIST EXPERT PROFILE SUCCESS", profileService.allExpertProfiles())); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/expert/{expertId}")
    // Note: Hàm `getExpertById` trả hồ sơ chuyên gia theo expertId để business xem trang cá nhân của expert.
    public ResponseEntity<ApiResponse<ExpertProfileEntity>> getExpertById(@PathVariable Integer expertId) {
        return ResponseEntity.ok(ApiResponse.success("GET EXPERT PROFILE BY ID SUCCESS", profileService.expertProfileById(expertId)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/expert/me")
    // Note: Hàm `myExpert` trả hồ sơ KYC của chính chuyên gia đang đăng nhập để reload trang vẫn thấy status mới.
    public ResponseEntity<ApiResponse<ExpertProfileEntity>> myExpert() {
        return ResponseEntity.ok(ApiResponse.success("GET MY EXPERT PROFILE SUCCESS", profileService.currentExpertProfile()));
    }

    // Note: Annotation này khai báo API upload file portfolio chuyên gia bằng multipart/form-data.
    @PostMapping(value = "/expert/portfolio-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // Note: Hàm `uploadExpertPortfolio` nhận file portfolio chuyên gia, gọi service upload Firebase và trả về storage path.
    public ResponseEntity<ApiResponse<String>> uploadExpertPortfolio(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("UPLOAD EXPERT PORTFOLIO SUCCESS", profileService.uploadExpertPortfolio(file)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/portfolio")
    // Note: Hàm `upsertPortfolio` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<PortfolioEntity>> upsertPortfolio(@RequestBody PortfolioEntity request) {
        return ResponseEntity.ok(ApiResponse.success("UPSERT PORTFOLIO SUCCESS", profileService.upsertPortfolio(request)));
    }

    // Note: Annotation này khai báo API upload file bằng multipart/form-data.
    @PostMapping(value = "/portfolio/certificate-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // Note: Hàm `uploadExpertCertificate` nhận file chứng chỉ chuyên gia, gọi service upload Firebase và trả về storage path.
    public ResponseEntity<ApiResponse<String>> uploadExpertCertificate(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("UPLOAD EXPERT CERTIFICATE SUCCESS", profileService.uploadExpertCertificate(file)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/portfolio")
    public ResponseEntity<ApiResponse<Object>> listPortfolio() { return ResponseEntity.ok(ApiResponse.success("LIST PORTFOLIO SUCCESS", profileService.allPortfolios())); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/portfolio/me")
    // Note: Hàm `myPortfolio` trả portfolio của chính chuyên gia đang đăng nhập để form không bị trống sau khi reload.
    public ResponseEntity<ApiResponse<PortfolioEntity>> myPortfolio() {
        return ResponseEntity.ok(ApiResponse.success("GET MY PORTFOLIO SUCCESS", profileService.currentPortfolio()));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/files/view-url")
    // Note: Hàm `fileViewUrl` trả signed URL tạm thời để staff/business/expert bấm xem file Firebase thay vì chỉ thấy raw path.
    public ResponseEntity<ApiResponse<String>> fileViewUrl(@RequestParam String path) {
        return ResponseEntity.ok(ApiResponse.success("GET FIREBASE FILE VIEW URL SUCCESS", profileService.createFileViewUrl(path)));
    }
}
