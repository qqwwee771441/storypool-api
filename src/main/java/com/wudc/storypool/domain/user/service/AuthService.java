package com.wudc.storypool.domain.user.service;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.domain.notification.service.NotificationSettingsService;
import com.wudc.storypool.domain.user.entity.User;
import com.wudc.storypool.domain.user.repository.UserRepository;
import com.wudc.storypool.global.security.jwt.JwtGenerator;
import com.wudc.storypool.global.security.jwt.JwtParser;
import com.wudc.storypool.global.security.jwt.JwtProperties;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final EmailService emailService;
    private final EmailTokenService emailTokenService;
    private final CommandUserService commandUserService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtGenerator jwtGenerator;
    private final JwtParser jwtParser;
    private final JwtProperties jwtProperties;
    private final TokenStorageService tokenStorageService;
    private final LoginAttemptService loginAttemptService;
    private final NotificationSettingsService notificationSettingsService;

    public int sendCodeByEmail(String email) {
        return emailService.sendAuthCode(email);
    }

    public String verifyCodeAndGenerateToken(String email, String code) {
        boolean isValidCode = emailService.verifyAuthCode(email, code);
        
        if (!isValidCode) {
            throw new BaseException(ErrorCode.INVALID_OR_EXPIRED_CODE);
        }
        
        return emailTokenService.generateEmailToken(email);
    }

    public String signup(String email, String emailToken, String password) {
        // 1. 이메일 토큰 검증
        boolean isValidToken = emailTokenService.validateEmailToken(email, emailToken);
        if (!isValidToken) {
            log.warn("Invalid email token provided for email: {}", email);
            throw new BaseException(ErrorCode.INVALID_EMAIL_TOKEN);
        }

        // 2. 회원가입 처리
        String userId = commandUserService.createUser(email, password);
        
        log.info("User signup completed successfully for email: {} with userId: {}", email, userId);
        return userId;
    }

    public LoginTokens login(String email, String password) {
        loginAttemptService.validateLoginAttempt(email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    loginAttemptService.recordFailedAttempt(email);
                    log.warn("Login attempted with non-existent or deleted email: {}", email);
                    return new BaseException(ErrorCode.INVALID_LOGIN_CREDENTIALS);
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            loginAttemptService.recordFailedAttempt(email);
            log.warn("Login failed due to incorrect password for email: {}", email);
            throw new BaseException(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        loginAttemptService.resetFailedAttempts(email);

        String accessToken = jwtGenerator.generateAccessToken(user.getId());
        String refreshToken = jwtGenerator.generateRefreshToken(user.getId());

        tokenStorageService.storeAccessToken(accessToken, email);
        tokenStorageService.storeRefreshToken(refreshToken, email);

        log.info("User login successful for email: {} with userId: {}", email, user.getId());
        return new LoginTokens(accessToken, refreshToken);
    }

    public LoginTokens refreshTokens(String accessToken, String refreshToken) {
        // 1. 리프레시 토큰에서 이메일 정보 추출 및 검증
        String cleanRefreshToken = refreshToken.replace("Bearer ", "");
        SecretKey refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getRefreshSecret()));
        String userIdFromRefresh = jwtParser.getUserIdByToken(cleanRefreshToken, refreshKey);
        
        if (userIdFromRefresh == null) {
            log.warn("Invalid refresh token provided");
            throw new BaseException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. 액세스 토큰에서 사용자 ID 추출 (만료 여부 무관)
        String cleanAccessToken = accessToken.replace("Bearer ", "");
        SecretKey accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getAccessSecret()));
        String userIdFromAccess = jwtParser.getUserIdByToken(cleanAccessToken, accessKey);
        
        // 3. 토큰 간 사용자 ID 일치 확인
        if (!userIdFromRefresh.equals(userIdFromAccess)) {
            log.warn("Token mismatch: refresh userId {} vs access userId {}", userIdFromRefresh, userIdFromAccess);
            throw new BaseException(ErrorCode.TOKEN_MISMATCH);
        }

        // 4. 사용자 조회 및 활성 상태 확인
        User user = userRepository.findById(userIdFromRefresh)
                .orElseThrow(() -> {
                    log.warn("User not found for token refresh: {}", userIdFromRefresh);
                    return new BaseException(ErrorCode.USER_NOT_FOUND);
                });

        // 5. Redis에서 리프레시 토큰 유효성 확인
        if (!tokenStorageService.isValidRefreshToken(refreshToken, user.getEmail())) {
            log.warn("Refresh token not found in Redis for email: {}", user.getEmail());
            throw new BaseException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        // 6. 새로운 토큰 생성
        String newAccessToken = jwtGenerator.generateAccessToken(user.getId());
        String newRefreshToken = jwtGenerator.generateRefreshToken(user.getId());

        // 7. Redis에서 기존 토큰 삭제 및 새 토큰 저장
        tokenStorageService.removeAccessToken(accessToken);
        tokenStorageService.removeRefreshToken(refreshToken);
        tokenStorageService.storeAccessToken(newAccessToken, user.getEmail());
        tokenStorageService.storeRefreshToken(newRefreshToken, user.getEmail());

        log.info("Tokens refreshed successfully for user: {} with userId: {}", user.getEmail(), user.getId());
        return new LoginTokens(newAccessToken, newRefreshToken);
    }

    public void logout(String accessToken, String refreshToken) {
        String cleanAccessToken = accessToken.replace("Bearer ", "");
        String cleanRefreshToken = refreshToken.replace("Bearer ", "");

        // 1. 액세스 토큰에서 사용자 정보 추출
        SecretKey accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getAccessSecret()));
        String userId = jwtParser.getUserIdByToken(cleanAccessToken, accessKey);
        
        if (userId == null) {
            log.warn("Invalid access token provided for logout");
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        // 2. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for logout: {}", userId);
                    return new BaseException(ErrorCode.USER_NOT_FOUND);
                });

        // 3. Redis에서 리프레시 토큰 존재 확인
        if (!tokenStorageService.isValidRefreshToken(refreshToken, user.getEmail())) {
            log.warn("Refresh token not found for logout: {}", user.getEmail());
            throw new BaseException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        // 4. Redis에서 모든 토큰 삭제
        tokenStorageService.removeAccessToken(accessToken);
        tokenStorageService.removeRefreshToken(refreshToken);

        log.info("User logged out successfully: {} with userId: {}", user.getEmail(), user.getId());
    }

    public void withdrawUser(String userId, String password, String accessToken, String refreshToken) {
        // 1. 사용자 조회 (탈퇴 시에는 실제 User를 찾아야 함)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for withdrawal: {}", userId);
                    return new BaseException(ErrorCode.USER_NOT_FOUND);
                });

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Invalid password provided for withdrawal: {}", user.getEmail());
            throw new BaseException(ErrorCode.INVALID_PASSWORD);
        }

        // 3. 소프트 삭제 처리
        userRepository.delete(user);

        // 4. 모든 토큰 삭제
        tokenStorageService.removeAccessToken(accessToken);
        tokenStorageService.removeRefreshToken(refreshToken);

        // 5. 해당 이메일의 모든 토큰 삭제 (다중 디바이스 대응)
        tokenStorageService.removeAllTokensForEmail(user.getEmail());

        log.info("User withdrawal completed successfully for email: {} with userId: {}", user.getEmail(), userId);
    }

    public void resetPassword(String email, String emailToken, String newPassword) {
        // 1. 이메일 토큰 검증
        boolean isValidToken = emailTokenService.validateEmailToken(email, emailToken);
        if (!isValidToken) {
            log.warn("Invalid email token provided for password reset: {}", email);
            throw new BaseException(ErrorCode.INVALID_EMAIL_TOKEN);
        }

        // 2. 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found for password reset: {}", email);
                    return new BaseException(ErrorCode.USER_NOT_FOUND);
                });

        // 3. 새 비밀번호 인코딩 및 저장
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // 4. 모든 기존 토큰 무효화 (보안상 중요)
        tokenStorageService.removeAllTokensForEmail(email);

        log.info("Password reset completed successfully for email: {}", email);
    }


    public String signupTestUser(String email, String password, String nickname, String description) {
        // 1. 중복 이메일 검사
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Test user signup failed - email already exists: {}", email);
            throw new BaseException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 3. 기본 프로필 이미지 URL 설정
        String defaultProfileImageUrl = "https://storypool-bucket.s3.amazonaws.com/default-profile.png";

        // 4. 테스트 사용자 생성
        User testUser = User.createUser(email, encodedPassword, nickname, defaultProfileImageUrl);
        
        // 5. description이 제공된 경우 설정
        if (description != null && !description.trim().isEmpty()) {
            testUser.setDescription(description);
        }

        // 6. 사용자 저장
        User savedUser = userRepository.save(testUser);

        // 7. 기본 알림 설정 생성
        notificationSettingsService.createDefaultSettings(savedUser.getId());

        log.info("Test user created successfully: {} with userId: {}", email, savedUser.getId());
        return savedUser.getId();
    }

    public record LoginTokens(String accessToken, String refreshToken) {}
}
