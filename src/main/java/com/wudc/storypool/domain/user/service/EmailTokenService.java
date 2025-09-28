package com.wudc.storypool.domain.user.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import de.huxhorn.sulky.ulid.ULID;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${jwt.accessSecret}")
    private String jwtSecret;
    
    @Value("${jwt.issuer}")
    private String issuer;

    private static final String EMAIL_TOKEN_PREFIX = "email_token:";
    private static final int EMAIL_TOKEN_EXPIRY_MINUTES = 3; // 180초

    public String generateEmailToken(String email) {
        ULID ulid = new ULID();
        String tokenId = ulid.nextULID();
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        
        String token = Jwts.builder()
                .setSubject(email)
                .setId(tokenId)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plusSeconds(EMAIL_TOKEN_EXPIRY_MINUTES * 60)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Redis에 email을 key로, token을 value로 저장 (덮어쓰기)
        String redisKey = EMAIL_TOKEN_PREFIX + email;
        redisTemplate.opsForValue().set(redisKey, token, Duration.ofMinutes(EMAIL_TOKEN_EXPIRY_MINUTES));
        
        log.info("Email token generated for email: {}", email);
        return token;
    }

    public boolean validateEmailToken(String email, String token) {
        String redisKey = EMAIL_TOKEN_PREFIX + email;
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        
        if (storedToken != null && storedToken.equals(token)) {
            // 토큰 사용 후 즉시 삭제
            redisTemplate.delete(redisKey);
            log.info("Email token validated and removed for email: {}", email);
            return true;
        }
        
        log.warn("Email token validation failed for email: {}", email);
        return false;
    }
}