package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.dto.WechatLoginRequest;
import com.notfound.timetrackpojo.entity.UserEntity;
import com.notfound.timetrackpojo.vo.UserProfileVO;
import com.notfound.timetrackserver.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, new UserStructMapper());
    }

    @Test
    void wxLoginCreatesNewUserWhenNotExists() {
        WechatLoginRequest request = new WechatLoginRequest();
        request.setCode("abc");
        request.setNickname(" ");
        request.setAvatarUrl("https://img/avatar.png");

        when(userMapper.findByOpenId("wx_abc")).thenReturn(null);
        doAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        }).when(userMapper).insert(any(UserEntity.class));

        UserProfileVO result = userService.wxLogin(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userMapper).insert(captor.capture());
        verify(userMapper, never()).updateById(any(UserEntity.class));

        UserEntity inserted = captor.getValue();
        assertEquals("wx_abc", inserted.getOpenId());
        assertEquals("TimeTrack User", inserted.getNickname());
        assertEquals("https://img/avatar.png", inserted.getAvatarUrl());
        assertNotNull(inserted.getCreatedAt());
        assertNotNull(inserted.getUpdatedAt());

        assertEquals(1L, result.getId());
        assertEquals("TimeTrack User", result.getNickname());
        assertEquals("https://img/avatar.png", result.getAvatarUrl());
    }

    @Test
    void wxLoginUpdatesExistingUserWhenExists() {
        WechatLoginRequest request = new WechatLoginRequest();
        request.setCode("abc");
        request.setNickname("new-name");
        request.setAvatarUrl("");

        UserEntity existing = new UserEntity();
        existing.setId(2L);
        existing.setOpenId("wx_abc");
        existing.setNickname("old-name");
        existing.setAvatarUrl("old-avatar");

        when(userMapper.findByOpenId("wx_abc")).thenReturn(existing);

        UserProfileVO result = userService.wxLogin(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userMapper).updateById(captor.capture());
        verify(userMapper, never()).insert(any(UserEntity.class));

        UserEntity updated = captor.getValue();
        assertEquals("new-name", updated.getNickname());
        assertEquals("old-avatar", updated.getAvatarUrl());
        assertNotNull(updated.getUpdatedAt());

        assertEquals(2L, result.getId());
        assertEquals("new-name", result.getNickname());
        assertEquals("old-avatar", result.getAvatarUrl());
    }

    @Test
    void getByIdReturnsProfileWhenUserExists() {
        UserEntity existing = new UserEntity();
        existing.setId(3L);
        existing.setNickname("u3");
        existing.setAvatarUrl("a3");

        when(userMapper.findById(3L)).thenReturn(existing);

        UserProfileVO result = userService.getById(3L);

        assertEquals(3L, result.getId());
        assertEquals("u3", result.getNickname());
        assertEquals("a3", result.getAvatarUrl());
    }

    @Test
    void getByIdThrowsBizExceptionWhenNotFound() {
        when(userMapper.findById(99L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> userService.getById(99L));

        assertEquals(ResultCode.NOT_FOUND, ex.getResultCode());
        assertEquals("user not found: 99", ex.getMessage());
    }
}

