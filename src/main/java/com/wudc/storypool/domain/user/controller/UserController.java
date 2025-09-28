package com.wudc.storypool.domain.user.controller;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.user.controller.request.UpdatePasswordRequest;
import com.wudc.storypool.domain.user.controller.request.UpdateUserProfileRequest;
    import com.wudc.storypool.domain.user.controller.response.DeleteProfileImageResponse;
import com.wudc.storypool.domain.user.controller.response.UserProfileResponse;
import com.wudc.storypool.domain.user.service.CommandUserService;
import com.wudc.storypool.domain.user.service.QueryUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final QueryUserService queryUserService;
    private final CommandUserService commandUserService;

    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 프로필 조회 성공"),
        @ApiResponse(responseCode = "401", description = "토큰이 없거나 유효하지 않음"),
        @ApiResponse(responseCode = "403", description = "탈퇴된 사용자 또는 권한 없는 접근"),
        @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponse findByProfile() {
        return queryUserService.findProfileById(AuthUtil.getUserId());
    }

    @Operation(summary = "내 프로필 수정", description = "닉네임, 프로필 이미지, 자기소개를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파라미터 형식 또는 유효성 실패"),
        @ApiResponse(responseCode = "401", description = "액세스 토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임입니다"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    public void updateProfile(
        @RequestBody @Valid UpdateUserProfileRequest request
    ) {
        commandUserService.updateProfileById(AuthUtil.getUserId(), request);
    }

    @Operation(summary = "특정 사용자 프로필 조회", description = "사용자 ID로 특정 사용자의 프로필을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "프로필 정상 반환"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자이거나 탈퇴한 사용자입니다"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponse findUserProfile(
        @PathVariable(name = "user-id") String targetUserId
    ) {
        return queryUserService.findProfileById(targetUserId);
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새로운 비밀번호로 변경합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
        @ApiResponse(responseCode = "400", description = "새 비밀번호 검증 실패"),
        @ApiResponse(responseCode = "401", description = "로그인이 필요합니다"),
        @ApiResponse(responseCode = "403", description = "기존 비밀번호가 올바르지 않거나 탈퇴한 사용자입니다"),
        @ApiResponse(responseCode = "409", description = "새 비밀번호는 기존 비밀번호와 달라야 합니다"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/me/change-password")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(
        @RequestBody @Valid UpdatePasswordRequest request
    ) {
        commandUserService.updatePasswordById(AuthUtil.getUserId(), request.newPassword(), request.password());
    }
}
