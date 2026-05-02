package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackpojo.entity.MediaEntity;
import com.notfound.timetrackpojo.vo.MediaVO;
import org.springframework.stereotype.Component;

@Component
public class MediaStructMapper {

    private final com.notfound.timetrackserver.service.MediaFileService mediaFileService;

    public MediaStructMapper(com.notfound.timetrackserver.service.MediaFileService mediaFileService) {
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
