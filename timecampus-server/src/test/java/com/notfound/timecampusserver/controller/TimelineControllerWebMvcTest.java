package com.notfound.timecampusserver.controller;

import com.notfound.timecampuspojo.vo.TimelineItemVO;
import com.notfound.timecampusserver.controller.user.TimelineController;
import com.notfound.timecampusserver.security.UserAuthInterceptor;
import com.notfound.timecampusserver.service.TimelineService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TimelineControllerWebMvcTest {

    @Test
    void timelineReturnsUnifiedResponse() throws Exception {
        TimelineService timelineService = mock(TimelineService.class);
        UserAuthInterceptor userAuthInterceptor = mock(UserAuthInterceptor.class);

        TimelineItemVO item = new TimelineItemVO();
        item.setId(123L);
        item.setType("official");
        item.setPoiId(1L);
        item.setPoiName("主楼");
        item.setYear(2020);
        item.setPreviewUrl("/api/v1/media/123/file");

        when(userAuthInterceptor.resolveUserId(org.mockito.Mockito.any())).thenReturn(null);
        when(timelineService.list(isNull(), eq(2020), eq(2022), eq("all"), isNull()))
                .thenReturn(List.of(item));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TimelineController(timelineService, userAuthInterceptor))
                .setControllerAdvice(new com.notfound.timecampuscommon.exception.GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/timeline")
                        .param("startYear", "2020")
                        .param("endYear", "2022"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(123))
                .andExpect(jsonPath("$.data[0].type").value("official"))
                .andExpect(jsonPath("$.data[0].poiName").value("主楼"))
                .andExpect(jsonPath("$.data[0].previewUrl").value("/api/v1/media/123/file"));
    }
}
