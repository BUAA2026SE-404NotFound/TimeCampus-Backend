package com.notfound.timetrackpojo.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class UserEntityTest {

    @Test
    void shouldReadAndWriteSchemaFields() {
        UserEntity entity = new UserEntity();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusMinutes(1);

        entity.setId(1L);
        entity.setOpenId("openid_1");
        entity.setNickname("nick");
        entity.setAvatarUrl("avatar-url");
        entity.setIdentity("current");
        entity.setEnrollYear(2022);
        entity.setCreateTime(now);
        entity.setUpdateTime(later);

        assertEquals(1L, entity.getId());
        assertEquals("openid_1", entity.getOpenId());
        assertEquals("nick", entity.getNickname());
        assertEquals("avatar-url", entity.getAvatarUrl());
        assertEquals("current", entity.getIdentity());
        assertEquals(2022, entity.getEnrollYear());
        assertEquals(now, entity.getCreateTime());
        assertEquals(later, entity.getUpdateTime());
    }

    @Test
    void backwardCompatibleCreatedAtAccessorsShouldPointToSchemaTimeFields() {
        UserEntity entity = new UserEntity();
        LocalDateTime createTime = LocalDateTime.now();
        LocalDateTime updateTime = createTime.plusHours(1);

        entity.setCreatedAt(createTime);
        entity.setUpdatedAt(updateTime);

        assertEquals(createTime, entity.getCreateTime());
        assertEquals(updateTime, entity.getUpdateTime());
        assertSame(entity.getCreateTime(), entity.getCreatedAt());
        assertSame(entity.getUpdateTime(), entity.getUpdatedAt());
    }
}

