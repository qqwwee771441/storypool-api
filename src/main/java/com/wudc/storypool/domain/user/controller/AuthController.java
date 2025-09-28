package com.wudc.storypool.domain.user.controller;

import com.wudc.storypool.domain.user.controller.request.*;
import com.wudc.storypool.domain.user.controller.response.*;
import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.PrePersist;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Tag(name = "Auth", description = "사용자 인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 올바르지 않습니다"),
        @ApiResponse(responseCode = "423", description = "계정이 잠겼습니다"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/login")
    public LoginResponse login(
        @RequestBody @Valid LoginRequest request
    ) {
        AuthService.LoginTokens tokens = authService.login(request.email(), request.password());
        return new LoginResponse(tokens.accessToken(), tokens.refreshToken());
    }

    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입 또는 비밀번호 재설정을 위한 6자리 인증 코드를 이메일로 발송합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 코드 발송 성공"),
        @ApiResponse(responseCode = "400", description = "이메일 형식이 올바르지 않습니다"),
        @ApiResponse(responseCode = "429", description = "너무 많은 요청입니다. 잠시 후 다시 시도해주세요"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/send-code")
    public SendAuthCodeResponse sendCode(
        @RequestBody @Valid SendAuthCodeRequest request
    ) {
        int authCodeExpiredAt = authService.sendCodeByEmail(request.email());

        return new SendAuthCodeResponse(authCodeExpiredAt);
    }

    @Operation(summary = "이메일 인증 토큰 발급", description = "이메일과 인증 코드를 검증하여 회원가입용 인증 토큰을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증 코드 검증 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "코드가 일치하지 않거나 만료되었습니다"),
        @ApiResponse(responseCode = "429", description = "너무 많은 인증 시도입니다"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/verify-code")
    public VerifyCodeResponse verifyCode(
        @RequestBody @Valid VerifyCodeRequest request
    ) {
        String emailToken = authService.verifyCodeAndGenerateToken(request.email(), request.code());

        return new VerifyCodeResponse(emailToken);
    }

    @Operation(summary = "회원가입", description = "이메일 인증을 완료한 후 새 계정을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 필드 검증 실패"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 인증 토큰입니다"),
        @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일입니다"),
        @ApiResponse(responseCode = "422", description = "비밀번호 정책을 만족하지 않습니다"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(
        @RequestBody @Valid SignupRequest request
    ) {
        authService.signup(request.email(), request.emailToken(), request.password());

        return new SignupResponse(true, "회원가입이 완료되었습니다.");
    }

    @Operation(summary = "토큰 갱신", description = "만료된 액세스 토큰을 리프레시 토큰으로 갱신합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰입니다"),
        @ApiResponse(responseCode = "403", description = "토큰 불일치로 갱신할 수 없습니다"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/refresh")
    public RefreshTokenResponse refreshToken(
        @RequestBody @Valid RefreshTokenRequest request
    ) {
        AuthService.LoginTokens tokens = authService.refreshTokens(request.accessToken(), request.refreshToken());
        return new RefreshTokenResponse(tokens.accessToken(), tokens.refreshToken());
    }

    @Operation(summary = "로그아웃", description = "모든 토큰을 무효화하여 로그아웃합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 액세스 토큰입니다"),
        @ApiResponse(responseCode = "404", description = "삭제할 리프레시 토큰을 찾을 수 없습니다"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> logout(
        @RequestBody @Valid LogoutRequest request
    ) {
        authService.logout(request.accessToken(), request.refreshToken());
        return Map.of("message", "로그아웃되었습니다.");
    }

    @Operation(summary = "회원 탈퇴", description = "본인 확인 후 계정을 탈퇴합니다. (Soft Delete)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다"),
        @ApiResponse(responseCode = "403", description = "비밀번호가 일치하지 않습니다"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/withdrawal")
    public Map<String, String> withdrawal(
        @RequestBody @Valid WithdrawalRequest request
    ) {
        String userId = AuthUtil.getUserId();
        authService.withdrawUser(userId, request.password(), request.accessToken(), request.refreshToken());
        return Map.of("message", "회원 탈퇴가 완료되었습니다.");
    }

    @Operation(summary = "비밀번호 초기화", description = "이메일 인증을 통해 비밀번호를 재설정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 요청입니다"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 인증 토큰입니다"),
        @ApiResponse(responseCode = "404", description = "등록된 계정이 없습니다"),
        @ApiResponse(responseCode = "422", description = "비밀번호 정책을 만족하지 않습니다"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(
        @RequestBody @Valid ResetPasswordRequest request
    ) {
        authService.resetPassword(request.email(), request.emailToken(), request.newPassword());
        return Map.of("message", "비밀번호가 성공적으로 변경되었습니다.");
    }

    @Operation(summary = "테스트 사용자 생성", description = "원활한 테스트를 위한 임시 사용자를 생성합니다. (이메일 인증 생략)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "테스트 사용자 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일입니다"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/signup-test-user")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signupTestUser(
        @RequestBody @Valid TestSignupRequest request
    ) {
        String userId = authService.signupTestUser(
            request.email(), 
            request.password(), 
            request.nickname(), 
            request.description()
        );
        
        return new SignupResponse(true, "테스트 사용자가 성공적으로 생성되었습니다.");
    }
}
