package com.aitasker.be.common.response;

import java.time.Instant;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String requestId;
    private LocalDateTime timestamp;

    // HÀM DÙNG KHI XỬ LÍ API THÀNH CÔNG VÀ TRẢ VỀ RESPONSE CÓ ĐỊNH DẠNG BUILDER
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true).message(message).data(data).timestamp(LocalDateTime.now()).build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false).message(message).timestamp(LocalDateTime.now()).build();
    }
}
