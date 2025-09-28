package com.wudc.storypool.domain.notification.controller;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.notification.controller.request.MarkReadRequest;
import com.wudc.storypool.domain.notification.controller.response.DeleteNotificationResponse;
import com.wudc.storypool.domain.notification.controller.response.MarkReadResponse;
import com.wudc.storypool.domain.notification.controller.response.NotificationListResponse;
import com.wudc.storypool.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 커서 기반 페이지네이션으로 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파라미터 (limit 범위 오류 등)"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping
    public NotificationListResponse getNotifications(
        @Parameter(description = "한 번에 가져올 알림 개수 (기본값 20, 최대 100)")
        @RequestParam(defaultValue = "20") int limit,
        
        @Parameter(description = "이전 응답에서 받은 nextCursor 값. 없으면 최신 알림부터 조회")
        @RequestParam(required = false) String cursor,
        
        @Parameter(description = "true면 읽지 않은 알림만 필터링 (기본값 false)")
        @RequestParam(defaultValue = "false") boolean unread
    ) {
        if (limit > 100) limit = 100;
        if (limit < 1) limit = 1;

        String userId = AuthUtil.getUserId();
        return notificationService.getNotifications(userId, limit, cursor, unread);
    }

    @Operation(summary = "알림 읽음 처리", description = "선택된 알림들을 읽음 상태로 처리합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공"),
        @ApiResponse(responseCode = "400", description = "notificationIds 배열이 비어있거나 잘못된 ID 포함"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "403", description = "본인의 알림이 아님"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/mark-read")
    public MarkReadResponse markNotificationsAsRead(
        @Valid @RequestBody MarkReadRequest request
    ) {
        String userId = AuthUtil.getUserId();
        return notificationService.markNotificationsAsRead(userId, request.notificationIds());
    }

    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "알림 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "403", description = "본인의 알림이 아님"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 알림"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/{id}")
    public DeleteNotificationResponse deleteNotification(
        @Parameter(description = "삭제할 알림의 ID")
        @PathVariable String id
    ) {
        String userId = AuthUtil.getUserId();
        return notificationService.deleteNotification(userId, id);
    }
}