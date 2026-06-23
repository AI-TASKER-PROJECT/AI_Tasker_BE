/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/auth/GoogleAuthRequest.java
 * Day la file gi: File DTO khai bao du lieu request/response di qua API hoac qua service.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Note: Annotation nay giup Lombok sinh getter, setter va cac ham tien ich cho du lieu.
@Data
public class GoogleAuthRequest {
    // Note: Annotation nay bat buoc field phai co gia tri text hop le.
    @NotBlank
    private String credential;

    private String role;

    private String fullName;

    private String phone;
}
