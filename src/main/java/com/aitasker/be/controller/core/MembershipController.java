/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/MembershipController.java
 * Day la file gi: File controller nhan request HTTP, goi service phu hop va tra response cho client.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.payment.PaymentActionResponse;
import com.aitasker.be.entity.MembershipPackageEntity;
import com.aitasker.be.entity.MembershipPurchaseEntity;
import com.aitasker.be.service.core.PaymentWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Note: Annotation nay bien class thanh REST controller de nhan request va tra JSON.
@RestController
// Note: Annotation nay dat prefix duong dan API cho controller hoac method.
@RequestMapping("/api/membership")
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class MembershipController {
    private final PaymentWalletService paymentWalletService;

    // Note: Annotation nay khai bao API doc du lieu bang HTTP GET.
    @GetMapping("/packages")
    // Note: Ham `listPackages` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<List<MembershipPackageEntity>>> listPackages() {
        return ResponseEntity.ok(ApiResponse.success("LIST MEMBERSHIP PACKAGES SUCCESS",
                paymentWalletService.listPackagesForCurrentRole()));
    }

    // Note: Annotation nay khai bao API tao moi hoac gui du lieu bang HTTP POST.
    @PostMapping("/packages/{packageId}/purchase")
    // Note: Ham `purchasePackage` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<PaymentActionResponse<MembershipPurchaseEntity>>> purchasePackage(
            // Note: Annotation nay doc bien tren URL path vao tham so ham.
            @PathVariable Long packageId
    ) {
        return ResponseEntity.ok(ApiResponse.success("PURCHASE MEMBERSHIP PACKAGE SUCCESS",
                paymentWalletService.purchaseMembership(packageId)));
    }
}
