package com.wudc.storypool.domain.fairytale.controller.response;

import com.wudc.storypool.domain.fairytale.entity.constant.FairytaleStatus;

public record GenerateFairytaleResponse(
    String fairytaleId,
    FairytaleStatus status,
    String message
) {}