package com.notfound.timetrackserver.controller;

import com.notfound.timetrackserver.controller.admin.AdminContentController;
import com.notfound.timetrackserver.controller.admin.AdminCommentController;
import com.notfound.timetrackserver.controller.admin.AdminLogController;
import com.notfound.timetrackserver.controller.admin.AdminMapController;
import com.notfound.timetrackserver.controller.admin.AdminMediaController;
import com.notfound.timetrackserver.controller.admin.AdminUgcController;
import com.notfound.timetrackserver.config.TencentMapProperties;
import com.notfound.timetrackserver.controller.user.AuthController;
import com.notfound.timetrackserver.controller.user.CommentController;
import com.notfound.timetrackserver.controller.user.ContentController;
import com.notfound.timetrackserver.controller.user.FavoriteController;
import com.notfound.timetrackserver.controller.user.MapController;
import com.notfound.timetrackserver.controller.user.MediaFileController;
import com.notfound.timetrackserver.controller.user.MeController;
import com.notfound.timetrackserver.controller.user.PoiController;
import com.notfound.timetrackserver.controller.user.UgcController;
import com.notfound.timetrackserver.controller.user.UserController;
import com.notfound.timetrackserver.mapper.MediaMapper;
import com.notfound.timetrackserver.service.AdminMediaService;
import com.notfound.timetrackserver.service.AdminMapService;
import com.notfound.timetrackserver.service.CommentService;
import com.notfound.timetrackserver.service.FavoriteService;
import com.notfound.timetrackserver.service.LogService;
import com.notfound.timetrackserver.service.MapService;
import com.notfound.timetrackserver.service.MediaFileService;
import com.notfound.timetrackserver.service.PoiService;
import com.notfound.timetrackserver.service.ReviewResultService;
import com.notfound.timetrackserver.service.UgcService;
import com.notfound.timetrackserver.service.UserService;
import com.notfound.timetrackserver.service.impl.MediaStructMapper;
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
