/*
 * NOTE FILE: src/main/java/com/aitasker/be/common/response/ApiResponse.java
 * Đây là file gì: File response chuẩn hóa định dạng dữ liệu API trả về cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.common.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Note: Annotation này giúp Lombok sinh getter, setter và các hàm tiện ích cho dữ liệu.
@Data
// Note: Annotation này giúp Lombok tạo builder để khởi tạo object rõ ràng hơn.
@Builder
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@NoArgsConstructor @AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String requestId;
    private LocalDateTime timestamp;

    // HÀM DÙNG KHI XỬ LÍ API THÀNH CÔNG VÀ TRẢ VỀ RESPONSE CÓ ĐỊNH DẠNG BUILDER
    // Note: Hàm `success` thực hiện chức năng riêng của file, giúp hoàn thiện luồng xử lý back-end.
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true).message(message).data(data).timestamp(LocalDateTime.now()).build();
    }

    // Note: Hàm `error` thực hiện chức năng riêng của file, giúp hoàn thiện luồng xử lý back-end.
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false).message(message).timestamp(LocalDateTime.now()).build();
    }
}
