package com.notfound.timetrackserver.mapper;

import com.notfound.timetrackpojo.entity.LogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LogMapper {

    int insert(LogEntity entity);

    List<LogEntity> list(@Param("operatorType") String operatorType,
                         @Param("type") String type,
                         @Param("action") String action,
                         @Param("targetType") String targetType,
                         @Param("limit") Integer limit);
}
