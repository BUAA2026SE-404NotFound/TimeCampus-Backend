package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuspojo.vo.AdminMapMediaVO;
import com.notfound.timecampuspojo.vo.AdminMapPoiVO;
import com.notfound.timecampusserver.mapper.AdminMapMapper;
import com.notfound.timecampusserver.service.MediaFileService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminMapServiceImplTest {

    @Test
    void overviewUsesSignedAdminPreviewUrlsForPoiMedia() {
        AdminMapMapper adminMapMapper = mock(AdminMapMapper.class);
        MediaFileService mediaFileService = mock(MediaFileService.class);
        AdminMapServiceImpl service = new AdminMapServiceImpl(adminMapMapper, mediaFileService);

        AdminMapPoiVO poi = new AdminMapPoiVO();
        poi.setId(1L);
        AdminMapMediaVO media = new AdminMapMediaVO();
        media.setId(10L);
        media.setPoiId(1L);
        media.setImagePath("/uploads/photo.jpg");
        String signedUrl = "/api/v1/admin/media/10/file?accessToken=admin-token";

        when(adminMapMapper.listPoiOverview(null, null)).thenReturn(List.of(poi));
        when(adminMapMapper.listRecentFavorites(50)).thenReturn(List.of());
        when(adminMapMapper.listRecentComments(null, 50)).thenReturn(List.of());
        when(adminMapMapper.listPoiFavorites(List.of(1L))).thenReturn(List.of());
        when(adminMapMapper.listPoiComments(List.of(1L), null)).thenReturn(List.of());
        when(adminMapMapper.listPoiMedia(List.of(1L))).thenReturn(List.of(media));
        when(mediaFileService.adminPreviewUrl(10L, "/uploads/photo.jpg")).thenReturn(signedUrl);

        var overview = service.overview(null, null, null, null);

        AdminMapMediaVO result = overview.getPois().get(0).getMediaList().get(0);
        assertThat(result.getPreviewUrl()).isEqualTo(signedUrl);
        assertThat(result.getImagePath()).isEqualTo(signedUrl);
    }
}
