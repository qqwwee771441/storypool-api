package com.wudc.storypool.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.domain.user.controller.request.TestSignupRequest;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("POST /signup-test-user 테스트 사용자 생성 테스트")
public class TestSignupTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthService authService;

    @DisplayName("성공")
    @Test
    public void testSignupSuccess() throws Exception {
        // given
        when(authService.signupTestUser("test@test.com", "password123!", "testNickname", "test description"))
            .thenReturn("testUserId");

        // when
        TestSignupRequest request = new TestSignupRequest(
            "test@test.com",
            "password123!",
            "testNickname",
            "test description"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/signup-test-user")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("테스트 사용자가 성공적으로 생성되었습니다."));
    }

    @DisplayName("이메일 형식 오류")
    @Test
    public void testSignupWithInvalidEmail() throws Exception {
        // when
        TestSignupRequest request = new TestSignupRequest(
            "invalid-email",
            "password123!",
            "testNickname",
            "test description"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/signup-test-user")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("이메일 형식에 맞지 않습니다."));
    }

    @DisplayName("비밀번호 형식 오류")
    @Test
    public void testSignupWithInvalidPassword() throws Exception {
        // when
        TestSignupRequest request = new TestSignupRequest(
            "test@test.com",
            "123",
            "testNickname",
            "test description"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/signup-test-user")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("비밀번호는 8자 이상, 영문·숫자·특수문자 조합이어야 합니다."));
    }

    @DisplayName("빈 닉네임으로 요청")
    @Test
    public void testSignupWithEmptyNickname() throws Exception {
        // when
        TestSignupRequest request = new TestSignupRequest(
            "test@test.com",
            "password123!",
            "",
            "test description"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/signup-test-user")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("닉네임은 필수입니다."));
    }
}