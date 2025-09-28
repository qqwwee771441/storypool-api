package com.wudc.storypool.global.security.exception;

import com.wudc.storypool.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TokenExpiredException extends BaseSecurityException {
    
    public TokenExpiredException(
        HttpServletRequest req,
        HttpServletResponse res
    ) {
        super(ErrorCode.JWT_EXPIRED, res, req);
    }
}