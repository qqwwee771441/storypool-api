package com.wudc.storypool.domain.user.service;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String LOGIN_ATTEMPT_PREFIX = "login_attempt:";
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 10;

    public void validateLoginAttempt(String email) {
        String key = LOGIN_ATTEMPT_PREFIX + email;
        String attempts = redisTemplate.opsForValue().get(key);

        if (attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS) {
            log.warn("Account locked due to too many failed login attempts: {}", email);
            throw new BaseException(ErrorCode.ACCOUNT_LOCKED);
        }
    }

    public void recordFailedAttempt(String email) {
        String key = LOGIN_ATTEMPT_PREFIX + email;
        String attempts = redisTemplate.opsForValue().get(key);

        if (attempts == null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(LOCKOUT_DURATION_MINUTES));
            log.debug("First failed login attempt recorded for email: {}", email);
        } else {
            int currentAttempts = Integer.parseInt(attempts) + 1;
            redisTemplate.opsForValue().set(key, String.valueOf(currentAttempts), Duration.ofMinutes(LOCKOUT_DURATION_MINUTES));
            log.warn("Failed login attempt {} recorded for email: {}", currentAttempts, email);
            
            if (currentAttempts >= MAX_ATTEMPTS) {
                log.warn("Account locked after {} failed attempts: {}", MAX_ATTEMPTS, email);
            }
        }
    }

    public void resetFailedAttempts(String email) {
        String key = LOGIN_ATTEMPT_PREFIX + email;
        redisTemplate.delete(key);
        log.debug("Failed login attempts reset for email: {}", email);
    }

    public int getFailedAttempts(String email) {
        String key = LOGIN_ATTEMPT_PREFIX + email;
        String attempts = redisTemplate.opsForValue().get(key);
        return attempts != null ? Integer.parseInt(attempts) : 0;
    }
}