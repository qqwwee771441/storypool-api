package com.wudc.storypool.domain.notification.controller.response;

public record NotificationSettingsResponse(
    boolean pushEnabled,
    boolean emailEnabled,
    boolean onComment,
    boolean onReply,
    boolean onLike,
    boolean onFairytaleComplete
) {}