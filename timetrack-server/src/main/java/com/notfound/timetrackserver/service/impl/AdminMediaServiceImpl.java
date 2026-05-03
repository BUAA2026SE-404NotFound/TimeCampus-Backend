package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.dto.OfficialMediaImportRequest;
import com.notfound.timetrackpojo.entity.MediaEntity;
import com.notfound.timetrackpojo.vo.ImportResultVO;
import com.notfound.timetrackpojo.vo.MediaVO;
import com.notfound.timetrackserver.mapper.MediaMapper;
import com.notfound.timetrackserver.mapper.PoiMapper;
import com.notfound.timetrackserver.service.AdminMediaService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminMediaServiceImpl implements AdminMediaService {

    private static final String TYPE_OFFICIAL = "official";
    private static final String REVIEW_APPROVED = "approved";
    private static final String REVIEW_PENDING = "pending";

    private final MediaMapper mediaMapper;
    private final PoiMapper poiMapper;
    private final MediaStructMapper mediaStructMapper;

    public AdminMediaServiceImpl(MediaMapper mediaMapper, PoiMapper poiMapper, MediaStructMapper mediaStructMapper) {
        this.mediaMapper = mediaMapper;
        this.poiMapper = poiMapper;
        this.mediaStructMapper = mediaStructMapper;
    }

    @Override
    public ImportResultVO importOfficial(OfficialMediaImportRequest request) {
        ImportResultVO result = new ImportResultVO();
        result.setTotal(request.getItems().size());

        List<MediaEntity> toInsert = new ArrayList<>();
        for (int i = 0; i < request.getItems().size(); i++) {
            OfficialMediaImportRequest.OfficialMediaItem item = request.getItems().get(i);
            String error = validateItem(item);
            if (error != null) {
                result.addFailure(i, error);
                continue;
            }

            if (!poiMapper.existsById(item.getPoiId())) {
                result.addFailure(i, "poi not found: " + item.getPoiId());
                continue;
            }

            MediaEntity entity = new MediaEntity();
            entity.setPoiId(item.getPoiId());
            entity.setType(TYPE_OFFICIAL);
            entity.setImagePath(item.getImagePath());
            entity.setYear(item.getYear());
            entity.setDescription(item.getDescription());

            String reviewStatus = normalizeReviewStatus(item.getReviewStatus());
            entity.setReviewStatus(reviewStatus);
            if (REVIEW_APPROVED.equals(reviewStatus)) {
                entity.setReviewTime(LocalDateTime.now());
            }

            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            toInsert.add(entity);
        }

        if (!toInsert.isEmpty()) {
            mediaMapper.insertBatch(toInsert);
        }

        result.setFailCount(result.getFailures() == null ? 0 : result.getFailures().size());
        result.setSuccessCount(result.getTotal() - result.getFailCount());
        return result;
    }

    @Override
    public MediaVO getById(Long id) {
        MediaEntity entity = mediaMapper.findById(id);
        if (entity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "media not found: " + id);
        }
        return mediaStructMapper.toVO(entity);
    }

    @Override
    public List<MediaVO> list(Long poiId, String type, String reviewStatus, Integer yearFrom, Integer yearTo) {
        return mediaMapper.list(poiId, type, reviewStatus, yearFrom, yearTo)
                .stream()
                .map(mediaStructMapper::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        mediaMapper.deleteById(id);
    }

    private String validateItem(OfficialMediaImportRequest.OfficialMediaItem item) {
        if (item == null) {
            return "item is null";
        }
        if (item.getPoiId() == null) {
            return "poiId is required";
        }
        if (item.getImagePath() == null || item.getImagePath().isBlank()) {
            return "imagePath is required";
        }
        if (item.getYear() == null) {
            return "year is required";
        }
        return null;
    }

    @Override
    public void approveMedia(Long id, Long reviewerId) {
        MediaEntity media = mediaMapper.findById(id);
        if (media == null) {
            throw new BizException(ResultCode.NOT_FOUND, "影像不存在: " + id);
        }
        // 仅对 pending 状态进行审核
        if (!"pending".equalsIgnoreCase(media.getReviewStatus())) {
            throw new BizException(ResultCode.BIZ_ERROR, "该影像已经审核过，无法重复审核");
        }
        int rows = mediaMapper.updateReviewStatus(id, "approved", null, LocalDateTime.now(), reviewerId);
        if (rows != 1) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "审核更新失败");
        }
    }

    @Override
    public void rejectMedia(Long id, Long reviewerId, String rejectReason) {
        if (rejectReason == null || rejectReason.isBlank()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "驳回原因不能为空");
        }
        MediaEntity media = mediaMapper.findById(id);
        if (media == null) {
            throw new BizException(ResultCode.NOT_FOUND, "影像不存在: " + id);
        }
        if (!"pending".equalsIgnoreCase(media.getReviewStatus())) {
            throw new BizException(ResultCode.BIZ_ERROR, "该影像已经审核过，无法重复审核");
        }
        int rows = mediaMapper.updateReviewStatus(id, "rejected", rejectReason, LocalDateTime.now(), reviewerId);
        if (rows != 1) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "审核更新失败");
        }
    }

    private String normalizeReviewStatus(String value) {
        if (value == null || value.isBlank()) {
            return REVIEW_APPROVED;
        }
        if (REVIEW_APPROVED.equals(value) || REVIEW_PENDING.equals(value) || "rejected".equals(value)) {
            return value;
        }
        return REVIEW_APPROVED;
    }
}

