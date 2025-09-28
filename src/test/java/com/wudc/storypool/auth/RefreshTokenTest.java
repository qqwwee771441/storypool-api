package com.wudc.storypool.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.domain.user.controller.request.RefreshTokenRequest;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("토큰 갱신")
public class RefreshTokenTest {

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
    public void refreshTokenSuccess() throws Exception {
        // given
        when(jwtParser.getUserIdByToken("refreshToken", Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getRefreshSecret()))))
            .thenReturn("userId");
        when(jwtParser.getUserIdByToken("accessToken", Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getAccessSecret()))))
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
        RefreshTokenRequest request = new RefreshTokenRequest(
            "accessToken",
            "refreshToken"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auth/refresh")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }
}
