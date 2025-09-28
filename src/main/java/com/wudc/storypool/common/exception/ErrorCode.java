package com.wudc.storypool.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    UNAUTHORIZED("로그인이 필요한 작업입니다.", HttpStatus.UNAUTHORIZED, "B4010"),
    FORBIDDEN("접근 권한이 없습니다.", HttpStatus.FORBIDDEN, "B4030"),
    INTERNAL_ERROR("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, "B5000"),

    // USER
    USER_NOT_FOUND("user not found", HttpStatus.NOT_FOUND, "U4040"),

    // Auth
    CANT_SEND_EMAIL("failed to send email", HttpStatus.INTERNAL_SERVER_ERROR, "A5001"),
    TOO_MANY_REQUESTS("Too many requests. Try again later.", HttpStatus.TOO_MANY_REQUESTS, "A4291"),
    INVALID_OR_EXPIRED_CODE("Invalid or expired code", HttpStatus.UNAUTHORIZED, "A4011"),
    INVALID_EMAIL_TOKEN("유효하지 않은 인증 토큰입니다.", HttpStatus.UNAUTHORIZED, "A4012"),
    EMAIL_ALREADY_EXISTS("이미 사용 중인 이메일 주소입니다.", HttpStatus.CONFLICT, "A4091"),
    INVALID_PASSWORD_FORMAT("비밀번호는 8자 이상, 영문·숫자·특수문자 조합이어야 합니다.", HttpStatus.UNPROCESSABLE_ENTITY, "A4221"),
    INVALID_LOGIN_CREDENTIALS("이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED, "A4013"),
    ACCOUNT_LOCKED("계정이 잠겼습니다. 고객센터에 문의하세요.", HttpStatus.LOCKED, "A4231"),
    INVALID_REFRESH_TOKEN("유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED, "A4014"),
    TOKEN_MISMATCH("토큰 불일치로 갱신할 수 없습니다.", HttpStatus.FORBIDDEN, "A4031"),
    REFRESH_TOKEN_NOT_FOUND("삭제할 리프레시 토큰을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "A4041"),
    INVALID_PASSWORD("비밀번호가 일치하지 않습니다.", HttpStatus.FORBIDDEN, "A4032"),
    ACCOUNT_DELETED("탈퇴한 회원입니다.", HttpStatus.FORBIDDEN, "A4033"),
    
    // Device
    DEVICE_NOT_FOUND("Device not found or access denied.", HttpStatus.NOT_FOUND, "D4041"),
    DEVICE_ALREADY_REGISTERED("Device is already registered.", HttpStatus.CONFLICT, "D4091"),
    
    // Story
    STORY_NOT_FOUND("스토리 초안을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "S4041"),
    
    // Fairytale
    FAIRYTALE_NOT_FOUND("해당 동화를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "F4041"),
    FAIRYTALE_HAS_POSTS("동화에 연결된 게시글이 존재하여 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST, "F4001"),
    
    // Post
    POST_NOT_FOUND("게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "P4041"),
    NO_AUTHORIZATION("권한이 없습니다.", HttpStatus.FORBIDDEN, "P4031"),
    
    // Comment
    COMMENT_NOT_FOUND("댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "C4041"),
    COMMENT_ACCESS_DENIED("댓글에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN, "C4031"),

    // Notice
    INVALID_PARAMETER("limit은 1~100 사이여야 합니다.", HttpStatus.NOT_FOUND, "N4001"),
    
    // Resource
    RESOURCE_NOT_FOUND("요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "R4041"),
    NOTIFICATION_NOT_FOUND("알람을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "N4041"),
    CANT_CONNECT_LLM("LLM 서버에 연결하지 못했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR , "N5001" ),

    // Jwt
    TOKEN_NOT_VALID("토큰 값이 정상적이지 않습니다.", HttpStatus.BAD_REQUEST, "TK400"),
    JWT_EXPIRED("토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED, "TK4011"),
    
    // Upload/S3
    S3_UPLOAD_ERROR("파일 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "UP5001"),
    S3_DELETE_ERROR("파일 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "UP5002"),
    INVALID_FILE_TYPE("지원되지 않는 파일 형식입니다.", HttpStatus.BAD_REQUEST, "UP4001"),
    FILE_NOT_FOUND("파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "UP4041"),
    ACCESS_DENIED("파일에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN, "UP4031"),
    FAIRYTALE_IMAGE_DELETE_DENIED("동화 이미지는 동화 삭제 API를 통해서만 삭제할 수 있습니다.", HttpStatus.FORBIDDEN, "UP4032"),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR, "UP5000"),;


    private final String message;
    private final HttpStatus status;
    private final String code;
}
