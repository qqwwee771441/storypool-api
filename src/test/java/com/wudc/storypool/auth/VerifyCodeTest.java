package com.wudc.storypool.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.domain.user.controller.request.VerifyCodeRequest;
import com.wudc.storypool.domain.user.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Duration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("인증코드 검증")
public class VerifyCodeTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("성공")
    public void verifyCodeSuccess() throws Exception {
        // given
        redisTemplate.opsForValue().set("auth_code:test@test.com", "123456", Duration.ofMinutes(1L));

        // when
        VerifyCodeRequest request = new VerifyCodeRequest(
            "test@test.com",
            "123456"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/verify-code")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.emailToken").isNotEmpty());
    }
}
