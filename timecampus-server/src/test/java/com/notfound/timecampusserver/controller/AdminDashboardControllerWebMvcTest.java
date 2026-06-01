package com.notfound.timecampusserver.controller;

import com.notfound.timecampuspojo.vo.AdminDashboardMetricVO;
import com.notfound.timecampuspojo.vo.AdminDashboardStatsVO;
import com.notfound.timecampusserver.controller.admin.AdminDashboardController;
import com.notfound.timecampusserver.service.AdminDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminDashboardControllerWebMvcTest {

    private MockMvc mockMvc;
    private AdminDashboardService adminDashboardService;

    @BeforeEach
    void setUp() {
        adminDashboardService = mock(AdminDashboardService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminDashboardController(adminDashboardService))
                .build();
    }

    @Test
    void statsUsesVersionedAdminApi() throws Exception {
        AdminDashboardMetricVO metric = new AdminDashboardMetricVO();
        metric.setLabel("POI 总数");
        metric.setValue("3");
        metric.setDetail("活跃 POI 3 个");
        metric.setTrend("来自当前 POI 表");

        AdminDashboardStatsVO stats = new AdminDashboardStatsVO();
        stats.setMetrics(List.of(metric));
        stats.setTrends(List.of());
        stats.setReviewDistribution(List.of());
        stats.setMediaTypeDistribution(List.of());
        when(adminDashboardService.stats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/admin/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.metrics[0].label").value("POI 总数"))
                .andExpect(jsonPath("$.data.metrics[0].value").value("3"));
    }
}
