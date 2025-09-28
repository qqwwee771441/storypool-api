package com.wudc.storypool.domain.upload.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Presigned URL 요청")
public record PresignedUrlRequest(
        @Schema(description = "파일명", example = "profile-image.jpg")
        @NotBlank(message = "파일명은 필수입니다.")
        String fileName,
        
        @Schema(description = "MIME 타입", example = "image/jpeg")
        @NotBlank(message = "MIME 타입은 필수입니다.")
        String contentType
) {
}