package com.wudc.storypool.domain.user.controller.response;

import com.wudc.storypool.domain.user.entity.constant.Platform;

import java.time.LocalDateTime;

public record DeviceResponse(
    String id,
    String deviceId,
    String fcmToken,
    Platform platform,
    LocalDateTime registeredAt
) {}