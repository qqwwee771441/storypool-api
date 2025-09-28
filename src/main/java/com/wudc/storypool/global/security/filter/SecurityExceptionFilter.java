package com.wudc.storypool.global.security.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.common.base.BaseErrorResponse;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.global.security.exception.BaseSecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@Slf4j
public class SecurityExceptionFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver resolver;

    // 생성자 주입을 통해 HandlerExceptionResolver를 주입받습니다.
    @Autowired
    public SecurityExceptionFilter(
        @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws IOException { // IOException 추가
        try {
            // 다음 필터 또는 서블릿으로 요청을 전달합니다.
            filterChain.doFilter(request, response);
        } catch (BaseSecurityException e) {
            // BasicSecurityException 발생 시 커스텀 응답을 생성합니다.
            exceptionToResponse(e.getErrorCode(), e.getRes());
        } catch (Exception e) {
            // 그 외 모든 예외 발생 시 스택 트레이스를 출력하고
            // Spring의 기본 예외 처리 메커니즘을 사용합니다.
            e.printStackTrace();
            resolver.resolveException(request, response, null, e);
        }
    }

    /**
     * 예외 정보를 기반으로 HTTP 응답을 구성합니다.
     *
     * @param errorCode 예외에 해당하는 에러 코드 객체
     * @param response  HttpServletResponse 객체
     * @throws IOException 응답 작성 중 발생할 수 있는 예외
     */
    private void exceptionToResponse(ErrorCode errorCode, HttpServletResponse response) throws IOException {
        // BasicResponse 객체를 생성하여 에러 정보를 담습니다.
        if (errorCode.getStatus().is5xxServerError()) {
            log.error("Server error occurred: {}", errorCode.getMessage());
        } else {
            log.warn("Client error occurred: {}", errorCode.getMessage());
        }

        ResponseEntity<BaseErrorResponse> body = ResponseEntity
            .status(errorCode.getStatus())
            .body(new BaseErrorResponse(errorCode.getMessage()));
        // HTTP 상태 코드 설정
        response.setStatus(errorCode.getStatus().value());
        // Content-Type을 JSON으로 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 문자 인코딩을 UTF-8로 설정
        response.setCharacterEncoding("UTF-8");

        // ObjectMapper를 사용하여 baseResponse 객체를 JSON 문자열로 변환하고 응답 본문에 작성합니다.
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }
}