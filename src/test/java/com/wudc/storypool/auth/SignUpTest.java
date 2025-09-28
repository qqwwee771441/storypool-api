package com.wudc.storypool.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.domain.user.controller.request.SignupRequest;
import com.wudc.storypool.domain.user.service.EmailTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@DisplayName("POST /signup 회원가입 테스트")
@Transactional
public class SignUpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailTokenService emailTokenService;
    @Autowired
    private DefaultAuthenticationEventPublisher authenticationEventPublisher;

    @Test
    @DisplayName("성공")
    public void signupSuccess() throws Exception {
        // given
        when(emailTokenService.validateEmailToken("test@test.com", "test")).thenReturn(true);

        // when
        SignupRequest request = new SignupRequest(
            "test@test.com",
            "test",
            "test1234!"
        );

        // then
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/signup")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        mockMvc.perform(requestBuilder)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));
    }

    @Test
    @DisplayName("토큰 검증 실패")
    public void emailTokenValidationFailed() throws Exception {
        // given
        when(emailTokenService.validateEmailToken("test@test.com", "test"))
            .thenReturn(false);

        // when
        SignupRequest request = new SignupRequest(
            "test@test.com",
            "token",
            "test1234!"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/signup")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("유효하지 않은 인증 토큰입니다."));
    }

    @DisplayName("이메일 형식 오류")
    @Test
    public void emailFormatError() throws Exception {
        // given
        when(emailTokenService.validateEmailToken("test@test.com", "test"))
            .thenReturn(false);

        // when
        SignupRequest request = new SignupRequest(
            "test",
            "test",
            "test1234!"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/signup")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("이메일 형식에 맞지 않습니다."));
    }
}
