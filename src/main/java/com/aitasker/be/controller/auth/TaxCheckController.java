package com.aitasker.be.controller.auth;

import com.aitasker.be.dto.auth.TaxCheckResponse;
import com.aitasker.be.service.auth.TaxCheckService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/tax-check")
@RequiredArgsConstructor
@SecurityRequirements
public class TaxCheckController {
    private final TaxCheckService taxCheckService;

    @GetMapping("/{mst}")
    public TaxCheckResponse checkTaxCode(@PathVariable String mst) {
        return taxCheckService.checkTaxCode(mst);
    }
}
