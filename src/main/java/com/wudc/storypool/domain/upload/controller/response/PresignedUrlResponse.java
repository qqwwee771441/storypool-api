package com.wudc.storypool.domain.upload.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned URL 응답")
public record PresignedUrlResponse(
        @Schema(description = "업로드용 Presigned URL")
        String presignedUrl,
        
        @Schema(description = "업로드 후 파일에 접근할 수 있는 URL")
        String fileUrl,
        
        @Schema(description = "S3 객체 키")
        String objectKey,
        
        @Schema(description = "URL 만료 시간 (초)")
        int expirationTime
) {
}