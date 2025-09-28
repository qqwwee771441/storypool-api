package com.wudc.storypool.notification;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.notification.controller.response.NotificationListResponse;
import com.wudc.storypool.domain.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("GET /api/notifications 알림 목록 조회 테스트")
public class GetNotificationsTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private NotificationService notificationService;

    @DisplayName("알림 목록 조회 성공")
    @Test
    public void getNotificationsSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            NotificationListResponse.NotificationItem notificationItem = 
                new NotificationListResponse.NotificationItem(
                    "notificationId",
                    "COMMENT",
                    "새로운 댓글이 있습니다.",
                    "postId",
                    false,
                    Instant.now()
                );
            
            NotificationListResponse mockResponse = new NotificationListResponse(
                List.of(notificationItem),
                true,
                "nextCursor"
            );
            
            when(notificationService.getNotifications(eq("userId"), eq(20), isNull(), eq(false)))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications").isArray())
                .andExpect(jsonPath("$.notifications[0].id").value("notificationId"))
                .andExpect(jsonPath("$.notifications[0].type").value("COMMENT"))
                .andExpect(jsonPath("$.notifications[0].message").value("새로운 댓글이 있습니다."))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value("nextCursor"));
        }
    }

    @DisplayName("페이지네이션이 있는 경우")
    @Test
    public void getNotificationsWithPagination() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            NotificationListResponse mockResponse = new NotificationListResponse(
                List.of(),
                false,
                null
            );
            
            when(notificationService.getNotifications(eq("userId"), eq(10), eq("cursor123"), eq(false)))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/notifications")
                    .param("cursor", "cursor123")
                    .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications").isArray())
                .andExpect(jsonPath("$.hasNext").value(false));
        }
    }
}