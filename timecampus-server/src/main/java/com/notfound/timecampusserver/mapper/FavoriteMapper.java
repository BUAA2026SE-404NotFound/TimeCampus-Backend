package com.notfound.timecampusserver.mapper;
import com.notfound.timecampuspojo.entity.FavoriteEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteMapper {
    /**
     * 插入收藏记录
     */
    int insert(FavoriteEntity favorite);

    /**
     * 根据用户ID和目标删除收藏
     */
    int deleteByUserAndTarget(@Param("userId") Long userId,
                              @Param("targetType") String targetType,
                              @Param("targetId") Long targetId);

    /**
     * 检查是否已收藏
     */
    boolean existsByUserAndTarget(@Param("userId") Long userId,
                                  @Param("targetType") String targetType,
                                  @Param("targetId") Long targetId);

    /**
     * 查询用户的收藏列表（仅收藏记录）
     */
    List<FavoriteEntity> listByUser(@Param("userId") Long userId,
                                    @Param("targetType") String targetType);
}
