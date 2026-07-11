/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/AdminController.java
 * Đây là file gì: File controller nhận request HTTP, gọi service phù hợp và trả response cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.admin.*;
import com.aitasker.be.dto.payment.WalletTransactionHistoryResponse;
import com.aitasker.be.entity.ReviewEntity;
import com.aitasker.be.entity.StaffEntity;
import com.aitasker.be.entity.SystemWalletEntity;
import com.aitasker.be.entity.SystemSettingEntity;
import com.aitasker.be.service.core.AdminDisputeDashboardService;
import com.aitasker.be.service.core.AdminService;
import com.aitasker.be.service.core.PaymentWalletService;
import com.aitasker.be.service.core.SystemWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

// Note: Annotation này biến class thành REST controller để nhận request và trả JSON.
@RestController
// Note: Annotation này đặt prefix đường dẫn API cho controller hoặc method.
@RequestMapping("/api/v1/admin")
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final AdminDisputeDashboardService adminDisputeDashboardService;
    private final SystemWalletService systemWalletService;
    private final PaymentWalletService paymentWalletService;

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/reviews")
    // Note: Hàm `createReview` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<ReviewEntity>> createReview(@RequestBody ReviewEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE REVIEW SUCCESS", adminService.createReview(request)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/reviews/contracts/{contractId}")
    // Note: Hàm `listReviewsByContract` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> listReviewsByContract(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("LIST REVIEWS SUCCESS", adminService.listReviewsByContract(contractId)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/settings")
    // Note: Hàm `listSettings` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> listSettings() {
        return ResponseEntity.ok(ApiResponse.success("LIST SYSTEM SETTINGS SUCCESS", adminService.listSettings()));
    }

    // Note: Annotation này khai báo API cập nhật một phần dữ liệu bằng HTTP PATCH.
    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/audit-logs")
    // Note: Hàm `listAuditLogs` trả audit log cho admin, có thể lọc theo nhóm role nội bộ hoặc bên ngoài.
    public ResponseEntity<ApiResponse<Object>> listAuditLogs(
            // Note: Annotation này lấy query parameter đưa vào tham số hàm.
            @RequestParam(required = false) String actorGroup) {
        return ResponseEntity.ok(ApiResponse.success("LIST AUDIT LOGS SUCCESS", adminService.listAuditLogs(actorGroup)));
    }

    @PatchMapping("/settings/{key}")
    public ResponseEntity<ApiResponse<SystemSettingEntity>> updateSetting(
            // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
            @PathVariable String key,
            // Note: Annotation này lấy query parameter đưa vào tham số hàm.
            @RequestParam(required = false) String value,
            // Note: Annotation này lấy query parameter đưa vào tham số hàm.
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE SYSTEM SETTING SUCCESS", adminService.updateSetting(key, value, isActive)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/staffs")
    // Note: Hàm `listStaffs` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> listStaffs() {
        return ResponseEntity.ok(ApiResponse.success("LIST STAFFS SUCCESS", adminService.listStaffs()));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/staffs")
    // Note: Hàm `createStaff` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(@RequestBody StaffRequest request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE STAFF SUCCESS", adminService.createStaff(request)));
    }

    // Note: Annotation này khai báo API cập nhật một phần dữ liệu bằng HTTP PATCH.
    @PatchMapping("/staffs/{staffId}")
    // Note: Hàm `updateStaff` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaff(@PathVariable Integer staffId, @RequestBody StaffRequest request) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE STAFF SUCCESS", adminService.updateStaff(staffId, request)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/analytics/overview")
    // Note: Hàm `analyticsOverview` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> analyticsOverview() {
        return ResponseEntity.ok(ApiResponse.success("ANALYTICS OVERVIEW SUCCESS", adminService.analyticsOverview()));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/wallet")
    // Note: Hàm `systemWallet` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<SystemWalletEntity>> systemWallet() {
        return ResponseEntity.ok(ApiResponse.success("SYSTEM WALLET SUCCESS", systemWalletService.getWalletForAdmin()));
    }

    @GetMapping("/wallet/transactions")
    public ResponseEntity<ApiResponse<List<WalletTransactionHistoryResponse>>> platformWalletTransactions() {
        return ResponseEntity.ok(ApiResponse.success("PLATFORM WALLET TRANSACTIONS SUCCESS",
                paymentWalletService.listPlatformWalletTransactions()));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/wallet/sync")
    // Note: Hàm `syncSystemWallet` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<SystemWalletEntity>> syncSystemWallet() {
        return ResponseEntity.ok(ApiResponse.success("SYNC SYSTEM WALLET SUCCESS", systemWalletService.getWalletForAdmin()));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/accounts")
    // Note: Hàm `listAccounts` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> listAccounts() {
        return ResponseEntity.ok(ApiResponse.success("LIST ACCOUNTS SUCCESS", adminService.listAccounts()));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/accounts")
    // Note: Hàm `createAccount` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@RequestBody AccountRequest request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE ACCOUNT SUCCESS", adminService.createAccount(request)));
    }

    // Note: Annotation này khai báo API cập nhật một phần dữ liệu bằng HTTP PATCH.
    @PatchMapping("/accounts/{accountId}")
    // Note: Hàm `updateAccount` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(@PathVariable Integer accountId, @RequestBody AccountRequest request) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE ACCOUNT SUCCESS", adminService.updateAccount(accountId, request)));
    }

    @GetMapping("/disputes")
    public ResponseEntity<ApiResponse<AdminDisputeListResponse>> listDisputes(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer assignedStaffId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String q) {
        AdminDisputeFilter filter = new AdminDisputeFilter();
        filter.setPage(page);
        filter.setSize(size);
        filter.setStatus(status);
        filter.setAssignedStaffId(assignedStaffId);
        filter.setFrom(from);
        filter.setTo(to);
        filter.setQ(q);
        return ResponseEntity.ok(ApiResponse.success("ADMIN DISPUTES LIST SUCCESS",
                adminDisputeDashboardService.listDisputes(filter)));
    }

    @GetMapping("/disputes/{disputeId}")
    public ResponseEntity<ApiResponse<AdminDisputeDetail>> getDisputeDetail(@PathVariable Integer disputeId) {
        return ResponseEntity.ok(ApiResponse.success("ADMIN DISPUTE DETAIL SUCCESS",
                adminDisputeDashboardService.getDisputeDetail(disputeId)));
    }

    // Note: Annotation này khai báo API cập nhật một phần dữ liệu bằng HTTP PATCH.
    @PatchMapping("/accounts/{accountId}/active")
    // Note: Hàm `setAccountActive` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<AccountResponse>> setAccountActive(@PathVariable Integer accountId, @RequestParam boolean active) {
        return ResponseEntity.ok(ApiResponse.success("SET ACCOUNT ACTIVE SUCCESS", adminService.setAccountActive(accountId, active)));
    }

    // Note: Annotation này khai báo API cập nhật một phần dữ liệu bằng HTTP PATCH.
    @PatchMapping("/accounts/{accountId}/status")
    // Note: Hàm `setAccountStatus` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<AccountResponse>> setAccountStatus(@PathVariable Integer accountId, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("SET ACCOUNT STATUS SUCCESS", adminService.setAccountStatus(accountId, status)));
    }

    // Note: Annotation này khai báo API xóa dữ liệu bằng HTTP DELETE.
    @DeleteMapping("/accounts/{accountId}")
    // Note: Hàm `deactivateAccount` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<AccountResponse>> deactivateAccount(@PathVariable Integer accountId) {
        return ResponseEntity.ok(ApiResponse.success("DEACTIVATE ACCOUNT SUCCESS", adminService.deactivateAccount(accountId)));
    }
}
