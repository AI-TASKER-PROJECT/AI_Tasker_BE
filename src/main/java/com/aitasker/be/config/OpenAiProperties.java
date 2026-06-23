/*
 * NOTE FILE: src/main/java/com/aitasker/be/config/OpenAiProperties.java
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
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {
    private String apiKey;
    private String model = "gpt-4o-mini";
    private String embeddingModel = "text-embedding-3-small";
    private String responsesUrl = "https://api.openai.com/v1/responses";
    private String embeddingsUrl = "https://api.openai.com/v1/embeddings";
    private String chatCompletionsUrl = "https://api.openai.com/v1/chat/completions";
    private Integer maxOutputTokens = 700;
}
