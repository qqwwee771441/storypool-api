package com.wudc.storypool.domain.user.repository;

import com.wudc.storypool.domain.user.entity.Device;
import com.wudc.storypool.domain.user.entity.User;
import com.wudc.storypool.domain.user.entity.constant.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    
    List<Device> findAllByUser(User user);
    
    List<Device> findByUserOrderByCreatedAtDesc(User user);
    
    Optional<Device> findByUserAndDeviceIdAndPlatform(User user, String deviceId, Platform platform);
    
    Optional<Device> findByIdAndUser(String id, User user);
    
    boolean existsByUserAndDeviceIdAndPlatform(User user, String deviceId, Platform platform);
    
    List<Device> findAllByUserAndPlatform(User user, Platform platform);
}