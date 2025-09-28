package com.wudc.storypool.global.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "Health", description = "애플리케이션 상태 체크 API")
@RestController
public class HealthController {

    @Operation(summary = "간단한 헬스 체크", description = "애플리케이션이 정상 동작하는지 확인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "애플리케이션이 정상 동작 중"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> simpleHealth() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", "storypool"
        ));
    }

//    @Operation(summary = "애플리케이션 정보", description = "애플리케이션의 기본 정보를 제공합니다.")
//    @ApiResponses({
//        @ApiResponse(responseCode = "200", description = "정보 조회 성공")
//    })
//    @GetMapping("/")
//    public ResponseEntity<Map<String, Object>> info() {
//        return ResponseEntity.ok(Map.of(
//            "application", "StoryPool API Server",
//            "version", "1.0.0",
//            "status", "UP",
//            "timestamp", LocalDateTime.now()
//        ));
//    }
}