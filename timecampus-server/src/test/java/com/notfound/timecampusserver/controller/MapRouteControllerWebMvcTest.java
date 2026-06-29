package com.notfound.timecampusserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.timecampusserver.controller.user.MapRouteController;
import com.notfound.timecampusserver.service.TencentRouteService;
import com.notfound.timecampusserver.service.TencentRouteService.RoutePoint;
import com.notfound.timecampusserver.service.TencentRouteService.WalkingRouteLeg;
import com.notfound.timecampusserver.service.TencentRouteService.WalkingRoutePlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MapRouteControllerWebMvcTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private TencentRouteService routeService;

    @BeforeEach
    void setUp() {
        routeService = mock(TencentRouteService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MapRouteController(routeService))
                .build();
    }

    @Test
    void walkingRouteUsesVersionedPublicApi() throws Exception {
        RoutePoint from = new RoutePoint("主楼", 39.981, 116.34);
        RoutePoint to = new RoutePoint("图书馆", 39.982, 116.341);
        when(routeService.planWalkingRoute(any())).thenReturn(new WalkingRoutePlan(
                "walking",
                "tencent-map",
                320,
                260,
                List.of(new WalkingRouteLeg(from, to, 320, 260, List.of(), List.of(), Map.of()))
        ));

        mockMvc.perform(post("/api/v1/map/walking-route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "points", List.of(
                                        Map.of("name", "主楼", "lat", 39.981, "lng", 116.34),
                                        Map.of("name", "图书馆", "lat", 39.982, "lng", 116.341)
                                )
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.mode").value("walking"))
                .andExpect(jsonPath("$.data.totalDistanceMeters").value(320))
                .andExpect(jsonPath("$.data.legs[0].from.name").value("主楼"))
                .andExpect(jsonPath("$.data.legs[0].to.name").value("图书馆"));
    }
}
