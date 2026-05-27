package com.aitasker.be.common.exception;

// lỗi trùng dữ liệu (vd email đã tồn tại)
public class ResourceConflictException extends AppException {
    public ResourceConflictException(String message) {
        super(message);
    }
}