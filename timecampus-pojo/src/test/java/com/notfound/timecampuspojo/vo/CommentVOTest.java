package com.notfound.timecampuspojo.vo;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommentVOTest {

    @Test
    void gettersAndSettersShouldWork() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 6, 16, 0);
        CommentVO vo = new CommentVO();
        vo.setId(1L);
        vo.setUserId(2L);
        vo.setNickname("student");
        vo.setTargetType("poi");
        vo.setTargetId(3L);
        vo.setContent("nice");
        vo.setReviewStatus("pending");
        vo.setRejectReason("bad");
        vo.setReviewTime(now);
        vo.setReviewerId(4L);
        vo.setCreateTime(now);
        vo.setUpdateTime(now);

        assertEquals(1L, vo.getId());
        assertEquals(2L, vo.getUserId());
        assertEquals("student", vo.getNickname());
        assertEquals("poi", vo.getTargetType());
        assertEquals(3L, vo.getTargetId());
        assertEquals("nice", vo.getContent());
        assertEquals("pending", vo.getReviewStatus());
        assertEquals("bad", vo.getRejectReason());
        assertEquals(now, vo.getReviewTime());
        assertEquals(4L, vo.getReviewerId());
        assertEquals(now, vo.getCreateTime());
        assertEquals(now, vo.getUpdateTime());
    }
}
