/*
 * NOTE FILE: src/main/java/com/aitasker/be/common/exception/NotFoundException.java
 * Đây là file gì: File exception chuẩn hóa lỗi nghiệp vụ và cách API trả lỗi cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.common.exception;

public class NotFoundException extends AppException {
    // Note: Hàm `NotFoundException` xử lý hoặc chuẩn hóa lỗi để API trả response nhất quán.
    public NotFoundException(String message) {
        super(message);
    }
}
