package com.aitasker.be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
public class PayOSConfig {
    @Bean
    public PayOS payOS(PayOSProperties properties) {
        return new PayOS(
                defaultString(properties.getClientId()),
                defaultString(properties.getApiKey()),
                defaultString(properties.getChecksumKey())
        );
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
