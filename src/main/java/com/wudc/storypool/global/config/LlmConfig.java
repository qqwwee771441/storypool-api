package com.wudc.storypool.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "llm.server")
@Getter
@Setter
public class LlmConfig {
    
    private String baseUrl;
    private Fairytale fairytale = new Fairytale();
    private int timeout;
    private String serviceToken;
    
    @Getter
    @Setter
    public static class Fairytale {
        private String generateEndpoint;
    }
    
    public String getFairytaleGenerateUrl() {
        return baseUrl + fairytale.generateEndpoint;
    }
}