package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.dto.FavoriteRequest;
import com.notfound.timecampuspojo.vo.FavoriteItemVO;

import java.util.List;
public interface FavoriteService {


    /**
     * 添加收藏
     */
    void addFavorite(FavoriteRequest request, Long userId);

    /**
     * 取消收藏
     */
    void removeFavorite(FavoriteRequest request, Long userId);

    /**
     * 查询收藏列表（含目标详情）
     */
    List<FavoriteItemVO> listFavorites(Long userId, String targetType);
}
