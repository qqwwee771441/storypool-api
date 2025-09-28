package com.wudc.storypool.global.security.exception;

import com.wudc.storypool.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ForbiddenException extends BaseSecurityException {
    
    public ForbiddenException(
        HttpServletResponse response,
        HttpServletRequest request
    ) {
        super(ErrorCode.FORBIDDEN, response, request);
    }
}