package com.wudc.storypool.fairytale;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.fairytale.entity.Fairytale;
import com.wudc.storypool.domain.fairytale.entity.constant.FairytaleStatus;
import com.wudc.storypool.domain.fairytale.service.FairytaleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("GET /api/fairytales/{fairytaleId}/status 동화 상태 조회 테스트")
public class GetFairytaleStatusTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private FairytaleService fairytaleService;

    @DisplayName("동화 상태 조회 성공")
    @Test
    public void getFairytaleStatusSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            Fairytale mockFairytale = new Fairytale();
            mockFairytale.setId("fairytaleId");
            mockFairytale.setStatus(FairytaleStatus.COMPLETED);
            mockFairytale.setMessage("Generation completed successfully");
            
            when(fairytaleService.getFairytaleStatus("userId", "fairytaleId"))
                .thenReturn(mockFairytale);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/fairytales/fairytaleId/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.message").value("Generation completed successfully"));
        }
    }
}