package com.wudc.storypool.global.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class ServiceAccountTokenInterceptor implements HandlerInterceptor {

    @Value("${llm.server.accept-token:default-token}")
    private String expectedServiceToken;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String authHeader = request.getHeader("Authorization");

        // POST 나 PATCH 가 아니면 통과
        if (!request.getMethod().equals("PATCH") && !request.getMethod().equals("POST")) {
            return true;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                new ObjectMapper().writeValueAsString(
                    Map.of("error", "Missing or invalid Authorization header")
                )
            );
            return false;
        }

        String token = authHeader.substring(7);

        if (!expectedServiceToken.equals(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                new ObjectMapper().writeValueAsString(
                    Map.of("error", "invalid Access Token")
                )
            );
            return false;
        }

        log.debug("Service account token validated successfully");
        return true;
    }
}