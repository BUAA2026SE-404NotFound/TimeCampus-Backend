package com.notfound.timecampuspojo.vo;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileVOTest {

    @Test
    void exposesUserTableFieldsExceptOpenId() {
        UserProfileVO vo = new UserProfileVO();
        LocalDateTime createTime = LocalDateTime.of(2026, 5, 10, 17, 0);
        LocalDateTime updateTime = LocalDateTime.of(2026, 5, 10, 18, 0);

        vo.setId(1L);
        vo.setNickname("student");
        vo.setAvatarUrl("https://example.com/avatar.png");
        vo.setIdentity("STUDENT");
        vo.setEnrollYear(2022);
        vo.setCreateTime(createTime);
        vo.setUpdateTime(updateTime);

        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getNickname()).isEqualTo("student");
        assertThat(vo.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(vo.getIdentity()).isEqualTo("STUDENT");
        assertThat(vo.getEnrollYear()).isEqualTo(2022);
        assertThat(vo.getCreateTime()).isEqualTo(createTime);
        assertThat(vo.getUpdateTime()).isEqualTo(updateTime);
    }
}
