package com.notfound.timecampusserver.controller;

import com.notfound.timecampusserver.controller.admin.AdminContentController;
import com.notfound.timecampusserver.controller.admin.AdminCommentController;
import com.notfound.timecampusserver.controller.admin.AdminLogController;
import com.notfound.timecampusserver.controller.admin.AdminMapController;
import com.notfound.timecampusserver.controller.admin.AdminMediaController;
import com.notfound.timecampusserver.controller.admin.AdminUgcController;
import com.notfound.timecampusserver.config.TencentMapProperties;
import com.notfound.timecampusserver.controller.user.AuthController;
import com.notfound.timecampusserver.controller.user.CommentController;
import com.notfound.timecampusserver.controller.user.ContentController;
import com.notfound.timecampusserver.controller.user.FavoriteController;
import com.notfound.timecampusserver.controller.user.MapController;
import com.notfound.timecampusserver.controller.user.MediaFileController;
import com.notfound.timecampusserver.controller.user.MeController;
import com.notfound.timecampusserver.controller.user.PoiController;
import com.notfound.timecampusserver.controller.user.TimelineController;
import com.notfound.timecampusserver.controller.user.UgcController;
import com.notfound.timecampusserver.controller.user.UserController;
import com.notfound.timecampusserver.mapper.MediaMapper;
import com.notfound.timecampusserver.service.AdminMediaService;
import com.notfound.timecampusserver.service.AdminMapService;
import com.notfound.timecampusserver.service.CommentService;
import com.notfound.timecampusserver.service.FavoriteService;
import com.notfound.timecampusserver.service.LogService;
import com.notfound.timecampusserver.service.MapService;
import com.notfound.timecampusserver.service.MediaFileService;
import com.notfound.timecampusserver.service.PoiService;
import com.notfound.timecampusserver.service.ReviewResultService;
import com.notfound.timecampusserver.service.TimelineService;
import com.notfound.timecampusserver.service.UgcService;
import com.notfound.timecampusserver.service.UserService;
import com.notfound.timecampusserver.security.UserAuthInterceptor;
import com.notfound.timecampusserver.service.impl.MediaStructMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;

class ControllerMappingRegressionTest {

    @Test
    void userAndAdminControllersHaveNoAmbiguousMappings() {
        UserService userService = mock(UserService.class);
        FavoriteService favoriteService = mock(FavoriteService.class);
        ReviewResultService reviewResultService = mock(ReviewResultService.class);
        CommentService commentService = mock(CommentService.class);
        PoiService poiService = mock(PoiService.class);
        MediaMapper mediaMapper = mock(MediaMapper.class);
        MediaFileService mediaFileService = mock(MediaFileService.class);
        MapService mapService = mock(MapService.class);
        UgcService ugcService = mock(UgcService.class);
        TimelineService timelineService = mock(TimelineService.class);
        UserAuthInterceptor userAuthInterceptor = mock(UserAuthInterceptor.class);
        AdminMediaService adminMediaService = mock(AdminMediaService.class);
        AdminMapService adminMapService = mock(AdminMapService.class);
        LogService logService = mock(LogService.class);

        MockMvcBuilders.standaloneSetup(
                new AuthController(userService),
                new UserController(userService, favoriteService, reviewResultService),
                new MeController(userService, reviewResultService),
                new FavoriteController(favoriteService),
                new CommentController(commentService),
                new PoiController(poiService),
                new ContentController(mediaMapper, new MediaStructMapper(mediaFileService)),
                new MapController(mapService),
                new TimelineController(timelineService, userAuthInterceptor),
                new MediaFileController(mediaFileService),
                new UgcController(ugcService),
                new AdminContentController(adminMediaService),
                new AdminMapController(adminMapService, new TencentMapProperties("test-map-key", "test-sk", null, null)),
                new AdminMediaController(adminMediaService, mediaFileService),
                new AdminCommentController(commentService),
                new AdminUgcController(ugcService),
                new AdminLogController(logService)
        ).build();
    }
}
