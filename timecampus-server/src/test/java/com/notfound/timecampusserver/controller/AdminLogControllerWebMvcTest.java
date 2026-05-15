package com.notfound.timecampusserver.controller;

import com.notfound.timecampuspojo.entity.LogEntity;
import com.notfound.timecampusserver.controller.admin.AdminLogController;
import com.notfound.timecampusserver.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminLogControllerWebMvcTest {

    private MockMvc mockMvc;
    private LogService logService;

    @BeforeEach
    void setUp() {
        logService = mock(LogService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminLogController(logService)).build();
    }

    @Test
    void listLogsUsesVersionedAdminApi() throws Exception {
        LogEntity log = new LogEntity();
        log.setId(1L);
        log.setOperatorType("ADMIN");
        log.setType("review");
        log.setAction("approve_ugc");
        log.setTargetType("media");
        log.setTargetId(7L);
        log.setCreateTime(LocalDateTime.of(2026, 5, 2, 20, 0));
        when(logService.list("ADMIN", "review", null, null, 50)).thenReturn(List.of(log));

        mockMvc.perform(get("/api/v1/admin/logs")
                        .param("operatorType", "ADMIN")
                        .param("type", "review")
                        .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].action").value("approve_ugc"))
                .andExpect(jsonPath("$.data[0].targetId").value(7));
    }
}
