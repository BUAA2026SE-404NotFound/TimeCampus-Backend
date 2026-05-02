package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.dto.WechatLoginRequest;
import com.notfound.timetrackpojo.entity.UserEntity;
import com.notfound.timetrackpojo.vo.UserLoginVO;
import com.notfound.timetrackpojo.vo.UserProfileVO;
import com.notfound.timetrackserver.mapper.UserMapper;
import com.notfound.timetrackserver.service.UserService;
import com.notfound.timetrackserver.service.WechatAuthService;
import com.notfound.timetrackserver.security.UserAuthInterceptor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserStructMapper userStructMapper;
    private final WechatAuthService wechatAuthService;
    private final UserAuthInterceptor userAuthInterceptor;

    public UserServiceImpl(UserMapper userMapper,
                           UserStructMapper userStructMapper,
                           WechatAuthService wechatAuthService,
                           UserAuthInterceptor userAuthInterceptor) {
        this.userMapper = userMapper;
        this.userStructMapper = userStructMapper;
        this.wechatAuthService = wechatAuthService;
        this.userAuthInterceptor = userAuthInterceptor;
    }

    @Override
    public UserLoginVO wxLogin(WechatLoginRequest request) {
        String openId = wechatAuthService.code2SessionOpenId(request.getCode());

        UserEntity entity = userMapper.findByOpenId(openId);
        if (entity == null) {
            entity = new UserEntity();
            entity.setOpenId(openId);
            entity.setNickname(defaultIfBlank(request.getNickname(), "TimeTrack User"));
            entity.setAvatarUrl(request.getAvatarUrl());
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            userMapper.insert(entity);
        } else {
            entity.setNickname(defaultIfBlank(request.getNickname(), entity.getNickname()));
            entity.setAvatarUrl(defaultIfBlank(request.getAvatarUrl(), entity.getAvatarUrl()));
            entity.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(entity);
        }

        UserProfileVO profile = userStructMapper.toProfileVO(entity);
        String token = userAuthInterceptor.issueToken(entity.getId());

        UserLoginVO vo = new UserLoginVO();
        vo.setToken(token);
        vo.setProfile(profile);
        return vo;
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
