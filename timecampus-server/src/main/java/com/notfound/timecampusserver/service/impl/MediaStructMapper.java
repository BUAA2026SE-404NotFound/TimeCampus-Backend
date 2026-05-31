package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuspojo.entity.MediaEntity;
import com.notfound.timecampuspojo.vo.MediaVO;
import org.springframework.stereotype.Component;

@Component
public class MediaStructMapper {

    private static final int DEFAULT_THUMBNAIL_SIZE = 192;

    private final com.notfound.timecampusserver.service.MediaFileService mediaFileService;

    public MediaStructMapper(com.notfound.timecampusserver.service.MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    public MediaVO toVO(MediaEntity entity) {
        if (entity == null) {
            return null;
        }
        MediaVO vo = new MediaVO();
        vo.setId(entity.getId());
        vo.setPoiId(entity.getPoiId());
        vo.setType(entity.getType());
        String previewUrl = mediaFileService.previewUrl(entity.getId(), entity.getImagePath());
        String thumbnailUrl = mediaFileService.previewUrl(entity.getId(), entity.getImagePath(), DEFAULT_THUMBNAIL_SIZE);
        vo.setImagePath(previewUrl);
        vo.setPreviewUrl(previewUrl);
        vo.setThumbnailUrl(thumbnailUrl);
        copyRest(entity, vo);
        return vo;
    }

    public MediaVO toAdminVO(MediaEntity entity) {
        if (entity == null) {
            return null;
        }
        MediaVO vo = new MediaVO();
        vo.setId(entity.getId());
        vo.setPoiId(entity.getPoiId());
        vo.setType(entity.getType());
        String previewUrl = mediaFileService.adminPreviewUrl(entity.getId(), entity.getImagePath());
        String thumbnailUrl = mediaFileService.adminPreviewUrl(entity.getId(), entity.getImagePath(), DEFAULT_THUMBNAIL_SIZE);
        vo.setImagePath(previewUrl);
        vo.setPreviewUrl(previewUrl);
        vo.setThumbnailUrl(thumbnailUrl);
        copyRest(entity, vo);
        return vo;
    }

    private void copyRest(MediaEntity entity, MediaVO vo) {
        vo.setYear(entity.getYear());
        vo.setDescription(entity.getDescription());
        vo.setUploadUserId(entity.getUploadUserId());
        vo.setReviewStatus(entity.getReviewStatus());
        vo.setRejectReason(entity.getRejectReason());
        vo.setReviewTime(entity.getReviewTime());
        vo.setReviewerId(entity.getReviewerId());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
    }
}
