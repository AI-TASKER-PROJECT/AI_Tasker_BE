/*
 * NOTE FILE: src/main/java/com/aitasker/be/common/exception/AppException.java
 * Đây là file gì: File exception chuẩn hóa lỗi nghiệp vụ và cách API trả lỗi cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.common.exception;

// EXCEPTION GOC CHO LOI NGHIEP VU.
public class AppException extends RuntimeException {
    // Note: Hàm `AppException` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public AppException(String message) {
        super(message);
    }
}
