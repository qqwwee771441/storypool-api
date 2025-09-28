package com.wudc.storypool.domain.user.service;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.mail.username}")
    private String senderEmail;
    
    private static final String AUTH_CODE_PREFIX = "auth_code:";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String VERIFY_RATE_LIMIT_PREFIX = "verify_rate_limit:";
    private static final int CODE_EXPIRY_MINUTES = 3;
    private static final int CODE_LENGTH = 6;
    private static final int RATE_LIMIT_MINUTES = 1;
    private static final int VERIFY_RATE_LIMIT_MINUTES = 1;
    private static final int MAX_VERIFY_ATTEMPTS = 3;

    public int sendAuthCode(String email) {
        checkRateLimit(email);
        
        String authCode = generateAuthCode();
        
        try {
            sendEmail(email, authCode);
            saveAuthCode(email, authCode);
            saveRateLimit(email);
            
            log.info("Authentication code sent successfully to email: {}", email);
            return CODE_EXPIRY_MINUTES * 60;
        } catch (Exception e) {
            log.error("Failed to send authentication code to email: {}", email, e);
            throw new BaseException(ErrorCode.CANT_SEND_EMAIL);
        }
    }

    private void checkRateLimit(String email) {
        String rateLimitKey = RATE_LIMIT_PREFIX + email;
        String rateLimitValue = redisTemplate.opsForValue().get(rateLimitKey);
        
        if (rateLimitValue != null) {
            log.warn("Rate limit exceeded for email: {}", email);
            throw new BaseException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    private void saveRateLimit(String email) {
        String rateLimitKey = RATE_LIMIT_PREFIX + email;
        redisTemplate.opsForValue().set(rateLimitKey, "1", Duration.ofMinutes(RATE_LIMIT_MINUTES));
    }

    private void sendEmail(String email, String authCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(senderEmail);
        message.setSubject("StoryPool 인증 코드");
        message.setText("인증 코드: " + authCode + "\n\n" +
                       "이 코드는 " + CODE_EXPIRY_MINUTES + "분 후 만료됩니다.");
        
        mailSender.send(message);
    }

    private String generateAuthCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        
        return code.toString();
    }

    private void saveAuthCode(String email, String authCode) {
        String key = AUTH_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, authCode, Duration.ofMinutes(CODE_EXPIRY_MINUTES));
    }

    public boolean verifyAuthCode(String email, String inputCode) {
        checkVerifyRateLimit(email);
        
        String key = AUTH_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode != null && storedCode.equals(inputCode)) {
            redisTemplate.delete(key);
            clearVerifyRateLimit(email); // 성공 시 rate limit 초기화
            log.info("Authentication code verified successfully for email: {}", email);
            return true;
        }
        
        incrementVerifyAttempts(email);
        log.warn("Authentication code verification failed for email: {} - code mismatch or expired", email);
        return false;
    }

    private void checkVerifyRateLimit(String email) {
        String rateLimitKey = VERIFY_RATE_LIMIT_PREFIX + email;
        String attempts = redisTemplate.opsForValue().get(rateLimitKey);
        
        if (attempts != null && Integer.parseInt(attempts) >= MAX_VERIFY_ATTEMPTS) {
            log.warn("Verification rate limit exceeded for email: {}", email);
            throw new BaseException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    private void incrementVerifyAttempts(String email) {
        String rateLimitKey = VERIFY_RATE_LIMIT_PREFIX + email;
        String attempts = redisTemplate.opsForValue().get(rateLimitKey);
        
        if (attempts == null) {
            redisTemplate.opsForValue().set(rateLimitKey, "1", Duration.ofMinutes(VERIFY_RATE_LIMIT_MINUTES));
        } else {
            int currentAttempts = Integer.parseInt(attempts) + 1;
            redisTemplate.opsForValue().set(rateLimitKey, String.valueOf(currentAttempts), Duration.ofMinutes(VERIFY_RATE_LIMIT_MINUTES));
        }
    }

    private void clearVerifyRateLimit(String email) {
        String rateLimitKey = VERIFY_RATE_LIMIT_PREFIX + email;
        redisTemplate.delete(rateLimitKey);
    }
}