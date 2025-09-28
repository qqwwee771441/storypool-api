package com.wudc.storypool.domain.notification.controller.response;

import java.time.Instant;
import java.util.List;

public record NotificationListResponse(
    List<NotificationItem> notifications,
    boolean hasNext,
    String nextCursor
) {
    public record NotificationItem(
        String id,
        String type,
        String message,
        String targetId,
        boolean isRead,
        Instant createdAt
    ) {}
}