package com.wudc.storypool.domain.notification.repository;

import com.wudc.storypool.domain.notification.entity.NotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, String> {
    
    Optional<NotificationSettings> findByUserId(String userId);
    
    boolean existsByUserId(String userId);
}