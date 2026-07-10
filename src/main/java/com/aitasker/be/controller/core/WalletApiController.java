/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/WalletApiController.java
 * Day la file gi: File controller nhan request HTTP, goi service phu hop va tra response cho client.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.payment.WalletTransactionHistoryResponse;
import com.aitasker.be.entity.SystemWalletEntity;
import com.aitasker.be.service.core.PaymentWalletService;
import com.aitasker.be.service.core.SystemWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Note: Annotation nay bien class thanh REST controller de nhan request va tra JSON.
@RestController
// Note: Annotation nay dat prefix duong dan API cho controller hoac method.
@RequestMapping("/api/wallet")
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class WalletApiController {
    private final SystemWalletService systemWalletService;
    private final PaymentWalletService paymentWalletService;

    // Note: Annotation nay khai bao API doc du lieu bang HTTP GET.
    @GetMapping("/current")
    // Note: Ham `currentWallet` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<SystemWalletEntity>> currentWallet() {
        return ResponseEntity.ok(ApiResponse.success("CURRENT WALLET SUCCESS", systemWalletService.getCurrentWallet()));
    }

    // Note: Annotation nay khai bao API doc du lieu bang HTTP GET.
    @GetMapping("/transactions")
    // Note: Ham `walletTransactions` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<List<WalletTransactionHistoryResponse>>> walletTransactions() {
        return ResponseEntity.ok(ApiResponse.success("WALLET TRANSACTIONS SUCCESS",
                paymentWalletService.listCurrentWalletTransactions()));
    }
}
