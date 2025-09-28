package com.wudc.storypool.domain.user.service;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.domain.user.controller.response.UserProfileResponse;
import com.wudc.storypool.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryUserService {

    private final UserRepository userRepository;

    public UserProfileResponse findProfileById(String userId) {
        return userRepository.findMyProfileById(userId)
            .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }
}
