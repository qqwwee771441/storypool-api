package com.wudc.storypool.global.fcm;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.wudc.storypool.domain.user.entity.Device;
import com.wudc.storypool.domain.user.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final DeviceService deviceService;

    public void sendFairytaleCompletionNotification(String userId, String fairytaleId, String fairytaleName) {
        try {
            List<Device> devices = deviceService.getAllDevicesByUserId(userId);
            
            if (devices.isEmpty()) {
                log.warn("No devices found for user: {}", userId);
                return;
            }
            
            int successCount = 0;
            int failureCount = 0;
            
            for (Device device : devices) {
                try {
                    sendNotificationToDevice(device.getFcmToken(), fairytaleId, fairytaleName);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to send notification to device: {} for user: {}", device.getId(), userId, e);
                    failureCount++;
                }
            }
            
            log.info("Fairytale completion notifications sent - Success: {}, Failed: {}, Total devices: {} for user: {}", 
                    successCount, failureCount, devices.size(), userId);
        } catch (Exception e) {
            log.error("Failed to send fairytale completion notification to user: {}", userId, e);
        }
    }

    public void sendNotificationToUser(String userId, String title, String body, String type, String targetId, String message) {
        if (!isFirebaseAvailable()) {
            log.warn("Firebase is not properly configured. Skipping FCM notification for user: {}", userId);
            return;
        }

        try {
            List<Device> devices = deviceService.getAllDevicesByUserId(userId);
            
            if (devices.isEmpty()) {
                log.warn("No devices found for user: {}", userId);
                return;
            }
            
            for (Device device : devices) {
                sendCustomNotificationToDevice(device.getFcmToken(), title, body, type, targetId, message);
            }
            
            log.info("Custom notifications sent to {} devices for user: {}", devices.size(), userId);
        } catch (Exception e) {
            log.error("Failed to send custom notification to user: {}", userId, e);
        }
    }

    private boolean isFirebaseAvailable() {
        try {
            return !FirebaseApp.getApps().isEmpty() && FirebaseApp.getInstance() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void sendNotificationToDevice(String fcmToken, String fairytaleId, String fairytaleName) throws FirebaseMessagingException {
        String title = "동화 생성 완료!";
        String body = String.format("'%s' 동화 생성이 완료되었습니다.", fairytaleName);
        
        Map<String, String> data = new HashMap<>();
        data.put("id", fairytaleId);
        data.put("type", "FAIRYTALE_GENERATED");
        data.put("message", body);
        data.put("targetId", fairytaleId);
        data.put("isRead", "false");
        data.put("createdAt", Instant.now().toString());
        
        Message message = Message.builder()
            .setToken(fcmToken)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .putAllData(data)
            .setAndroidConfig(AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                    .setChannelId("fairytale_notifications")
                    .setPriority(AndroidNotification.Priority.HIGH)
                    .build())
                .build())
            .setApnsConfig(ApnsConfig.builder()
                .setAps(Aps.builder()
                    .setAlert(ApsAlert.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                    .setSound("default")
                    .build())
                .build())
            .build();
        
        String response = FirebaseMessaging.getInstance().send(message);
        log.info("Successfully sent FCM message to token: {} for fairytale: {}, response: {}", 
                fcmToken.substring(0, Math.min(10, fcmToken.length())) + "...", fairytaleId, response);
    }

    private void sendCustomNotificationToDevice(String fcmToken, String title, String body, String type, String targetId, String message) throws FirebaseMessagingException {
        Map<String, String> data = new HashMap<>();
        data.put("type", type);
        data.put("message", message);
        data.put("targetId", targetId);
        data.put("isRead", "false");
        data.put("createdAt", Instant.now().toString());
        
        Message fcmMessage = Message.builder()
            .setToken(fcmToken)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .putAllData(data)
            .setAndroidConfig(AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                    .setChannelId("general_notifications")
                    .setPriority(AndroidNotification.Priority.HIGH)
                    .build())
                .build())
            .setApnsConfig(ApnsConfig.builder()
                .setAps(Aps.builder()
                    .setAlert(ApsAlert.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                    .setSound("default")
                    .build())
                .build())
            .build();
        
        String response = FirebaseMessaging.getInstance().send(fcmMessage);
        log.info("Successfully sent custom FCM message to token: {}, response: {}", 
                fcmToken.substring(0, Math.min(10, fcmToken.length())) + "...", response);
    }
}