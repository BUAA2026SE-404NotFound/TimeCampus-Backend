package com.notfound.timetrackserver.mapper;

import com.notfound.timetrackpojo.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    UserEntity findById(Long id);

    UserEntity findByOpenId(String openId);

    int insert(UserEntity entity);

    int updateById(UserEntity entity);
}

