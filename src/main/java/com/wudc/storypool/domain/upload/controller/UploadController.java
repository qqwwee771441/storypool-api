package com.wudc.storypool.domain.upload.controller;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.upload.controller.request.PresignedUrlRequest;
import com.wudc.storypool.domain.upload.controller.response.PresignedUrlResponse;
import com.wudc.storypool.domain.upload.service.S3UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Upload", description = "S3 파일 업로드 관리 API")
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UploadController {

    private final S3UploadService s3UploadService;

    @Operation(summary = "Presigned URL 발급", description = "S3에 파일을 업로드하기 위한 Presigned URL을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Presigned URL 발급 성공"),
        @ApiResponse(responseCode = "400", description = "요청 본문 필수 필드 누락 또는 지원되지 않는 파일 형식"),
        @ApiResponse(responseCode = "401", description = "토큰이 없거나 유효하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/presigned-url")
    @ResponseStatus(HttpStatus.CREATED)
    public PresignedUrlResponse generatePresignedUrl(@RequestBody @Valid PresignedUrlRequest request) {
        String userId = AuthUtil.getUserId();
        
        S3UploadService.PresignedUrlData data = s3UploadService.generatePresignedUrl(
                userId, 
                request.fileName(), 
                request.contentType()
        );
        
        return new PresignedUrlResponse(
                data.presignedUrl(),
                data.fileUrl(),
                data.objectKey(),
                data.expirationTime()
        );
    }
}