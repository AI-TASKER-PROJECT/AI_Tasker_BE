/*
 * NOTE FILE: src/main/java/com/aitasker/be/config/RestTemplateConfig.java
 * Day la file gi: File cau hinh bean/thu vien, giup Spring Boot khoi tao hanh vi dung chung.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// Note: Annotation nay danh dau class cau hinh bean cho Spring.
@Configuration
public class RestTemplateConfig {

    // Note: Annotation nay khai bao object duoc Spring quan ly va inject khi can.
    @Bean
    // Note: Ham `restTemplate` khai bao bean hoac cau hinh dung chung cho ung dung.
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
