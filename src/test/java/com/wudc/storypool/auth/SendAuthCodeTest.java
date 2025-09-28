package com.wudc.storypool.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.domain.user.controller.request.SendAuthCodeRequest;
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
@DisplayName("POST /send-code 인증코드 발송 테스트")
public class SendAuthCodeTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthService authService;

    @DisplayName("성공")
    @Test
    public void sendAuthCodeSuccess() throws Exception {
        // given
        when(authService.sendCodeByEmail("test@test.com")).thenReturn(300);

        // when
        SendAuthCodeRequest request = new SendAuthCodeRequest("test@test.com");

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/send-code")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.seconds").value(300));
    }

    @DisplayName("이메일 형식 오류")
    @Test
    public void sendAuthCodeWithInvalidEmail() throws Exception {
        // when
        SendAuthCodeRequest request = new SendAuthCodeRequest("invalid-email");

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/send-code")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("이메일 형식에 맞지 않습니다."));
    }

    @DisplayName("빈 이메일로 요청")
    @Test
    public void sendAuthCodeWithEmptyEmail() throws Exception {
        // when
        SendAuthCodeRequest request = new SendAuthCodeRequest("");

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/send-code")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("이메일 입력 값이 비어있습니다."));
    }
}