package com.aitasker.be.common.exception;

//  lỗi xác thực/ủy quyền
public class UnauthorizedException extends AppException {
    public UnauthorizedException(String message) {
        super(message);
    }
}