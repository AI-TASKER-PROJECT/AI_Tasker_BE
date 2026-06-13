package com.aitasker.be.controller.core;

import com.aitasker.be.dto.sow.GenerateSowRequest;
import com.aitasker.be.dto.sow.GenerateSowResponse;
import com.aitasker.be.service.core.AiSowGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SoW Generation", description = "AI generate Statement of Work for jobs")
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class SowGenerationController {
    private final AiSowGenerationService aiSowGenerationService;

    @Operation(summary = "Generate SoW", description = "Generate structured SoW, milestones and milestone budgets from raw job requirements.")
    @PostMapping("/generate-sow")
    public GenerateSowResponse generateSow(@Valid @RequestBody GenerateSowRequest request) {
        return aiSowGenerationService.generateSow(request);
    }
}
