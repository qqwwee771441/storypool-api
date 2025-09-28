package com.wudc.storypool.global.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class LlmServerHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;

    @Override
    public Health health() {
        try {
            String llmHealthUrl = "http://localhost:8080/health"; // LLM 서버의 헬스체크 URL
            
            ResponseEntity<String> response = restTemplate.getForEntity(llmHealthUrl, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return Health.up()
                    .withDetail("llm-server", "Available")
                    .withDetail("status", response.getStatusCode())
                    .withDetail("url", llmHealthUrl)
                    .build();
            } else {
                return Health.down()
                    .withDetail("llm-server", "Unavailable")
                    .withDetail("status", response.getStatusCode())
                    .withDetail("url", llmHealthUrl)
                    .build();
            }
        } catch (Exception e) {
            log.warn("LLM server health check failed: {}", e.getMessage());
            return Health.down()
                .withDetail("llm-server", "Connection failed")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}