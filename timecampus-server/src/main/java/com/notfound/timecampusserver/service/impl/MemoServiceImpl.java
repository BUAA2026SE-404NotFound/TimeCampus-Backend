package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampusserver.service.impl.MediaStructMapper;
import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.constant.MediaTypes;
import com.notfound.timecampuspojo.constant.ReviewStatuses;
import com.notfound.timecampuspojo.entity.MediaEntity;
import com.notfound.timecampuspojo.entity.PoiEntity;
import com.notfound.timecampuspojo.vo.MediaVO;
import com.notfound.timecampusserver.mapper.MediaMapper;
import com.notfound.timecampusserver.mapper.PoiMapper;
import com.notfound.timecampusserver.service.LogService;
import com.notfound.timecampusserver.service.MemoService;
import com.notfound.timecampusserver.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemoServiceImpl implements MemoService {
    @Autowired private MediaMapper mediaMapper;
    @Autowired private PoiMapper poiMapper;
    @Autowired
    private StorageService storageService;
    @Autowired private MediaStructMapper mediaStructMapper;
    @Autowired private LogService logService;

    @Override
    @Transactional
    public MediaVO createMemo(Long userId, Long poiId, Integer year, String description, MultipartFile file) {
        // 校验 POI 存在且上架
        PoiEntity poi = poiMapper.findById(poiId);
        if (poi == null || poi.getStatus() != 1) {
            throw new BizException(ResultCode.NOT_FOUND, "poi not found or inactive");
        }
        // 校验年份范围（可选）
        if (year == null || year < 1900 || year > LocalDate.now().getYear()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "invalid year");
        }
        // 存储文件（复用现有存储服务）
        String imagePath = storageService.store(file, userId);

        MediaEntity entity = new MediaEntity();
        entity.setPoiId(poiId);
        entity.setType(MediaTypes.MEMO);
        entity.setImagePath(imagePath);
        entity.setYear(year);
        entity.setDescription(description);
        entity.setUploadUserId(userId);
        entity.setReviewStatus(ReviewStatuses.APPROVED);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        mediaMapper.insert(entity);

        logService.record("USER", userId, "memo", "create_memo", "media", entity.getId(), null);
        return mediaStructMapper.toVO(entity);
    }

    @Override
    public List<MediaVO> listMemos(Long userId, Long poiId, Integer yearFrom, Integer yearTo) {
        return mediaMapper.listMemosByUser(userId, poiId, yearFrom, yearTo);
    }

    @Override
    @Transactional
    public void deleteMemo(Long id, Long userId) {
        int deleted = mediaMapper.deleteMemoByIdAndUserId(id, userId); // 需在 Mapper 中实现
        if (deleted == 0) {
            throw new BizException(ResultCode.NOT_FOUND, "memo not found or permission denied");
        }
        logService.record("USER", userId, "memo", "delete_memo", "media", id, null);
    }
}
