package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuspojo.entity.UserEntity;
import com.notfound.timecampuspojo.vo.UserProfileVO;
import org.springframework.stereotype.Component;

@Component
public class UserStructMapper {

    public UserProfileVO toProfileVO(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        UserProfileVO vo = new UserProfileVO();
        vo.setId(entity.getId());
        vo.setNickname(entity.getNickname());
        vo.setAvatarUrl(entity.getAvatarUrl());
        vo.setIdentity(entity.getIdentity());
        vo.setEnrollYear(entity.getEnrollYear());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
