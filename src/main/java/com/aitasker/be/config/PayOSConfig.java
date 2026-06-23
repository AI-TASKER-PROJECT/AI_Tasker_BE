/*
 * NOTE FILE: src/main/java/com/aitasker/be/config/PayOSConfig.java
 * Day la file gi: File cau hinh bean/thu vien, giup Spring Boot khoi tao hanh vi dung chung.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

// Note: Annotation nay danh dau class cau hinh bean cho Spring.
@Configuration
public class PayOSConfig {
    // Note: Annotation nay khai bao object duoc Spring quan ly va inject khi can.
    @Bean
    // Note: Ham `payOS` khai bao bean hoac cau hinh dung chung cho ung dung.
    public PayOS payOS(PayOSProperties properties) {
        return new PayOS(
                defaultString(properties.getClientId()),
                defaultString(properties.getApiKey()),
                defaultString(properties.getChecksumKey())
        );
    }

    // Note: Ham `defaultString` khai bao bean hoac cau hinh dung chung cho ung dung.
    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
