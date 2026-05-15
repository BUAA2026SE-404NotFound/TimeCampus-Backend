package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuspojo.entity.MediaEntity;
import com.notfound.timecampuspojo.vo.MediaVO;
import org.springframework.stereotype.Component;

@Component
public class MediaStructMapper {

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
        vo.setImagePath(entity.getImagePath());
        vo.setPreviewUrl(mediaFileService.previewUrl(entity.getId(), entity.getImagePath()));
        vo.setYear(entity.getYear());
        vo.setDescription(entity.getDescription());
        vo.setUploadUserId(entity.getUploadUserId());
        vo.setReviewStatus(entity.getReviewStatus());
        vo.setRejectReason(entity.getRejectReason());
        vo.setReviewTime(entity.getReviewTime());
        vo.setReviewerId(entity.getReviewerId());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
