/*
 * NOTE FILE: src/main/java/com/aitasker/be/common/exception/UnauthorizedException.java
 * Đây là file gì: File exception chuẩn hóa lỗi nghiệp vụ và cách API trả lỗi cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.common.exception;

// LOI XAC THUC HOAC UY QUYEN.
public class UnauthorizedException extends AppException {
    // Note: Hàm `UnauthorizedException` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public UnauthorizedException(String message) {
        super(message);
    }
}
