package com.wudc.storypool.domain.notification.controller;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.notification.controller.request.UpdateNotificationSettingsRequest;
import com.wudc.storypool.domain.notification.controller.response.NotificationSettingsResponse;
import com.wudc.storypool.domain.notification.entity.NotificationSettings;
import com.wudc.storypool.domain.notification.service.NotificationSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "NotificationSettings", description = "알림 설정 관리 API")
@RestController
@RequestMapping("/api/users/me/settings")
@RequiredArgsConstructor
public class NotificationSettingsController {

    private final NotificationSettingsService notificationSettingsService;

    @Operation(summary = "내 알림 설정 조회", description = "현재 사용자의 알림 설정을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "알림 설정 조회 성공"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "404", description = "사용자 알림 설정을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/notifications")
    public NotificationSettingsResponse getNotificationSettings() {
        String userId = AuthUtil.getUserId();
        NotificationSettings settings = notificationSettingsService.getSettings(userId);
        
        return new NotificationSettingsResponse(
            settings.getPushEnabled(),
            settings.getEmailEnabled(),
            settings.getOnComment(),
            settings.getOnReply(),
            settings.getOnLike(),
            settings.getOnFairytaleComplete()
        );
    }

    @Operation(summary = "내 알림 설정 수정", description = "사용자의 알림 설정을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "알림 설정 수정 성공"),
        @ApiResponse(responseCode = "400", description = "요청 본문 검증 실패 (불린 값 등)"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PutMapping("/notifications")
    public NotificationSettingsResponse updateNotificationSettings(
        @RequestBody @Valid UpdateNotificationSettingsRequest request
    ) {
        String userId = AuthUtil.getUserId();
        NotificationSettings settings = notificationSettingsService.updateSettings(
            userId,
            request.pushEnabled(),
            request.emailEnabled(),
            request.onComment(),
            request.onReply(),
            request.onLike(),
            request.onFairytaleComplete()
        );
        
        return new NotificationSettingsResponse(
            settings.getPushEnabled(),
            settings.getEmailEnabled(),
            settings.getOnComment(),
            settings.getOnReply(),
            settings.getOnLike(),
            settings.getOnFairytaleComplete()
        );
    }
}