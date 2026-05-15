package com.notfound.timecampusserver.mapper;

import com.notfound.timecampuspojo.entity.AdminEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {

    AdminEntity findByAdminName(String adminName);

    int updateLastLoginTime(Long id);
}

