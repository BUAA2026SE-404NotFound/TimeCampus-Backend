package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.entity.MediaEntity;
import com.notfound.timecampuspojo.vo.MediaVO;
import com.notfound.timecampusserver.mapper.MediaMapper;
import com.notfound.timecampusserver.mapper.PoiMapper;
import com.notfound.timecampusserver.service.LogService;
import com.notfound.timecampusserver.service.StorageService;
import com.notfound.timecampusserver.service.UgcService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UgcServiceImpl implements UgcService {

    private static final String TYPE_UGC = "ugc";
    private static final String REVIEW_PENDING = "pending";
    private static final String REVIEW_APPROVED = "approved";
    private static final String REVIEW_REJECTED = "rejected";

    private final MediaMapper mediaMapper;
    private final PoiMapper poiMapper;
    private final StorageService storageService;
    private final MediaStructMapper mediaStructMapper;
    private final LogService logService;

    public UgcServiceImpl(MediaMapper mediaMapper,
                          PoiMapper poiMapper,
                          StorageService storageService,
                          MediaStructMapper mediaStructMapper,
                          LogService logService) {
        this.mediaMapper = mediaMapper;
        this.poiMapper = poiMapper;
        this.storageService = storageService;
        this.mediaStructMapper = mediaStructMapper;
        this.logService = logService;
    }

    @Override
    public MediaVO upload(MultipartFile file, Long poiId, Integer year, String description, String source, Long userId) {
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing user token");
        }
        if (poiId == null || !poiMapper.existsById(poiId)) {
            throw new BizException(ResultCode.NOT_FOUND, "poi not found: " + poiId);
        }
        validateYear(year);
        String imagePath = storageService.store(file, userId);
        MediaEntity entity = new MediaEntity();
        entity.setPoiId(poiId);
        entity.setType(TYPE_UGC);
        entity.setImagePath(imagePath);
        entity.setYear(year);
        entity.setDescription(description);
        entity.setUploadUserId(userId);
        entity.setReviewStatus(REVIEW_PENDING);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        mediaMapper.insert(entity);
        logService.record("USER", userId, "content", "upload_ugc", "media", entity.getId(), imagePath);
        return mediaStructMapper.toVO(entity);
    }

    @Override
    public List<MediaVO> list(String status) {
        return mediaMapper.list(null, TYPE_UGC, normalizeStatus(status), null, null)
                .stream()
                .map(mediaStructMapper::toAdminVO)
                .collect(Collectors.toList());
    }

    @Override
    public MediaVO approve(Long id, Long reviewerId) {
        MediaEntity entity = requireUgc(id);
        mediaMapper.updateReview(entity.getId(), REVIEW_APPROVED, null, reviewerId);
        logService.record("ADMIN", reviewerId, "review", "approve_ugc", "media", id, null);
        return mediaStructMapper.toAdminVO(mediaMapper.findById(id));
    }

    @Override
    public MediaVO reject(Long id, String reason, Long reviewerId) {
        if (reason == null || reason.isBlank()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "reject reason is required");
        }
        MediaEntity entity = requireUgc(id);
        mediaMapper.updateReview(entity.getId(), REVIEW_REJECTED, reason, reviewerId);
        logService.record("ADMIN", reviewerId, "review", "reject_ugc", "media", id, reason);
        return mediaStructMapper.toAdminVO(mediaMapper.findById(id));
    }

    private MediaEntity requireUgc(Long id) {
        MediaEntity entity = mediaMapper.findById(id);
        if (entity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "ugc not found: " + id);
        }
        if (!TYPE_UGC.equals(entity.getType())) {
            throw new BizException(ResultCode.BIZ_ERROR, "media is not ugc: " + id);
        }
        return entity;
    }

    private void validateYear(Integer year) {
        int currentYear = LocalDate.now().getYear();
        if (year == null || year < 1953 || year > currentYear) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "year must be between 1953 and " + currentYear);
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return REVIEW_PENDING;
        }
        String normalized = status.toLowerCase();
        if (REVIEW_PENDING.equals(normalized) || REVIEW_APPROVED.equals(normalized) || REVIEW_REJECTED.equals(normalized)) {
            return normalized;
        }
        throw new BizException(ResultCode.VALIDATION_ERROR, "invalid review status: " + status);
    }
}
