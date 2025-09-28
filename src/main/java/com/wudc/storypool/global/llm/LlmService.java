package com.wudc.storypool.global.llm;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.global.config.LlmConfig;
import com.wudc.storypool.global.llm.dto.LlmApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmService {
    
    private final LlmConfig llmConfig;
    private final RestTemplate restTemplate;
    
    public void requestFairytaleGeneration(String fairytaleId, String text) {
        try {
            String url = llmConfig.getFairytaleGenerateUrl();
            
            // Request body 생성 (LLM 서버 API 스펙에 맞게)
            Map<String, Object> requestBody = Map.of(
                "fairytaleId", fairytaleId,
                "text", text
            );
            
            // HTTP Headers 설정 (Authorization 헤더 포함)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(llmConfig.getServiceToken());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.info("Sending fairytale generation request to LLM server: fairytaleId={}, url={}", 
                    fairytaleId, url);
            
            ResponseEntity<LlmApiResponse> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                LlmApiResponse.class
            );
            
            // 응답 처리
            handleLlmApiResponse(fairytaleId, response);
            
        } catch (HttpClientErrorException e) {
            handleHttpClientError(fairytaleId, e);
        } catch (HttpServerErrorException e) {
            handleHttpServerError(fairytaleId, e);
        } catch (Exception e) {
            log.error("Unexpected error occurred while sending fairytale generation request: fairytaleId={}", 
                    fairytaleId, e);
            throw new BaseException(ErrorCode.CANT_SEND_EMAIL); // 적절한 에러코드로 변경 필요
        }
    }
    
    private void handleLlmApiResponse(String fairytaleId, ResponseEntity<LlmApiResponse> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            LlmApiResponse apiResponse = response.getBody();
            if (apiResponse != null && apiResponse.isSuccess()) {
                log.info("Fairytale generation request sent successfully: fairytaleId={}", fairytaleId);
            } else {
                log.error("LLM server returned success=false: fairytaleId={}, response={}", 
                        fairytaleId, apiResponse);
                throw new BaseException(ErrorCode.CANT_SEND_EMAIL);
            }
        } else {
            log.error("Failed to send fairytale generation request: fairytaleId={}, status={}", 
                    fairytaleId, response.getStatusCode());
            throw new BaseException(ErrorCode.CANT_SEND_EMAIL);
        }
    }
    
    private void handleHttpClientError(String fairytaleId, HttpClientErrorException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        switch (status) {
            case BAD_REQUEST:
                log.error("Bad request to LLM server: fairytaleId={}, message={}", fairytaleId, e.getMessage());
                break;
            case UNAUTHORIZED:
                log.error("Unauthorized request to LLM server: fairytaleId={}, check service token", fairytaleId);
                break;
            case CONFLICT:
                log.warn("Duplicate request to LLM server: fairytaleId={}", fairytaleId);
                // 중복 요청은 성공으로 간주할 수도 있음
                return;
            default:
                log.error("Client error from LLM server: fairytaleId={}, status={}, message={}", 
                        fairytaleId, status, e.getMessage());
        }
        throw new BaseException(ErrorCode.CANT_SEND_EMAIL);
    }
    
    private void handleHttpServerError(String fairytaleId, HttpServerErrorException e) {
        log.error("Server error from LLM server: fairytaleId={}, status={}, message={}", 
                fairytaleId, e.getStatusCode(), e.getMessage());
        throw new BaseException(ErrorCode.CANT_SEND_EMAIL);
    }
}