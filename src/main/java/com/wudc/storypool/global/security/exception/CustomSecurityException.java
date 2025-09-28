package com.wudc.storypool.global.security.exception;

import com.wudc.storypool.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomSecurityException extends BaseSecurityException {
    
    public CustomSecurityException(
        HttpServletResponse res,
        HttpServletRequest req,
        ErrorCode errorCode
    ) {
        super(errorCode, res, req);
    }
}