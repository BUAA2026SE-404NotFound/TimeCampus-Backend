package com.notfound.timetrackserver.controller;

import com.notfound.timetrackpojo.entity.MediaEntity;
import com.notfound.timetrackpojo.vo.PoiVO;
import com.notfound.timetrackpojo.vo.UserProfileVO;
import com.notfound.timetrackserver.controller.user.ContentController;
import com.notfound.timetrackserver.controller.user.MeController;
import com.notfound.timetrackserver.controller.user.PoiController;
import com.notfound.timetrackserver.mapper.MediaMapper;
import com.notfound.timetrackserver.service.MediaFileService;
import com.notfound.timetrackserver.security.UserContext;
import com.notfound.timetrackserver.service.PoiService;
import com.notfound.timetrackserver.service.ReviewResultService;
import com.notfound.timetrackserver.service.UserService;
import com.notfound.timetrackserver.service.impl.MediaStructMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserApiRegressionTest {

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void publicPoiListUsesVersionedApiPrefix() throws Exception {
        PoiService poiService = mock(PoiService.class);
        PoiVO poi = new PoiVO();
        poi.setId(1L);
        poi.setName("主楼");
        when(poiService.list(1, "主楼")).thenReturn(List.of(poi));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PoiController(poiService)).build();

        mockMvc.perform(get("/api/v1/pois").param("keyword", "主楼"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("主楼"));
    }

    @Test
    void meRequiresUserContextAndReturnsUnifiedError() throws Exception {
        UserService userService = mock(UserService.class);
        ReviewResultService reviewResultService = mock(ReviewResultService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MeController(userService, reviewResultService))
                .setControllerAdvice(new com.notfound.timetrackcommon.exception.GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4010))
                .andExpect(jsonPath("$.message", containsString("missing user token")));
    }

    @Test
    void meReturnsCurrentUserWhenAuthenticated() throws Exception {
        UserService userService = mock(UserService.class);
        ReviewResultService reviewResultService = mock(ReviewResultService.class);
        UserContext.setUserId(9L);
        UserProfileVO profile = new UserProfileVO();
        profile.setId(9L);
        profile.setNickname("student");
        when(userService.getById(9L)).thenReturn(profile);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MeController(userService, reviewResultService)).build();

        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(9))
                .andExpect(jsonPath("$.data.nickname").value("student"));
    }

    @Test
    void timeSwitchReturnsBestApprovedMedia() throws Exception {
        MediaMapper mediaMapper = mock(MediaMapper.class);
        MediaEntity entity = new MediaEntity();
        entity.setId(3L);
        entity.setPoiId(1L);
        entity.setType("official");
        entity.setImagePath("/img.jpg");
        entity.setYear(2001);
        entity.setReviewStatus("approved");
        when(mediaMapper.findBestByPoiAndYear(1L, 2000, null, "approved")).thenReturn(entity);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ContentController(mediaMapper, new MediaStructMapper(mock(MediaFileService.class)))).build();

        mockMvc.perform(get("/api/v1/pois/1/time-switch").param("year", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.year").value(2001));
    }
}
