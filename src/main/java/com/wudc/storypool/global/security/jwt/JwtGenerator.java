package com.wudc.storypool.global.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtGenerator {
    private final JwtProperties jwtProperties;

    public String generateAccessToken(String userId) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getAccessSecret()));

        return generateToken(userId, key, jwtProperties.getTokenExpire());
    }

    public String generateRefreshToken(String userId) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getRefreshSecret()));
        return generateToken(userId, key, jwtProperties.getRefreshTokenExpire());
    }

    public String generateServiceToken(String clientId, String scope) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getAccessSecret()));
        
        return "Bearer " + Jwts.builder()
            .signWith(key, SignatureAlgorithm.HS512)
            .setSubject("service:" + clientId)
            .setIssuer(jwtProperties.getIssuer())
            .setIssuedAt(new Date())
            .setExpiration(Date.from(Instant.now().plusSeconds(jwtProperties.getTokenExpire() * 60)))
            .claim("scope", scope)
            .claim("client_id", clientId)
            .compact();
    }

    private String generateToken(String userId, Key secret, Long expire) {
        return "Bearer " + Jwts.builder()
            .signWith(secret, SignatureAlgorithm.HS512)
            .setSubject(userId)
            .setIssuer(jwtProperties.getIssuer())
            .setIssuedAt(new Date())
            .setExpiration(Date.from(Instant.now().plusSeconds(expire * 60 * 60)))
            .compact();
    }

}
