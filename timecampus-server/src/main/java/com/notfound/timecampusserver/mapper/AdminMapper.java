package com.notfound.timecampusserver.mapper;

import com.notfound.timecampuspojo.entity.AdminEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminMapper {

    AdminEntity findByAdminName(String adminName);

    AdminEntity findById(Long id);

    List<AdminEntity> findAll();

    int insert(AdminEntity admin);

    int updateStatus(AdminEntity admin);

    int updateRole(AdminEntity admin);

    int updateLastLoginTime(AdminEntity admin);
}
