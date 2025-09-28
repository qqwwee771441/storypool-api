package com.wudc.storypool.domain.user.controller.request;

import com.wudc.storypool.domain.user.entity.constant.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDeviceRequest(
    @NotBlank(message = "deviceId는 필수입니다.")
    @Size(min = 1, max = 64, message = "deviceId는 1자 이상 64자 이하여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "deviceId는 영문, 숫자, '_', '-'만 허용됩니다.")
    String deviceId,
    
    @NotBlank(message = "fcmToken은 필수입니다.")
    String fcmToken,
    
    @NotNull(message = "platform은 필수입니다.")
    Platform platform
) {}