package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.entity.ExpertProfileEntity;
import com.aitasker.be.entity.PortfolioEntity;
import com.aitasker.be.service.core.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PostMapping("/business")
    public ResponseEntity<ApiResponse<BusinessProfileEntity>> upsertBusiness(@RequestBody BusinessProfileEntity request) {
        return ResponseEntity.ok(ApiResponse.success("UPSERT BUSINESS PROFILE SUCCESS", profileService.upsertBusiness(request)));
    }

    @PostMapping("/expert")
    public ResponseEntity<ApiResponse<ExpertProfileEntity>> upsertExpert(@RequestBody ExpertProfileEntity request) {
        return ResponseEntity.ok(ApiResponse.success("UPSERT EXPERT PROFILE SUCCESS", profileService.upsertExpert(request)));
    }

    @PostMapping("/approve/{type}/{id}")
    public ResponseEntity<ApiResponse<Object>> approve(@PathVariable String type, @PathVariable Integer id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("APPROVE PROFILE SUCCESS", profileService.approveProfile(type, id, status)));
    }

    @GetMapping("/business")
    public ResponseEntity<ApiResponse<Object>> listBusiness() { return ResponseEntity.ok(ApiResponse.success("LIST BUSINESS PROFILE SUCCESS", profileService.allBusinessProfiles())); }

    @GetMapping("/expert")
    public ResponseEntity<ApiResponse<Object>> listExpert() { return ResponseEntity.ok(ApiResponse.success("LIST EXPERT PROFILE SUCCESS", profileService.allExpertProfiles())); }

    @PostMapping("/portfolio")
    public ResponseEntity<ApiResponse<PortfolioEntity>> upsertPortfolio(@RequestBody PortfolioEntity request) {
        return ResponseEntity.ok(ApiResponse.success("UPSERT PORTFOLIO SUCCESS", profileService.upsertPortfolio(request)));
    }

    @GetMapping("/portfolio")
    public ResponseEntity<ApiResponse<Object>> listPortfolio() { return ResponseEntity.ok(ApiResponse.success("LIST PORTFOLIO SUCCESS", profileService.allPortfolios())); }
}
