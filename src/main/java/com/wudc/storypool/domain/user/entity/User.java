package com.wudc.storypool.domain.user.entity;

import com.wudc.storypool.common.base.BaseEntity;
import com.wudc.storypool.domain.user.entity.constant.UserRole;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import lombok.*;
import org.hibernate.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@AllArgsConstructor
@SQLDelete(sql = "UPDATE user SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Column(
        nullable = false,
        length = 1000,
        unique = true
    )
    private String email;

    @Column(
        nullable = false,
        length = 1000
    )
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(
        nullable = false,
        length = 1000000000
    )
    private String profileImageUrl;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault(value = "'USER'")
    private UserRole role = UserRole.USER;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Device> devices = new ArrayList<>();

    private boolean deleted = false;

    public static User createUser(String email, String encodedPassword, String nickname, String profileImageUrl) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.nickname = nickname;
        user.profileImageUrl = profileImageUrl;
        user.role = UserRole.USER;
        return user;
    }

    public void addDevice(Device device) {
        devices.add(device);
        device.setUser(this);
    }

    public void removeDevice(Device device) {
        devices.remove(device);
        device.setUser(null);
    }
}
