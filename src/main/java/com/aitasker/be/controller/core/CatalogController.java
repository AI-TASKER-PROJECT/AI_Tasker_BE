package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.catalog.DomainRequest;
import com.aitasker.be.dto.catalog.JobSkillAssignmentRequest;
import com.aitasker.be.dto.catalog.SkillRequest;
import com.aitasker.be.entity.*;
import com.aitasker.be.service.core.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalogService;

    @GetMapping("/domains")
    public ResponseEntity<ApiResponse<Object>> listDomains(@RequestParam(defaultValue = "false") Boolean activeOnly) {
        return ResponseEntity.ok(ApiResponse.success("LIST DOMAINS SUCCESS", catalogService.listDomains(activeOnly)));
    }

    @PostMapping("/domains")
    public ResponseEntity<ApiResponse<DomainEntity>> createDomain(@RequestBody DomainRequest request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE DOMAIN SUCCESS", catalogService.createDomain(request)));
    }

    @PatchMapping("/domains/{domainId}")
    public ResponseEntity<ApiResponse<DomainEntity>> updateDomain(@PathVariable Integer domainId, @RequestBody DomainRequest request) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE DOMAIN SUCCESS", catalogService.updateDomain(domainId, request)));
    }

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<Object>> listSkills(@RequestParam(defaultValue = "false") Boolean activeOnly) {
        return ResponseEntity.ok(ApiResponse.success("LIST SKILLS SUCCESS", catalogService.listSkills(activeOnly)));
    }

    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<SkillEntity>> createSkill(@RequestBody SkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE SKILL SUCCESS", catalogService.createSkill(request)));
    }

    @PatchMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<SkillEntity>> updateSkill(@PathVariable Integer skillId, @RequestBody SkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE SKILL SUCCESS", catalogService.updateSkill(skillId, request)));
    }

    @GetMapping("/jobs/{jobId}/domains")
    public ResponseEntity<ApiResponse<Object>> listJobDomains(@PathVariable Integer jobId) {
        return ResponseEntity.ok(ApiResponse.success("LIST JOB DOMAINS SUCCESS", catalogService.listJobDomains(jobId)));
    }

    @PutMapping("/jobs/{jobId}/domains")
    public ResponseEntity<ApiResponse<Object>> replaceJobDomains(@PathVariable Integer jobId, @RequestBody List<Integer> domainIds) {
        return ResponseEntity.ok(ApiResponse.success("REPLACE JOB DOMAINS SUCCESS", catalogService.replaceJobDomains(jobId, domainIds)));
    }

    @GetMapping("/jobs/{jobId}/skills")
    public ResponseEntity<ApiResponse<Object>> listJobSkills(@PathVariable Integer jobId) {
        return ResponseEntity.ok(ApiResponse.success("LIST JOB SKILLS SUCCESS", catalogService.listJobSkills(jobId)));
    }

    @PutMapping("/jobs/{jobId}/skills")
    public ResponseEntity<ApiResponse<Object>> replaceJobSkills(@PathVariable Integer jobId, @RequestBody List<JobSkillAssignmentRequest> assignments) {
        return ResponseEntity.ok(ApiResponse.success("REPLACE JOB SKILLS SUCCESS", catalogService.replaceJobSkills(jobId, assignments)));
    }
}
