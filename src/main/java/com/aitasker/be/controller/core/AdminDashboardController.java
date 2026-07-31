package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.admin.dashboard.*;
import com.aitasker.be.service.core.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> summary() {
        return ResponseEntity.ok(ApiResponse.success("ADMIN DASHBOARD SUMMARY SUCCESS",
                adminDashboardService.summary()));
    }

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<DashboardSeriesResponse>> revenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "month") String groupBy) {
        return ResponseEntity.ok(ApiResponse.success("ADMIN DASHBOARD REVENUE SUCCESS",
                adminDashboardService.revenue(from, to, groupBy)));
    }

    @GetMapping("/contracts")
    public ResponseEntity<ApiResponse<DashboardContractsResponse>> contracts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "month") String groupBy) {
        return ResponseEntity.ok(ApiResponse.success("ADMIN DASHBOARD CONTRACTS SUCCESS",
                adminDashboardService.contracts(from, to, groupBy)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<DashboardUsersResponse>> users(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "month") String groupBy) {
        return ResponseEntity.ok(ApiResponse.success("ADMIN DASHBOARD USERS SUCCESS",
                adminDashboardService.users(from, to, groupBy)));
    }

    @GetMapping("/jobs-proposals")
    public ResponseEntity<ApiResponse<DashboardJobsProposalsResponse>> jobsProposals(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "month") String groupBy) {
        return ResponseEntity.ok(ApiResponse.success("ADMIN DASHBOARD JOBS PROPOSALS SUCCESS",
                adminDashboardService.jobsProposals(from, to, groupBy)));
    }

    @GetMapping("/disputes")
    public ResponseEntity<ApiResponse<DashboardDisputesResponse>> disputes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "month") String groupBy) {
        return ResponseEntity.ok(ApiResponse.success("ADMIN DASHBOARD DISPUTES SUCCESS",
                adminDashboardService.disputes(from, to, groupBy)));
    }

    @GetMapping("/membership")
    public ResponseEntity<ApiResponse<DashboardMembershipResponse>> membership(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "month") String groupBy) {
        return ResponseEntity.ok(ApiResponse.success("ADMIN DASHBOARD MEMBERSHIP SUCCESS",
                adminDashboardService.membership(from, to, groupBy)));
    }

    @GetMapping("/finance-breakdown")
    public ResponseEntity<ApiResponse<DashboardFinanceBreakdownResponse>> financeBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success("ADMIN DASHBOARD FINANCE BREAKDOWN SUCCESS",
                adminDashboardService.financeBreakdown(from, to)));
    }
}
