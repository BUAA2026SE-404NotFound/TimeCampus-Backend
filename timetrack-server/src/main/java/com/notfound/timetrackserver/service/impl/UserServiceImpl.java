package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.dto.WechatLoginRequest;
import com.notfound.timetrackpojo.entity.UserEntity;
import com.notfound.timetrackpojo.vo.UserProfileVO;
import com.notfound.timetrackserver.mapper.UserMapper;
import com.notfound.timetrackserver.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserStructMapper userStructMapper;

    public UserServiceImpl(UserMapper userMapper, UserStructMapper userStructMapper) {
        this.userMapper = userMapper;
        this.userStructMapper = userStructMapper;
    }

    @Override
    public UserProfileVO wxLogin(WechatLoginRequest request) {
        String openId = "wx_" + request.getCode();

        UserEntity entity = userMapper.findByOpenId(openId);
        if (entity == null) {
            entity = new UserEntity();
            entity.setOpenId(openId);
            entity.setNickname(defaultIfBlank(request.getNickname(), "TimeTrack User"));
            entity.setAvatarUrl(request.getAvatarUrl());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(entity);
        } else {
            entity.setNickname(defaultIfBlank(request.getNickname(), entity.getNickname()));
            entity.setAvatarUrl(defaultIfBlank(request.getAvatarUrl(), entity.getAvatarUrl()));
            entity.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(entity);
        }

        return userStructMapper.toProfileVO(entity);
    }

    @Override
    public UserProfileVO getById(Long id) {
        UserEntity entity = userMapper.findById(id);
        if (entity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "user not found: " + id);
        }
        return userStructMapper.toProfileVO(entity);
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
