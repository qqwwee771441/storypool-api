package com.wudc.storypool.global.security.filter;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.global.security.exception.CustomSecurityException;
import com.wudc.storypool.global.security.exception.TokenExpiredException;
import com.wudc.storypool.global.security.exception.TokenNotValidException;
import com.wudc.storypool.global.security.jwt.JwtParser;
import com.wudc.storypool.global.security.jwt.JwtProperties;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtParser jwtParser;
    private final UserDetailsService principalDetailsService; // UserDetailsService로 변경
    private final JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException { // IOException 추가
        String userId = jwtParser.parseAccessToken(request, response);

        if (userId == null) {
            filterChain.doFilter(request, response);
        } else {
            try {
                UserDetails userDetails = principalDetailsService.loadUserByUsername(userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);
            } catch (BaseException e) {
                throw new CustomSecurityException(response, request, e.getErrorCode());
            }
        }
    }
}