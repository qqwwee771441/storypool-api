package com.wudc.storypool.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenStorageService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.jwt.tokenExpire:30}")
    private Long accessTokenExpireMinutes;

    @Value("${spring.jwt.refreshTokenExpire:20160}")
    private Long refreshTokenExpireMinutes;

    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    public void storeAccessToken(String accessToken, String email) {
        String key = ACCESS_TOKEN_PREFIX + accessToken;
        redisTemplate.opsForValue().set(key, email, Duration.ofMinutes(accessTokenExpireMinutes));
        log.debug("Access token stored for email: {}", email);
    }

    public void storeRefreshToken(String refreshToken, String email) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(key, email, Duration.ofMinutes(refreshTokenExpireMinutes));
        log.debug("Refresh token stored for email: {}", email);
    }

    public boolean isValidAccessToken(String accessToken, String email) {
        String key = ACCESS_TOKEN_PREFIX + accessToken;
        String storedEmail = redisTemplate.opsForValue().get(key);
        return email.equals(storedEmail);
    }

    public boolean isValidRefreshToken(String refreshToken, String email) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        String storedEmail = redisTemplate.opsForValue().get(key);
        return email.equals(storedEmail);
    }

    public void removeAccessToken(String accessToken) {
        String key = ACCESS_TOKEN_PREFIX + accessToken;
        redisTemplate.delete(key);
        log.debug("Access token removed: {}", accessToken);
    }

    public void removeRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.delete(key);
        log.debug("Refresh token removed: {}", refreshToken);
    }

    public void removeAllTokensForEmail(String email) {
        // Access token 패턴으로 검색하여 삭제
        String accessPattern = ACCESS_TOKEN_PREFIX + "*";
        String refreshPattern = REFRESH_TOKEN_PREFIX + "*";
        
        redisTemplate.keys(accessPattern).forEach(key -> {
            String storedEmail = redisTemplate.opsForValue().get(key);
            if (email.equals(storedEmail)) {
                redisTemplate.delete(key);
            }
        });
        
        redisTemplate.keys(refreshPattern).forEach(key -> {
            String storedEmail = redisTemplate.opsForValue().get(key);
            if (email.equals(storedEmail)) {
                redisTemplate.delete(key);
            }
        });
        
        log.info("All tokens removed for email: {}", email);
    }
}