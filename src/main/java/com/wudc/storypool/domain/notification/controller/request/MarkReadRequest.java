package com.wudc.storypool.domain.notification.controller.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record MarkReadRequest(
    @NotEmpty(message = "알림 ID 목록은 필수입니다.")
    List<String> notificationIds
) {}