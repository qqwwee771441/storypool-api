package com.wudc.storypool.domain.notification.service;

import com.wudc.storypool.domain.notification.entity.NotificationSettings;
import com.wudc.storypool.domain.notification.repository.NotificationSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSettingsService {

    private final NotificationSettingsRepository notificationSettingsRepository;

    @Transactional(readOnly = true)
    public NotificationSettings getSettings(String userId) {
        return notificationSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
    }

    @Transactional
    public NotificationSettings updateSettings(String userId, boolean pushEnabled, boolean emailEnabled,
                                              boolean onComment, boolean onReply, boolean onLike, 
                                              boolean onFairytaleComplete) {
        NotificationSettings settings = notificationSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        settings.updateSettings(pushEnabled, emailEnabled, onComment, onReply, onLike, onFairytaleComplete);
        NotificationSettings savedSettings = notificationSettingsRepository.save(settings);
        
        log.info("Notification settings updated for user: {}", userId);
        return savedSettings;
    }

    @Transactional
    public NotificationSettings createDefaultSettings(String userId) {
        if (notificationSettingsRepository.existsByUserId(userId)) {
            return notificationSettingsRepository.findByUserId(userId).get();
        }

        NotificationSettings defaultSettings = NotificationSettings.createDefault(userId);
        NotificationSettings savedSettings = notificationSettingsRepository.save(defaultSettings);
        
        log.info("Default notification settings created for user: {}", userId);
        return savedSettings;
    }
}