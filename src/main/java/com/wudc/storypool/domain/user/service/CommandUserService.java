package com.wudc.storypool.domain.user.service;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.domain.notification.service.NotificationSettingsService;
import com.wudc.storypool.domain.upload.service.S3UploadService;
import com.wudc.storypool.domain.user.controller.request.UpdateUserProfileRequest;
import com.wudc.storypool.domain.user.entity.User;
import com.wudc.storypool.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CommandUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NicknameService nicknameService;
    private final NotificationSettingsService notificationSettingsService;
    private final S3UploadService s3UploadService;

    @Value("${default.image.url}")
    private String defaultProfileImageUrl;

    public void updateProfileById(String userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // 프로필 이미지 URL이 null이면 기본 이미지로 설정
        String currentProfileImageUrl = user.getProfileImageUrl();
        String newProfileImageUrl = request.profileImageUrl() != null ? 
                request.profileImageUrl() : defaultProfileImageUrl;
        
        if (shouldDeleteOldProfileImage(currentProfileImageUrl, newProfileImageUrl)) {
            try {
                s3UploadService.deleteByUrl(currentProfileImageUrl);
                log.info("Successfully deleted old profile image for user: {} URL: {}", userId, currentProfileImageUrl);
            } catch (Exception e) {
                // 기존 이미지 삭제 실패해도 프로필 업데이트는 진행
                log.warn("Failed to delete old profile image for user: {} URL: {} error: {}", 
                        userId, currentProfileImageUrl, e.getMessage());
            }
        }

        user.setNickname(request.nickname());
        user.setDescription(request.description());
        user.setProfileImageUrl(newProfileImageUrl);
    }

    private boolean shouldDeleteOldProfileImage(String currentUrl, String newUrl) {
        // 현재 이미지가 없으면 삭제할 필요 없음
        if (currentUrl == null || currentUrl.isEmpty()) {
            return false;
        }
        
        // 기본 이미지는 삭제하지 않음
        if (currentUrl.equals(defaultProfileImageUrl)) {
            return false;
        }
        
        // 새 URL이 다른 경우에만 삭제 (null도 기본 이미지로 변환되므로 다름으로 처리)
        return !currentUrl.equals(newUrl);
    }

    public void updatePasswordById(String userId, String newPassword, String password) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BaseException(ErrorCode.INVALID_PASSWORD);
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
    }

    public String createUser(String email, String password) {
        Optional<User> user = userRepository.findDeletedUserByEmail(email);
        String encodedPassword = passwordEncoder.encode(password);

        if (user.isPresent()) {
            user.get().setDeleted(false);
            user.get().setPassword(encodedPassword);
            
            notificationSettingsService.createDefaultSettings(user.get().getId());
            
            return user.get().getId();
        }

        String nickname = nicknameService.generateUniqueNickname(email);

        User newUser = User.createUser(email, encodedPassword, nickname, defaultProfileImageUrl);
        User savedUser = userRepository.save(newUser);

        notificationSettingsService.createDefaultSettings(savedUser.getId());

        log.info("User created successfully with email: {} and id: {}", email, savedUser.getId());
        return savedUser.getId();
    }

    public void deleteProfileImage(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        String profileImageUrl = user.getProfileImageUrl();
        if (profileImageUrl == null || profileImageUrl.isEmpty()) {
            log.warn("User {} attempted to delete profile image but no profile image is set", userId);
            throw new BaseException(ErrorCode.FILE_NOT_FOUND);
        }

        // 기본 프로필 이미지인 경우 S3에서 삭제하지 않음
        if (profileImageUrl.equals(defaultProfileImageUrl)) {
            log.warn("User {} attempted to delete default profile image", userId);
            throw new BaseException(ErrorCode.ACCESS_DENIED);
        }

        // S3에서 이미지 삭제
        s3UploadService.deleteByUrl(profileImageUrl);

        // 사용자 엔티티의 프로필 이미지 URL을 null로 설정
        user.setProfileImageUrl(null);
        userRepository.save(user);

        log.info("Profile image deleted successfully for user: {}", userId);
    }
}
