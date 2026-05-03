package com.notfound.timetrackserver.controller;

import com.notfound.timetrackpojo.vo.AdminMapOverviewVO;
import com.notfound.timetrackpojo.vo.AdminMapPoiVO;
import com.notfound.timetrackserver.config.TencentMapProperties;
import com.notfound.timetrackserver.controller.admin.AdminMapController;
import com.notfound.timetrackserver.service.AdminMapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminMapControllerWebMvcTest {

    private MockMvc mockMvc;
    private AdminMapService adminMapService;

    @BeforeEach
    void setUp() {
        adminMapService = mock(AdminMapService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminMapController(
                adminMapService,
                new TencentMapProperties("test-map-key", "test-sk", null, null)
        )).build();
    }

    @Test
    void overviewUsesVersionedAdminApi() throws Exception {
        AdminMapPoiVO poi = new AdminMapPoiVO();
        poi.setId(1L);
        poi.setName("主楼");
        poi.setLatitude(new BigDecimal("39.90420000"));
        poi.setLongitude(new BigDecimal("116.40740000"));
        poi.setFavoriteCount(3);
        poi.setCommentCount(2);

        AdminMapOverviewVO overview = new AdminMapOverviewVO();
        overview.setPois(List.of(poi));
        when(adminMapService.overview(1, "主", "pending", 20)).thenReturn(overview);

        mockMvc.perform(get("/api/v1/admin/map/overview")
                        .param("status", "1")
                        .param("keyword", "主")
                        .param("commentStatus", "pending")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.pois[0].name").value("主楼"))
                .andExpect(jsonPath("$.data.pois[0].favoriteCount").value(3));
    }

    @Test
    void configReturnsTencentMapKeyOnly() throws Exception {
        mockMvc.perform(get("/api/v1/admin/map/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.tencentMapKey").value("test-map-key"));
    }
}
