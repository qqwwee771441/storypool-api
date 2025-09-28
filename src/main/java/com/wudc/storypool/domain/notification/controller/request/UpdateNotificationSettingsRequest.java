package com.wudc.storypool.domain.notification.controller.request;

import jakarta.validation.constraints.NotNull;

public record UpdateNotificationSettingsRequest(
    @NotNull(message = "pushEnabled는 필수입니다.")
    Boolean pushEnabled,
    
    @NotNull(message = "emailEnabled는 필수입니다.")
    Boolean emailEnabled,
    
    @NotNull(message = "onComment는 필수입니다.")
    Boolean onComment,
    
    @NotNull(message = "onReply는 필수입니다.")
    Boolean onReply,
    
    @NotNull(message = "onLike는 필수입니다.")
    Boolean onLike,
    
    @NotNull(message = "onFairytaleComplete는 필수입니다.")
    Boolean onFairytaleComplete
) {}