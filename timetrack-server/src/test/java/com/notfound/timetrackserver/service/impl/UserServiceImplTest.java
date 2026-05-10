package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.dto.WechatLoginRequest;
import com.notfound.timetrackpojo.entity.UserEntity;
import com.notfound.timetrackpojo.vo.UserLoginVO;
import com.notfound.timetrackpojo.vo.UserProfileVO;
import com.notfound.timetrackserver.mapper.UserMapper;
import com.notfound.timetrackserver.security.UserAuthInterceptor;
import com.notfound.timetrackserver.service.WechatAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private WechatAuthService wechatAuthService;

    @Mock
    private UserAuthInterceptor userAuthInterceptor;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, new UserStructMapper(), wechatAuthService, userAuthInterceptor);
    }

    @Test
    void wxLoginCreatesNewUserWhenNotExists() {
        WechatLoginRequest request = new WechatLoginRequest();
        request.setCode("abc");
        request.setNickname(" ");
        request.setAvatarUrl("https://img/avatar.png");
        request.setIdentityType("STUDENT");

        when(wechatAuthService.code2SessionOpenId("abc")).thenReturn("openid_abc");
        when(userMapper.findByOpenId("openid_abc")).thenReturn(null);
        doAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        }).when(userMapper).insert(any(UserEntity.class));

        when(userAuthInterceptor.issueToken(1L)).thenReturn("token-1");

        UserLoginVO result = userService.wxLogin(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userMapper).insert(captor.capture());
        verify(userMapper, never()).updateById(any(UserEntity.class));

        UserEntity inserted = captor.getValue();
        assertEquals("openid_abc", inserted.getOpenId());
        assertEquals("TimeTrack User", inserted.getNickname());
        assertEquals("https://img/avatar.png", inserted.getAvatarUrl());
        assertEquals("STUDENT", inserted.getIdentity());
        assertNotNull(inserted.getCreateTime());
        assertNotNull(inserted.getUpdateTime());

        assertEquals("token-1", result.getToken());
        assertEquals(1L, result.getProfile().getId());
        assertEquals("TimeTrack User", result.getProfile().getNickname());
        assertEquals("https://img/avatar.png", result.getProfile().getAvatarUrl());
    }

    @Test
    void wxLoginUpdatesExistingUserWhenExists() {
        WechatLoginRequest request = new WechatLoginRequest();
        request.setCode("abc");
        request.setNickname("new-name");
        request.setAvatarUrl("");
        request.setIdentityType("ALUMNI");

        UserEntity existing = new UserEntity();
        existing.setId(2L);
        existing.setOpenId("openid_abc");
        existing.setNickname("old-name");
        existing.setAvatarUrl("old-avatar");
        existing.setIdentity("STUDENT");

        when(wechatAuthService.code2SessionOpenId("abc")).thenReturn("openid_abc");
        when(userMapper.findByOpenId("openid_abc")).thenReturn(existing);

        when(userAuthInterceptor.issueToken(2L)).thenReturn("token-2");

        UserLoginVO result = userService.wxLogin(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userMapper).updateById(captor.capture());
        verify(userMapper, never()).insert(any(UserEntity.class));

        UserEntity updated = captor.getValue();
        assertEquals("new-name", updated.getNickname());
        assertEquals("old-avatar", updated.getAvatarUrl());
        assertEquals("ALUMNI", updated.getIdentity());
        assertNotNull(updated.getUpdateTime());

        assertEquals("token-2", result.getToken());
        assertEquals(2L, result.getProfile().getId());
        assertEquals("new-name", result.getProfile().getNickname());
        assertEquals("old-avatar", result.getProfile().getAvatarUrl());
    }

    @Test
    void getByIdReturnsProfileWhenUserExists() {
        UserEntity existing = new UserEntity();
        existing.setId(3L);
        existing.setNickname("u3");
        existing.setAvatarUrl("a3");
        existing.setIdentity("STUDENT");
        existing.setEnrollYear(2022);
        LocalDateTime createTime = LocalDateTime.of(2026, 5, 10, 17, 0);
        LocalDateTime updateTime = LocalDateTime.of(2026, 5, 10, 18, 0);
        existing.setCreateTime(createTime);
        existing.setUpdateTime(updateTime);

        when(userMapper.findById(3L)).thenReturn(existing);

        UserProfileVO result = userService.getById(3L);

        assertEquals(3L, result.getId());
        assertEquals("u3", result.getNickname());
        assertEquals("a3", result.getAvatarUrl());
        assertEquals("STUDENT", result.getIdentity());
        assertEquals(2022, result.getEnrollYear());
        assertEquals(createTime, result.getCreateTime());
        assertEquals(updateTime, result.getUpdateTime());
    }

    @Test
    void getByIdThrowsBizExceptionWhenNotFound() {
        when(userMapper.findById(99L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> userService.getById(99L));

        assertEquals(ResultCode.NOT_FOUND, ex.getResultCode());
        assertEquals("user not found: 99", ex.getMessage());
    }
}
