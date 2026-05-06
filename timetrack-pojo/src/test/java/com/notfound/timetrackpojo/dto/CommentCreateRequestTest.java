package com.notfound.timetrackpojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommentCreateRequestTest {

    @Test
    void targetTypeShouldOnlyAllowKnownTargets() throws NoSuchFieldException {
        Field field = CommentCreateRequest.class.getDeclaredField("targetType");

        NotBlank notBlank = field.getAnnotation(NotBlank.class);
        Pattern pattern = field.getAnnotation(Pattern.class);

        assertNotNull(notBlank);
        assertEquals("targetType cannot be blank", notBlank.message());
        assertNotNull(pattern);
        assertEquals("poi|media", pattern.regexp());
    }

    @Test
    void targetIdShouldBeRequired() throws NoSuchFieldException {
        Field field = CommentCreateRequest.class.getDeclaredField("targetId");

        NotNull notNull = field.getAnnotation(NotNull.class);

        assertNotNull(notNull);
        assertEquals("targetId cannot be null", notNull.message());
    }

    @Test
    void contentShouldBeRequiredAndBounded() throws NoSuchFieldException {
        Field field = CommentCreateRequest.class.getDeclaredField("content");

        NotBlank notBlank = field.getAnnotation(NotBlank.class);
        Size size = field.getAnnotation(Size.class);

        assertNotNull(notBlank);
        assertEquals("content cannot be blank", notBlank.message());
        assertNotNull(size);
        assertEquals(1000, size.max());
    }

    @Test
    void gettersAndSettersShouldWork() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setTargetType("poi");
        request.setTargetId(7L);
        request.setContent("hello");

        assertEquals("poi", request.getTargetType());
        assertEquals(7L, request.getTargetId());
        assertEquals("hello", request.getContent());
    }
}
