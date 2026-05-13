package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.dto.FavoriteRequest;
import com.notfound.timetrackpojo.entity.FavoriteEntity;
import com.notfound.timetrackpojo.entity.MediaEntity;
import com.notfound.timetrackpojo.entity.PoiEntity;
import com.notfound.timetrackpojo.vo.FavoriteItemVO;
import com.notfound.timetrackserver.mapper.FavoriteMapper;
import com.notfound.timetrackserver.mapper.MediaMapper;
import com.notfound.timetrackserver.mapper.PoiMapper;
import com.notfound.timetrackserver.service.FavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl  implements FavoriteService{

    private static final String TARGET_TYPE_POI = "poi";
    private static final String TARGET_TYPE_MEDIA = "media";

    private final FavoriteMapper favoriteMapper;
    private final PoiMapper poiMapper;
    private final MediaMapper mediaMapper;

    public FavoriteServiceImpl(FavoriteMapper favoriteMapper,
                               PoiMapper poiMapper,
                               MediaMapper mediaMapper) {
        this.favoriteMapper = favoriteMapper;
        this.poiMapper = poiMapper;
        this.mediaMapper = mediaMapper;
    }

    @Override
    @Transactional
    public void addFavorite(FavoriteRequest request, Long userId) {
        String targetType = request.getTargetType();
        Long targetId = request.getTargetId();

        // 校验 targetType 合法
        if (!TARGET_TYPE_POI.equals(targetType) && !TARGET_TYPE_MEDIA.equals(targetType)) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "targetType 只能是 poi 或 media");
        }

        // 校验目标存在且可收藏（POI 需上架；Media 需审核通过）
        if (TARGET_TYPE_POI.equals(targetType)) {
            PoiEntity poi = poiMapper.findById(targetId);
            if (poi == null) {
                throw new BizException(ResultCode.NOT_FOUND, "地点不存在: " + targetId);
            }
            if (poi.getStatus() == null || poi.getStatus() != 1) {
                throw new BizException(ResultCode.BIZ_ERROR, "该地点已下架，无法收藏");
            }
        } else {
            MediaEntity media = mediaMapper.findById(targetId);
            if (media == null) {
                throw new BizException(ResultCode.NOT_FOUND, "影像不存在: " + targetId);
            }
            // 只有审核通过的影像才允许收藏
            if (!"approved".equalsIgnoreCase(media.getReviewStatus())) {
                throw new BizException(ResultCode.BIZ_ERROR, "该影像不可用，无法收藏");
            }
        }

        // 检查是否已收藏（避免唯一键冲突）
        boolean exists = favoriteMapper.existsByUserAndTarget(userId, targetType, targetId);
        if (exists) {
            throw new BizException(ResultCode.BIZ_ERROR, "已经收藏过该内容");
        }

        FavoriteEntity entity = new FavoriteEntity();
        entity.setUserId(userId);
        entity.setTargetType(targetType);
        entity.setTargetId(targetId);
        entity.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(entity);
    }

    @Override
    @Transactional
    public void removeFavorite(FavoriteRequest request, Long userId) {
        String targetType = request.getTargetType();
        Long targetId = request.getTargetId();

        int deleted = favoriteMapper.deleteByUserAndTarget(userId, targetType, targetId);
        if (deleted == 0) {
            throw new BizException(ResultCode.NOT_FOUND, "未找到对应的收藏记录");
        }
    }

    @Override
    public List<FavoriteItemVO> listFavorites(Long userId, String targetType) {
        // 1. 查询收藏记录
        List<FavoriteEntity> favorites = favoriteMapper.listByUser(userId, targetType);
        if (favorites.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 分离 POI 和 Media 的收藏
        List<Long> poiIds = new ArrayList<>();
        List<Long> mediaIds = new ArrayList<>();
        for (FavoriteEntity fav : favorites) {
            if (TARGET_TYPE_POI.equals(fav.getTargetType())) {
                poiIds.add(fav.getTargetId());
            } else if (TARGET_TYPE_MEDIA.equals(fav.getTargetType())) {
                mediaIds.add(fav.getTargetId());
            }
        }

        // 3. 批量查询 POI 和 Media
        Map<Long, PoiEntity> poiMap = poiIds.isEmpty() ? Map.of() :
                poiMapper.listByIds(poiIds).stream().collect(Collectors.toMap(PoiEntity::getId, p -> p));
        Map<Long, MediaEntity> mediaMap = mediaIds.isEmpty() ? Map.of() :
                mediaMapper.listByIds(mediaIds).stream().collect(Collectors.toMap(MediaEntity::getId, m -> m));

        // 4. 组装 VO
        List<FavoriteItemVO> result = new ArrayList<>();
        for (FavoriteEntity fav : favorites) {
            FavoriteItemVO vo = new FavoriteItemVO();
            vo.setFavoriteId(fav.getId());
            vo.setTargetType(fav.getTargetType());
            vo.setTargetId(fav.getTargetId());
            vo.setCreateTime(fav.getCreateTime());

            if (TARGET_TYPE_POI.equals(fav.getTargetType())) {
                PoiEntity poi = poiMap.get(fav.getTargetId());
                if (poi != null) {
                    vo.setPoiName(poi.getName());
                    vo.setPoiDescription(poi.getDescription());
                }
            } else if (TARGET_TYPE_MEDIA.equals(fav.getTargetType())) {
                MediaEntity media = mediaMap.get(fav.getTargetId());
                if (media != null) {
                    vo.setMediaYear(media.getYear());
                    vo.setMediaImagePath(media.getImagePath());
                    vo.setMediaDescription(media.getDescription());
                }
            }
            result.add(vo);
        }
        return result;
    }

}
