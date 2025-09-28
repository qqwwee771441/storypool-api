package com.wudc.storypool.global.security.exception;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Getter
public class BaseSecurityException extends BaseException { // BasicException을 상속받도록 변경
    private final HttpServletResponse res;
    private final HttpServletRequest req; // HttpServletRequest 필드 추가

    public BaseSecurityException(ErrorCode errorCode, HttpServletResponse res, HttpServletRequest req) {
        super(errorCode);
        this.res = res;
        this.req = req;
    }
}