package com.wudc.storypool.domain.user.entity;

import com.wudc.storypool.common.base.BaseEntity;
import com.wudc.storypool.domain.user.entity.constant.Platform;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor()
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "uk_device_user_deviceid", columnNames = {"user_id", "deviceId"})
})
public class Device extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 64)
    private String deviceId;

    @Column(nullable = false, length = 1000)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    public static Device create(User user, String deviceId, String fcmToken, Platform platform) {
        Device device = new Device();
        device.user = user;
        device.deviceId = deviceId;
        device.fcmToken = fcmToken;
        device.platform = platform;
        return device;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}