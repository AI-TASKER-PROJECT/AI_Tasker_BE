/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/auth/SendOtpRequest.java
 * Day la file gi: File DTO khai bao du lieu request/response di qua API hoac qua service.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Note: Annotation nay giup Lombok sinh getter, setter va cac ham tien ich cho du lieu.
@Data
public class SendOtpRequest {
    // Note: Annotation nay validate field dung dinh dang email.
    @Email
    // Note: Annotation nay bat buoc field phai co gia tri text hop le.
    @NotBlank
    private String email;
}
