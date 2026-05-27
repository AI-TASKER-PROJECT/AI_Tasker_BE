package com.aitasker.be.common.exception;

// exception gốc cho lỗi nghiệp vụ
public class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }
}