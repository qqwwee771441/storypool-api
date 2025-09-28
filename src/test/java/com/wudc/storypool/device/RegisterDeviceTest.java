package com.wudc.storypool.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.user.controller.request.RegisterDeviceRequest;
import com.wudc.storypool.domain.user.entity.Device;
import com.wudc.storypool.domain.user.entity.constant.Platform;
import com.wudc.storypool.domain.user.service.DeviceService;
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


import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("POST /api/users/me/devices 디바이스 등록 테스트")
public class RegisterDeviceTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private DeviceService deviceService;

    @DisplayName("디바이스 등록 성공")
    @Test
    public void registerDeviceSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            Device mockDevice = new Device();
            mockDevice.setId("deviceId");
            mockDevice.setFcmToken("fcmToken123");
            mockDevice.setPlatform(Platform.ANDROID);
            
            when(deviceService.registerDevice(
                "userId",
                "deviceId123",
                "fcmToken123",
                Platform.ANDROID
            )).thenReturn(mockDevice);

            // when
            RegisterDeviceRequest request = new RegisterDeviceRequest(
                "deviceId123",
                "fcmToken123",
                Platform.ANDROID
            );

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/users/me/devices")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType("application/json");

            // then
            mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("deviceId"))
                .andExpect(jsonPath("$.fcmToken").value("fcmToken123"))
                .andExpect(jsonPath("$.platform").value("ANDROID"));
        }
    }

    @DisplayName("FCM 토큰이 빈 문자열로 요청")
    @Test
    public void registerDeviceWithEmptyFcmToken() throws Exception {
        // when
        RegisterDeviceRequest request = new RegisterDeviceRequest(
            "deviceId",
            "",
            Platform.ANDROID
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/users/me/devices")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("fcmToken은 필수입니다."));
    }

    @DisplayName("플랫폼이 null로 요청")
    @Test
    public void registerDeviceWithNullPlatform() throws Exception {
        // when
        RegisterDeviceRequest request = new RegisterDeviceRequest(
            "deviceId123",
            "fcmToken123",
            null
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/users/me/devices")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("platform은 필수입니다."));
    }
}