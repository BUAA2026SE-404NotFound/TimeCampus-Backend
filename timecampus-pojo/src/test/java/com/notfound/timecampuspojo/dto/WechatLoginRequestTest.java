package com.notfound.timecampuspojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    void identityTypeFieldShouldRequireKnownIdentity() throws NoSuchFieldException {
        Field identityTypeField = WechatLoginRequest.class.getDeclaredField("identityType");

        NotBlank notBlank = identityTypeField.getAnnotation(NotBlank.class);
        Pattern pattern = identityTypeField.getAnnotation(Pattern.class);

        assertNotNull(notBlank);
        assertEquals("identityType cannot be blank", notBlank.message());
        assertNotNull(pattern);
        assertEquals("FRESHMAN|STUDENT|ALUMNI", pattern.regexp());
    }

    @Test
    void gettersAndSettersShouldWork() {
        WechatLoginRequest request = new WechatLoginRequest();
        request.setCode("wx-code");
        request.setNickname("nick");
        request.setAvatarUrl("avatar");
        request.setIdentityType("STUDENT");

        assertEquals("wx-code", request.getCode());
        assertEquals("nick", request.getNickname());
        assertEquals("avatar", request.getAvatarUrl());
        assertEquals("STUDENT", request.getIdentityType());
    }
}
