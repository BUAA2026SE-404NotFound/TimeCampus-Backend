package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.dto.WechatLoginRequest;
import com.notfound.timecampuspojo.entity.UserEntity;
import com.notfound.timecampuspojo.vo.UserLoginVO;
import com.notfound.timecampuspojo.vo.UserProfileVO;
import com.notfound.timecampusserver.mapper.UserMapper;
import com.notfound.timecampusserver.service.UserService;
import com.notfound.timecampusserver.service.WechatAuthService;
import com.notfound.timecampusserver.security.UserAuthInterceptor;
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
            entity.setNickname(defaultIfBlank(request.getNickname(), "TimeCampus User"));
            entity.setAvatarUrl(request.getAvatarUrl());
            entity.setIdentity(request.getIdentityType());
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            userMapper.insert(entity);
        } else {
            entity.setNickname(defaultIfBlank(request.getNickname(), entity.getNickname()));
            entity.setAvatarUrl(defaultIfBlank(request.getAvatarUrl(), entity.getAvatarUrl()));
            entity.setIdentity(defaultIfBlank(request.getIdentityType(), entity.getIdentity()));
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
