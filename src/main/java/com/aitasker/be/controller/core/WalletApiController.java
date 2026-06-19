package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.entity.SystemWalletEntity;
import com.aitasker.be.entity.WalletTransactionEntity;
import com.aitasker.be.service.core.PaymentWalletService;
import com.aitasker.be.service.core.SystemWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletApiController {
    private final SystemWalletService systemWalletService;
    private final PaymentWalletService paymentWalletService;

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<SystemWalletEntity>> currentWallet() {
        return ResponseEntity.ok(ApiResponse.success("CURRENT WALLET SUCCESS", systemWalletService.getCurrentWallet()));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<WalletTransactionEntity>>> walletTransactions() {
        return ResponseEntity.ok(ApiResponse.success("WALLET TRANSACTIONS SUCCESS",
                paymentWalletService.listCurrentWalletTransactions()));
    }
}
