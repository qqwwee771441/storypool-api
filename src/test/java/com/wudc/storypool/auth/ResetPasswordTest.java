package com.wudc.storypool.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.domain.user.controller.request.ResetPasswordRequest;
import com.wudc.storypool.domain.user.service.AuthService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("POST /reset-password 비밀번호 재설정 테스트")
public class ResetPasswordTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthService authService;

    @DisplayName("성공")
    @Test
    public void resetPasswordSuccess() throws Exception {
        // given
        doNothing().when(authService).resetPassword("test@test.com", "emailToken123", "newPassword1!");

        // when
        ResetPasswordRequest request = new ResetPasswordRequest(
            "test@test.com",
            "emailToken123",
            "newPassword1!"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/reset-password")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("비밀번호가 성공적으로 변경되었습니다."));
    }

    @DisplayName("이메일 형식 오류")
    @Test
    public void resetPasswordWithInvalidEmail() throws Exception {
        // when
        ResetPasswordRequest request = new ResetPasswordRequest(
            "invalid-email",
            "emailToken123",
            "newPassword1!"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/reset-password")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("이메일 형식에 맞지 않습니다."));
    }

    @DisplayName("빈 이메일 토큰으로 요청")
    @Test
    public void resetPasswordWithEmptyEmailToken() throws Exception {
        // when
        ResetPasswordRequest request = new ResetPasswordRequest(
            "test@test.com",
            "",
            "newPassword1!"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/reset-password")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("이메일 토큰 입력 값이 비어있습니다."));
    }

    @DisplayName("비밀번호 형식 오류")
    @Test
    public void resetPasswordWithInvalidPasswordFormat() throws Exception {
        // when
        ResetPasswordRequest request = new ResetPasswordRequest(
            "test@test.com",
            "emailToken123",
            "123"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/reset-password")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("비밀번호는 최소 8자 이상, 영문 대소문자·숫자·특수문자를 모두 포함해야 합니다."));
    }
}