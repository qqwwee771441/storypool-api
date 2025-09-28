package com.wudc.storypool.global.security.jwt;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.domain.user.entity.User;
import com.wudc.storypool.domain.user.repository.UserRepository;
import com.wudc.storypool.domain.user.service.TokenStorageService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtParser {
    private final JwtProperties jwtProperties;
    private final TokenStorageService tokenStorageService;
    private final UserRepository userRepository;

    public String parseAccessToken(HttpServletRequest request, HttpServletResponse response) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getAccessSecret()));

        String token = request.getHeader("Authorization");

        if (token == null || token.replace("Bearer ", "").isBlank() || token.isBlank()) {
            return null;
        }

        String accessToken = token.replace("Bearer ", "");
        String userId = getUserIdByToken(accessToken, key);

        if (userId == null) {
            return null;
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (!tokenStorageService.isValidAccessToken("Bearer " + accessToken, user.getEmail())) {
            return null;
        }

        return userId;
    }

    public String getUserIdByToken(String token, Key key) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        } catch (ExpiredJwtException | MalformedJwtException e) {
            return null;
        }
    }
}
