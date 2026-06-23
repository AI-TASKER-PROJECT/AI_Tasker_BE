/*
 * NOTE FILE: src/main/java/com/aitasker/be/common/exception/BadGatewayException.java
 * Day la file gi: File exception/handler chuan hoa loi dung chung de API tra response nhat quan.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.common.exception;

public class BadGatewayException extends AppException {
    // Note: Ham `BadGatewayException` xu ly hoac chuan hoa loi de API tra response nhat quan.
    public BadGatewayException(String message) {
        super(message);
    }
}
