package com.notfound.timetrackserver.mapper;

import com.notfound.timetrackpojo.entity.PoiEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PoiMapper {

    PoiEntity findById(Long id);

    int insert(PoiEntity entity);

    int updateById(PoiEntity entity);

    int deleteById(Long id);

    boolean existsById(Long id);

    List<PoiEntity> list(@Param("status") Integer status, @Param("keyword") String keyword);

    List<PoiEntity> listByIds(@Param("ids") List<Long> ids);
}

