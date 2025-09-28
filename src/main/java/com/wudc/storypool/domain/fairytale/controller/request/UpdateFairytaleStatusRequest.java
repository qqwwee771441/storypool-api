package com.wudc.storypool.domain.fairytale.controller.request;

import com.wudc.storypool.domain.fairytale.entity.constant.FairytaleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "동화 생성 상태 수정 요청")
public record UpdateFairytaleStatusRequest(
    @Schema(description = "동화 생성 진행 상태", example = "IN_PROGRESS")
    @NotNull(message = "상태는 필수입니다.")
    FairytaleStatus status,
    
    @Schema(description = "상태에 대한 부가 설명", example = "이미지 10/30 생성 완료")
    @Size(max = 255, message = "메시지는 최대 255자까지 입력할 수 있습니다.")
    String message
) {}