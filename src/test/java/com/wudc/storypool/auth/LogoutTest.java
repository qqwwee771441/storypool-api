package com.wudc.storypool.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.domain.user.controller.request.LogoutRequest;
import com.wudc.storypool.domain.user.entity.User;
import com.wudc.storypool.domain.user.repository.UserRepository;
import com.wudc.storypool.domain.user.service.TokenStorageService;
import com.wudc.storypool.global.security.jwt.JwtParser;
import com.wudc.storypool.global.security.jwt.JwtProperties;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("POST /logout 로그아웃 테스트")
public class LogoutTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private JwtParser jwtParser;
    @MockitoBean
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private TokenStorageService tokenStorageService;
    @Autowired
    private JwtProperties jwtProperties;

    @DisplayName("성공")
    @Test
    public void logoutSuccess() throws Exception {
        // given
        when(jwtParser.getUserIdByToken("accessToken", Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getAccessSecret()))))
            .thenReturn("userId");
        when(jwtParser.getUserIdByToken("refreshToken", Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getRefreshSecret()))))
            .thenReturn("userId");
        
        User testUser = User.createUser(
            "test@test.com",
            passwordEncoder.encode("test1234!"),
            "test",
            ""
        );
        when(userRepository.findById("userId")).thenReturn(Optional.of(testUser));
        when(tokenStorageService.isValidRefreshToken("refreshToken", "test@test.com")).thenReturn(true);

        // when
        LogoutRequest request = new LogoutRequest(
            "accessToken",
            "refreshToken"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/logout")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("로그아웃되었습니다."));
    }

    @DisplayName("유효하지 않은 토큰으로 로그아웃 시도")
    @Test
    public void logoutWithInvalidToken() throws Exception {
        // given
        when(jwtParser.getUserIdByToken("invalidAccessToken", Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getAccessSecret()))))
            .thenReturn(null);

        // when
        LogoutRequest request = new LogoutRequest(
            "invalidAccessToken",
            "invalidRefreshToken"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/logout")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("로그인이 필요한 작업입니다."));
    }

    @DisplayName("빈 리프레시 토큰으로 로그아웃 시도")
    @Test
    public void logoutWithEmptyRefreshToken() throws Exception {
        // when
        LogoutRequest request = new LogoutRequest(
            "accessToken",
            ""
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/logout")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("리프레시 토큰 입력 값이 비어있습니다."));
    }

    @DisplayName("빈 엑세스 토큰으로 로그아웃 시도")
    @Test
    public void logoutWithEmptyToken() throws Exception {
        // when
        LogoutRequest request = new LogoutRequest(
            "",
            "refresh"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/logout")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("액세스 토큰 입력 값이 비어있습니다."));
    }
}