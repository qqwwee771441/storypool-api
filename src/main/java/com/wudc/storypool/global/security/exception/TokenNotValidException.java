package com.wudc.storypool.global.security.exception;

import com.wudc.storypool.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TokenNotValidException extends BaseSecurityException {
    
    public TokenNotValidException(
        HttpServletResponse res,
        HttpServletRequest req
    ) {
        super(ErrorCode.TOKEN_NOT_VALID, res, req);
    }
}