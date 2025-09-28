package com.wudc.storypool.global.security.principal;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.domain.user.entity.User;
import com.wudc.storypool.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new BaseException(ErrorCode.USER_NOT_FOUND);
        }
        
        User user = userRepository.findById(username)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return new PrincipalDetails(user);
    }
}