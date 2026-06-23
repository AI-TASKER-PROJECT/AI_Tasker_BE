/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/auth/TaxCheckController.java
 * Day la file gi: File controller nhan request HTTP, goi service phu hop va tra response cho client.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.controller.auth;

import com.aitasker.be.dto.auth.TaxCheckResponse;
import com.aitasker.be.service.auth.TaxCheckService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Note: Annotation nay bien class thanh REST controller de nhan request va tra JSON.
@RestController
// Note: Annotation nay dat prefix duong dan API cho controller hoac method.
@RequestMapping("/api/auth/tax-check")
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
// Note: Annotation nay ghi ro endpoint/class nay khong ap dung security scheme mac dinh tren Swagger.
@SecurityRequirements
public class TaxCheckController {
    private final TaxCheckService taxCheckService;

    // Note: Annotation nay khai bao API doc du lieu bang HTTP GET.
    @GetMapping("/{mst}")
    // Note: Ham `checkTaxCode` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public TaxCheckResponse checkTaxCode(@PathVariable String mst) {
        return taxCheckService.checkTaxCode(mst);
    }
}
