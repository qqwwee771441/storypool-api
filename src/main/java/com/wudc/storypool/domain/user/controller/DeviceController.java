package com.wudc.storypool.domain.user.controller;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.user.controller.request.RegisterDeviceRequest;
import com.wudc.storypool.domain.user.controller.response.DeviceResponse;
import com.wudc.storypool.domain.user.entity.Device;
import com.wudc.storypool.domain.user.service.DeviceService;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Device", description = "디바이스 관리 및 푸시 알림 API")
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "디바이스 등록", description = "FCM 푸시 알림을 위한 디바이스를 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "디바이스가 성공적으로 등록됨"),
        @ApiResponse(responseCode = "400", description = "요청 본문 필수 필드 누락 또는 형식 오류"),
        @ApiResponse(responseCode = "401", description = "토큰이 없거나 유효하지 않음"),
        @ApiResponse(responseCode = "409", description = "동일한 디바이스가 이미 등록됨"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceResponse registerDevice(@RequestBody @Valid RegisterDeviceRequest request) {
        String userId = AuthUtil.getUserId();
        Device device = deviceService.registerDevice(
            userId,
            request.deviceId(),
            request.fcmToken(),
            request.platform()
        );
        
        return new DeviceResponse(
            device.getId(),
            device.getDeviceId(),
            device.getFcmToken(),
            device.getPlatform(),
            device.getCreatedAtByLocalDateTime()
        );
    }

    @Operation(summary = "내 디바이스 목록 조회", description = "현재 사용자가 등록한 모든 디바이스 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "디바이스 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "토큰이 없거나 유효하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping
    public List<DeviceResponse> getMyDevices() {
        String userId = AuthUtil.getUserId();
        List<Device> devices = deviceService.getUserDevices(userId);
        
        return devices.stream()
                .map(device -> new DeviceResponse(
                    device.getId(),
                    device.getDeviceId(),
                    device.getFcmToken(),
                    device.getPlatform(),
                    device.getCreatedAtByLocalDateTime()
                ))
                .toList();
    }

    @Operation(summary = "디바이스 해제", description = "등록된 디바이스를 해제하여 더 이상 푸시 알림을 받지 않도록 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "디바이스 해제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 토큰이 없거나 유효하지 않음"),
        @ApiResponse(responseCode = "404", description = "지정된 id의 디바이스가 존재하지 않음 또는 본인 소유 아님"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/{id}")
    public Map<String, String> unregisterDevice(@PathVariable String id) {
        String userId = AuthUtil.getUserId();
        deviceService.unregisterDevice(userId, id);
        
        return Map.of("message", "Device unregistered successfully.");
    }
}