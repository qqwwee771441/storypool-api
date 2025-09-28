package com.wudc.storypool.domain.fairytale.controller.response;

import com.wudc.storypool.domain.fairytale.entity.constant.FairytaleStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "동화 생성 상태 수정 응답")
public record UpdateFairytaleStatusResponse(
    @Schema(description = "업데이트된 Fairytale ID", example = "01F8XYZDEF987654ABC3210JKL")
    String id,
    
    @Schema(description = "갱신된 상태", example = "IN_PROGRESS")
    FairytaleStatus status,
    
    @Schema(description = "갱신된 메시지", example = "이미지 10/30 생성 완료")
    String message,
    
    @Schema(description = "최종 변경 시각", example = "2025-07-12T08:45:30")
    LocalDateTime updatedAt
) {}