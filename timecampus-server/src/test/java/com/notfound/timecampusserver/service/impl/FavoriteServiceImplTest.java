package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.dto.FavoriteRequest;
import com.notfound.timecampuspojo.entity.FavoriteEntity;
import com.notfound.timecampuspojo.entity.PoiEntity;
import com.notfound.timecampuspojo.vo.FavoriteItemVO;
import com.notfound.timecampusserver.mapper.FavoriteMapper;
import com.notfound.timecampusserver.mapper.MediaMapper;
import com.notfound.timecampusserver.mapper.PoiMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private PoiMapper poiMapper;

    @Mock
    private MediaMapper mediaMapper;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    private Long userId = 1L;
    private Long poiId = 10L;

    @Test
    void addFavorite_ShouldSuccess_WhenPoiExistsAndNotFavorited() {
        // Given
        FavoriteRequest request = new FavoriteRequest();
        request.setTargetType("poi");
        request.setTargetId(poiId);

        PoiEntity poi = new PoiEntity();
        poi.setId(poiId);
        poi.setStatus(1);
        when(poiMapper.findById(poiId)).thenReturn(poi);
        when(favoriteMapper.existsByUserAndTarget(userId, "poi", poiId)).thenReturn(false);
        when(favoriteMapper.insert(any(FavoriteEntity.class))).thenReturn(1);

        // When
        favoriteService.addFavorite(request, userId);

        // Then
        verify(favoriteMapper).insert(any(FavoriteEntity.class));
    }

    @Test
    void addFavorite_ShouldThrow_WhenPoiNotFound() {
        FavoriteRequest request = new FavoriteRequest();
        request.setTargetType("poi");
        request.setTargetId(999L);
        when(poiMapper.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> favoriteService.addFavorite(request, userId))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("地点不存在");
    }

    @Test
    void removeFavorite_ShouldSuccess_WhenExists() {
        FavoriteRequest request = new FavoriteRequest();
        request.setTargetType("poi");
        request.setTargetId(poiId);

        when(favoriteMapper.deleteByUserAndTarget(userId, "poi", poiId)).thenReturn(1);

        favoriteService.removeFavorite(request, userId);
        verify(favoriteMapper).deleteByUserAndTarget(userId, "poi", poiId);
    }

    @Test
    void listFavorites_ShouldReturnVOList() {
        when(favoriteMapper.listByUser(userId, null)).thenReturn(List.of(createFavoriteEntity()));
        when(poiMapper.listByIds(List.of(poiId))).thenReturn(List.of(createPoiEntity()));

        List<FavoriteItemVO> result = favoriteService.listFavorites(userId, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPoiName()).isEqualTo("主楼");
    }

    private FavoriteEntity createFavoriteEntity() {
        FavoriteEntity entity = new FavoriteEntity();
        entity.setId(1L);
        entity.setUserId(userId);
        entity.setTargetType("poi");
        entity.setTargetId(poiId);
        return entity;
    }

    private PoiEntity createPoiEntity() {
        PoiEntity poi = new PoiEntity();
        poi.setId(poiId);
        poi.setName("主楼");
        poi.setDescription("北航主楼");
        poi.setStatus(1);
        return poi;
    }
}