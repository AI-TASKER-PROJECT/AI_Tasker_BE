package com.aitasker.be.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
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
