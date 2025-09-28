package com.wudc.storypool.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.domain.user.controller.request.WithdrawalRequest;
import com.wudc.storypool.domain.user.service.AuthService;
import com.wudc.storypool.common.util.AuthUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("DELETE /withdrawal 회원 탈퇴 테스트")
public class WithdrawalTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthService authService;

    @DisplayName("성공")
    @Test
    public void withdrawalSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            doNothing().when(authService).withdrawUser("userId", "password123!", "accessToken", "refreshToken");

            // when
            WithdrawalRequest request = new WithdrawalRequest(
                "password123!",
                "accessToken",
                "refreshToken"
            );

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/api/auth/withdrawal")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType("application/json");

            // then
            mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴가 완료되었습니다."));
        }
    }

    @DisplayName("빈 비밀번호로 요청")
    @Test
    public void withdrawalWithEmptyPassword() throws Exception {
        // when
        WithdrawalRequest request = new WithdrawalRequest(
            "",
            "accessToken",
            "refreshToken"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/api/auth/withdrawal")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("비밀번호는 1자 이상 100자 이하여야 합니다."));
    }

    @DisplayName("빈 액세스 토큰으로 요청")
    @Test
    public void withdrawalWithEmptyAccessToken() throws Exception {
        // when
        WithdrawalRequest request = new WithdrawalRequest(
            "password123!",
            "",
            "refreshToken"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/api/auth/withdrawal")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("액세스 토큰 입력 값이 비어있습니다."));
    }

    @DisplayName("빈 리프레시 토큰으로 요청")
    @Test
    public void withdrawalWithEmptyRefreshToken() throws Exception {
        // when
        WithdrawalRequest request = new WithdrawalRequest(
            "password123!",
            "accessToken",
            ""
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/api/auth/withdrawal")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("리프레시 토큰 입력 값이 비어있습니다."));
    }
}