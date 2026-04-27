package com.notfound.timetrackpojo.dto;

import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WechatLoginRequestTest {

    @Test
    void codeFieldShouldKeepNotBlankConstraint() throws NoSuchFieldException {
        Field codeField = WechatLoginRequest.class.getDeclaredField("code");
        NotBlank notBlank = codeField.getAnnotation(NotBlank.class);

        assertNotNull(notBlank);
        assertEquals("code cannot be blank", notBlank.message());
    }

    @Test
    void gettersAndSettersShouldWork() {
        WechatLoginRequest request = new WechatLoginRequest();
        request.setCode("wx-code");
        request.setNickname("nick");
        request.setAvatarUrl("avatar");

        assertEquals("wx-code", request.getCode());
        assertEquals("nick", request.getNickname());
        assertEquals("avatar", request.getAvatarUrl());
    }
}

