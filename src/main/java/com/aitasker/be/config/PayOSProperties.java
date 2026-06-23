/*
 * NOTE FILE: src/main/java/com/aitasker/be/config/PayOSProperties.java
 * Day la file gi: File cau hinh bean/thu vien, giup Spring Boot khoi tao hanh vi dung chung.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// Note: Annotation nay giup Lombok sinh getter, setter va cac ham tien ich cho du lieu.
@Data
// Note: Annotation nay dang ky class thanh bean dung chung trong Spring context.
@Component
// Note: Annotation nay bind cau hinh tu application properties vao object Java.
@ConfigurationProperties(prefix = "payos")
public class PayOSProperties {
    private String clientId;
    private String apiKey;
    private String checksumKey;
    private String returnUrl;
    private String cancelUrl;
}
