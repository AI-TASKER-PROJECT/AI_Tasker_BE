package com.aitasker.be.test_demo;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SecurityRequirements
public class HealthController {
    @GetMapping("/api/health")
    public String health() {
        return "Health Api Oke";
    }
}
