package com.notfound.timecampusserver.controller;

import com.notfound.timecampuspojo.vo.MapHomeVO;
import com.notfound.timecampuspojo.vo.MapMediaVO;
import com.notfound.timecampuspojo.vo.MapPoiVO;
import com.notfound.timecampusserver.controller.publicapi.PortalMapController;
import com.notfound.timecampusserver.service.MapService;
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

class PortalMapControllerWebMvcTest {

    private MockMvc mockMvc;
    private MapService mapService;

    @BeforeEach
    void setUp() {
        mapService = mock(MapService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PortalMapController(mapService))
                .build();
    }

    @Test
    void homeUsesPublicPortalApi() throws Exception {
        MapMediaVO media = new MapMediaVO();
        media.setId(10L);
        media.setYear(2001);
        media.setPreviewUrl("/api/v1/media/10/file?accessToken=token");
        media.setThumbnailUrl("/api/v1/media/10/file?accessToken=token&size=192");

        MapPoiVO poi = new MapPoiVO();
        poi.setId(1L);
        poi.setName("主楼");
        poi.setLatitude(new BigDecimal("39.98404000"));
        poi.setLongitude(new BigDecimal("116.35112900"));
        poi.setAvailableYears(List.of(2001));
        poi.setMediaList(List.of(media));

        MapHomeVO home = new MapHomeVO();
        home.setPois(List.of(poi));
        when(mapService.getPortalHome(2001)).thenReturn(home);

        mockMvc.perform(get("/api/v1/portal/map/home").param("year", "2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.pois[0].name").value("主楼"))
                .andExpect(jsonPath("$.data.pois[0].mediaList[0].previewUrl").value("/api/v1/media/10/file?accessToken=token"));
    }
}
