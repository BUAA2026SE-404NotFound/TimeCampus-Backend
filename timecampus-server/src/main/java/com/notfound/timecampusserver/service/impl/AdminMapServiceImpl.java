package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuspojo.vo.AdminMapOverviewVO;
import com.notfound.timecampuspojo.vo.AdminMapPoiVO;
import com.notfound.timecampusserver.mapper.AdminMapMapper;
import com.notfound.timecampusserver.service.AdminMapService;
import com.notfound.timecampusserver.service.MediaFileService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminMapServiceImpl implements AdminMapService {

    private final AdminMapMapper adminMapMapper;
    private final MediaFileService mediaFileService;

    public AdminMapServiceImpl(AdminMapMapper adminMapMapper, MediaFileService mediaFileService) {
        this.adminMapMapper = adminMapMapper;
        this.mediaFileService = mediaFileService;
    }

    @Override
    public AdminMapOverviewVO overview(Integer status, String keyword, String commentStatus, Integer limit) {
        int safeLimit = normalizeLimit(limit);
        AdminMapOverviewVO overview = new AdminMapOverviewVO();
        List<AdminMapPoiVO> pois = adminMapMapper.listPoiOverview(status, keyword);
        overview.setPois(pois);
        overview.setRecentFavorites(adminMapMapper.listRecentFavorites(safeLimit));
        overview.setRecentComments(adminMapMapper.listRecentComments(commentStatus, safeLimit));
        enrichPoiDetails(pois, commentStatus);
        return overview;
    }

    private void enrichPoiDetails(List<AdminMapPoiVO> pois, String commentStatus) {
        if (pois == null || pois.isEmpty()) {
            return;
        }
        List<Long> poiIds = pois.stream().map(AdminMapPoiVO::getId).toList();
        Map<Long, List<com.notfound.timecampuspojo.vo.AdminMapFavoriteVO>> favoritesByPoi =
                adminMapMapper.listPoiFavorites(poiIds).stream()
                        .filter(item -> item.getRelatedPoiId() != null)
                        .collect(Collectors.groupingBy(com.notfound.timecampuspojo.vo.AdminMapFavoriteVO::getRelatedPoiId));
        Map<Long, List<com.notfound.timecampuspojo.vo.AdminMapCommentVO>> commentsByPoi =
                adminMapMapper.listPoiComments(poiIds, commentStatus).stream()
                        .filter(item -> item.getRelatedPoiId() != null)
                        .collect(Collectors.groupingBy(com.notfound.timecampuspojo.vo.AdminMapCommentVO::getRelatedPoiId));
        Map<Long, List<com.notfound.timecampuspojo.vo.AdminMapMediaVO>> mediaByPoi =
                adminMapMapper.listPoiMedia(poiIds).stream()
                        .peek(media -> media.setPreviewUrl(mediaFileService.previewUrl(media.getId(), media.getImagePath())))
                        .collect(Collectors.groupingBy(com.notfound.timecampuspojo.vo.AdminMapMediaVO::getPoiId));

        for (AdminMapPoiVO poi : pois) {
            poi.setFavorites(favoritesByPoi.getOrDefault(poi.getId(), List.of()));
            poi.setComments(commentsByPoi.getOrDefault(poi.getId(), List.of()));
            poi.setMediaList(mediaByPoi.getOrDefault(poi.getId(), List.of()));
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return 50;
        }
        return Math.max(1, Math.min(limit, 200));
    }
}
