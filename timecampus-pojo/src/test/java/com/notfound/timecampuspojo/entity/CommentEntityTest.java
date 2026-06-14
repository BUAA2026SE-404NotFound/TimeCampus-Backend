package com.notfound.timecampuspojo.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommentEntityTest {

    @Test
    void shouldReadAndWriteFields() {
        CommentEntity entity = new CommentEntity();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusMinutes(5);

        entity.setId(10L);
        entity.setUserId(30L);
        entity.setTargetType("media");
        entity.setTargetId(20L);
        entity.setContent("这栋楼以前是图书馆");
        entity.setReviewStatus("pending");
        entity.setRejectReason(null);
        entity.setReviewTime(null);
        entity.setReviewerId(null);
        entity.setCreateTime(now);
        entity.setUpdateTime(later);

        assertEquals(10L, entity.getId());
        assertEquals(30L, entity.getUserId());
        assertEquals("media", entity.getTargetType());
        assertEquals(20L, entity.getTargetId());
        assertEquals("这栋楼以前是图书馆", entity.getContent());
        assertEquals("pending", entity.getReviewStatus());
        assertEquals(now, entity.getCreateTime());
        assertEquals(later, entity.getUpdateTime());
    }
}


