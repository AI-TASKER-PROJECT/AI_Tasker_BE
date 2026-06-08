package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.admin.AccountRequest;
import com.aitasker.be.dto.admin.AccountResponse;
import com.aitasker.be.dto.admin.StaffResponse;
import com.aitasker.be.entity.ReviewEntity;
import com.aitasker.be.entity.StaffEntity;
import com.aitasker.be.entity.SystemWalletEntity;
import com.aitasker.be.entity.SystemSettingEntity;
import com.aitasker.be.service.core.AdminService;
import com.aitasker.be.service.core.SystemWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final SystemWalletService systemWalletService;

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
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(@RequestBody StaffEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE STAFF SUCCESS", adminService.createStaff(request)));
    }

    @PatchMapping("/staffs/{staffId}")
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaff(@PathVariable Integer staffId, @RequestBody StaffEntity request) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE STAFF SUCCESS", adminService.updateStaff(staffId, request)));
    }

    @GetMapping("/analytics/overview")
    public ResponseEntity<ApiResponse<Object>> analyticsOverview() {
        return ResponseEntity.ok(ApiResponse.success("ANALYTICS OVERVIEW SUCCESS", adminService.analyticsOverview()));
    }

    @GetMapping("/wallet")
    public ResponseEntity<ApiResponse<SystemWalletEntity>> systemWallet() {
        return ResponseEntity.ok(ApiResponse.success("SYSTEM WALLET SUCCESS", systemWalletService.getWalletForAdmin()));
    }

    @PostMapping("/wallet/sync")
    public ResponseEntity<ApiResponse<SystemWalletEntity>> syncSystemWallet() {
        return ResponseEntity.ok(ApiResponse.success("SYNC SYSTEM WALLET SUCCESS", systemWalletService.getWalletForAdmin()));
    }

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<Object>> listAccounts() {
        return ResponseEntity.ok(ApiResponse.success("LIST ACCOUNTS SUCCESS", adminService.listAccounts()));
    }

    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@RequestBody AccountRequest request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE ACCOUNT SUCCESS", adminService.createAccount(request)));
    }

    @PatchMapping("/accounts/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(@PathVariable Integer accountId, @RequestBody AccountRequest request) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE ACCOUNT SUCCESS", adminService.updateAccount(accountId, request)));
    }

    @PatchMapping("/accounts/{accountId}/active")
    public ResponseEntity<ApiResponse<AccountResponse>> setAccountActive(@PathVariable Integer accountId, @RequestParam boolean active) {
        return ResponseEntity.ok(ApiResponse.success("SET ACCOUNT ACTIVE SUCCESS", adminService.setAccountActive(accountId, active)));
    }

    @PatchMapping("/accounts/{accountId}/status")
    public ResponseEntity<ApiResponse<AccountResponse>> setAccountStatus(@PathVariable Integer accountId, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("SET ACCOUNT STATUS SUCCESS", adminService.setAccountStatus(accountId, status)));
    }

    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> deactivateAccount(@PathVariable Integer accountId) {
        return ResponseEntity.ok(ApiResponse.success("DEACTIVATE ACCOUNT SUCCESS", adminService.deactivateAccount(accountId)));
    }
}
