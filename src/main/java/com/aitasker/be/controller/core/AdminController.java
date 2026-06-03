package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.entity.ReviewEntity;
import com.aitasker.be.entity.StaffEntity;
import com.aitasker.be.entity.SystemSettingEntity;
import com.aitasker.be.service.core.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewEntity>> createReview(@RequestBody ReviewEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE REVIEW SUCCESS", adminService.createReview(request)));
    }

    @GetMapping("/reviews/contracts/{contractId}")
    public ResponseEntity<ApiResponse<Object>> listReviewsByContract(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("LIST REVIEWS SUCCESS", adminService.listReviewsByContract(contractId)));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<Object>> listSettings() {
        return ResponseEntity.ok(ApiResponse.success("LIST SYSTEM SETTINGS SUCCESS", adminService.listSettings()));
    }

    @PatchMapping("/settings/{key}")
    public ResponseEntity<ApiResponse<SystemSettingEntity>> updateSetting(
            @PathVariable String key,
            @RequestParam(required = false) String value,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE SYSTEM SETTING SUCCESS", adminService.updateSetting(key, value, isActive)));
    }

    @GetMapping("/staffs")
    public ResponseEntity<ApiResponse<Object>> listStaffs() {
        return ResponseEntity.ok(ApiResponse.success("LIST STAFFS SUCCESS", adminService.listStaffs()));
    }

    @PostMapping("/staffs")
    public ResponseEntity<ApiResponse<StaffEntity>> createStaff(@RequestBody StaffEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE STAFF SUCCESS", adminService.createStaff(request)));
    }

    @GetMapping("/analytics/overview")
    public ResponseEntity<ApiResponse<Object>> analyticsOverview() {
        return ResponseEntity.ok(ApiResponse.success("ANALYTICS OVERVIEW SUCCESS", adminService.analyticsOverview()));
    }
}
