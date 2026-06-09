/*
 * NOTE FILE: src/main/java/com/aitasker/be/common/exception/ResourceConflictException.java
 * Đây là file gì: File exception chuẩn hóa lỗi nghiệp vụ và cách API trả lỗi cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.common.exception;

// LOI TRUNG DU LIEU (VI DU EMAIL DA TON TAI).
public class ResourceConflictException extends AppException {
    // Note: Hàm `ResourceConflictException` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public ResourceConflictException(String message) {
        super(message);
    }
}
