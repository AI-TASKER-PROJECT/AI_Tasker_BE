package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.entity.SystemWalletEntity;
import com.aitasker.be.service.core.SystemWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final SystemWalletService systemWalletService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SystemWalletEntity>> currentWallet() {
        return ResponseEntity.ok(ApiResponse.success("CURRENT WALLET SUCCESS", systemWalletService.getCurrentWallet()));
    }
}
