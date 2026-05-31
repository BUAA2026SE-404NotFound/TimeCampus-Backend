package com.notfound.timecampusserver.mapper;

import com.notfound.timecampuspojo.entity.AdminEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {

    AdminEntity findByAdminName(String adminName);

    AdminEntity findById(Long id);

    int insert(AdminEntity admin);

    int updateStatus(AdminEntity admin);

    int updateRole(AdminEntity admin);

    int updateLastLoginTime(AdminEntity admin);
}
