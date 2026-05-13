package com.notfound.timetrackserver.mapper;

import com.notfound.timetrackpojo.entity.AdminEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {

    AdminEntity findByAdminName(String adminName);

    int updateLastLoginTime(Long id);
}

