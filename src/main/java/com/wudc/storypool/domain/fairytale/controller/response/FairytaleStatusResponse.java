package com.wudc.storypool.domain.fairytale.controller.response;

import com.wudc.storypool.domain.fairytale.entity.constant.FairytaleStatus;

public record FairytaleStatusResponse(
    FairytaleStatus status,
    String message
) {}