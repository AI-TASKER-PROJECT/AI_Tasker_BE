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

@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
public class MembershipController {
    private final PaymentWalletService paymentWalletService;

    @GetMapping("/packages")
    public ResponseEntity<ApiResponse<List<MembershipPackageEntity>>> listPackages() {
        return ResponseEntity.ok(ApiResponse.success("LIST MEMBERSHIP PACKAGES SUCCESS",
                paymentWalletService.listPackagesForCurrentRole()));
    }

    @PostMapping("/packages/{packageId}/purchase")
    public ResponseEntity<ApiResponse<PaymentActionResponse<MembershipPurchaseEntity>>> purchasePackage(
            @PathVariable Long packageId
    ) {
        return ResponseEntity.ok(ApiResponse.success("PURCHASE MEMBERSHIP PACKAGE SUCCESS",
                paymentWalletService.purchaseMembership(packageId)));
    }
}
