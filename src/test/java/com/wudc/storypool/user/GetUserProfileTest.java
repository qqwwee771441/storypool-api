package com.wudc.storypool.user;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.user.controller.response.UserProfileResponse;
import com.wudc.storypool.domain.user.service.QueryUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("GET /api/users/me 사용자 프로필 조회 테스트")
public class GetUserProfileTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private QueryUserService queryUserService;

    @DisplayName("사용자 프로필 조회 성공")
    @Test
    public void getUserProfileSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            UserProfileResponse mockResponse = new UserProfileResponse(
                "test@test.com",
                "Test User",
                "profileImageUrl",
                "Test description"
            );
            
            when(queryUserService.findProfileById("userId"))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("Test User"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.profileImageUrl").value("profileImageUrl"));
        }
    }
}