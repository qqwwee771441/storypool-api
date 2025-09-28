package com.wudc.storypool.common.util;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.global.security.principal.PrincipalDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {
    public static String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated())
            throw new BaseException(ErrorCode.UNAUTHORIZED);

        if (authentication.getPrincipal() == "anonymousUser") {
            throw new BaseException(ErrorCode.UNAUTHORIZED);
        }

        return ((PrincipalDetails) authentication.getPrincipal()).getUser().getId();
    }
}
