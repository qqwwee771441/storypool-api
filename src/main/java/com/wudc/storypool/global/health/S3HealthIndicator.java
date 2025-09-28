package com.wudc.storypool.global.health;

import com.wudc.storypool.global.config.S3Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3HealthIndicator implements HealthIndicator {

    private final S3Client s3Client;
    private final S3Config s3Config;

    @Override
    public Health health() {
        try {
            // S3 버킷에 접근 가능한지 확인
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .build();
            
            s3Client.headBucket(headBucketRequest);
            
            return Health.up()
                .withDetail("s3", "Available")
                .withDetail("bucket", s3Config.getBucketName())
                .withDetail("region", s3Config.getRegion())
                .build();
                
        } catch (Exception e) {
            log.warn("S3 health check failed: {}", e.getMessage());
            return Health.down()
                .withDetail("s3", "Connection failed")
                .withDetail("bucket", s3Config.getBucketName())
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}