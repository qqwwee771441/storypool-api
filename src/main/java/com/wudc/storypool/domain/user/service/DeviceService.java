package com.wudc.storypool.domain.user.service;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.domain.user.entity.Device;
import com.wudc.storypool.domain.user.entity.User;
import com.wudc.storypool.domain.user.entity.constant.Platform;
import com.wudc.storypool.domain.user.repository.DeviceRepository;
import com.wudc.storypool.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public Device registerDevice(String userId, String deviceId, String fcmToken, Platform platform) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        
        // Check if device already exists
        Device existingDevice = deviceRepository
                .findByUserAndDeviceIdAndPlatform(user, deviceId, platform)
                .orElse(null);

        if (existingDevice != null) {
            // Update FCM token if device already exists
            existingDevice.updateFcmToken(fcmToken);
            Device updatedDevice = deviceRepository.save(existingDevice);
            log.info("Device FCM token updated for user: {} deviceId: {}", userId, deviceId);
            return updatedDevice;
        }

        // Create new device
        Device device = Device.create(user, deviceId, fcmToken, platform);
        user.addDevice(device);
        Device savedDevice = deviceRepository.save(device);
        
        log.info("Device registered successfully for user: {} deviceId: {}", userId, deviceId);
        return savedDevice;
    }

    @Transactional(readOnly = true)
    public List<Device> getUserDevices(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        
        List<Device> devices = deviceRepository.findByUserOrderByCreatedAtDesc(user);
        log.info("Retrieved {} devices for user: {}", devices.size(), userId);
        return devices;
    }


    @Transactional
    public void unregisterDevice(String userId, String deviceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
                
        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> {
                    log.warn("Device not found or access denied for user: {} deviceId: {}", userId, deviceId);
                    return new BaseException(ErrorCode.DEVICE_NOT_FOUND);
                });

        user.removeDevice(device);
        deviceRepository.delete(device);
        log.info("Device unregistered successfully for user: {} deviceId: {}", userId, deviceId);
    }

    @Transactional(readOnly = true)
    public List<Device> getDevicesByUserIdAndPlatform(String userId, Platform platform) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        return deviceRepository.findAllByUserAndPlatform(user, platform);
    }

    @Transactional(readOnly = true)
    public List<Device> getAllDevicesByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        return deviceRepository.findAllByUser(user);
    }
}