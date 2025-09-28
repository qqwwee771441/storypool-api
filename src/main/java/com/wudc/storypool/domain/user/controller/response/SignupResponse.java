package com.wudc.storypool.domain.user.controller.response;

public record SignupResponse(
    boolean success,
    String message
) {
}