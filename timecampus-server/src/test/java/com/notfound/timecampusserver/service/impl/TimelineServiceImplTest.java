package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.vo.TimelineItemVO;
import com.notfound.timecampusserver.mapper.CommentMapper;
import com.notfound.timecampusserver.mapper.MediaMapper;
import com.notfound.timecampusserver.service.MediaFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TimelineServiceImplTest {

    private MediaMapper mediaMapper;
    private CommentMapper commentMapper;
    private MediaFileService mediaFileService;
    private TimelineServiceImpl timelineService;

    @BeforeEach
    void setUp() {
        mediaMapper = mock(MediaMapper.class);
        commentMapper = mock(CommentMapper.class);
        mediaFileService = mock(MediaFileService.class);
        timelineService = new TimelineServiceImpl(mediaMapper, commentMapper, mediaFileService);
    }

    @Test
    void anonymousUserOnlyGetsOfficial() {
        TimelineItemVO official = officialItem(1L, 2020);
        when(mediaMapper.listTimelineOfficial(isNull(), eq(2018), eq(2022))).thenReturn(List.of(official));
        when(mediaFileService.previewUrl(1L, "img.jpg")).thenReturn("/api/v1/media/1/file");

        List<TimelineItemVO> result = timelineService.list(null, 2018, 2022, "all", null);

        assertEquals(1, result.size());
        assertEquals("official", result.get(0).getType());
        assertEquals("/api/v1/media/1/file", result.get(0).getPreviewUrl());
        verifyNoInteractions(commentMapper);
    }

    @Test
    void loggedInUserGetsOfficialUgcAndComment() {
        TimelineItemVO official = officialItem(1L, 2020);
        TimelineItemVO memo = memoItem(2L, 2019);
        TimelineItemVO note = noteItem(3L, 2018);

        when(mediaMapper.listTimelineOfficial(isNull(), eq(2018), eq(2022)))
                .thenReturn(List.of(official));
        when(mediaMapper.listTimelineMemos(eq(9L), isNull(), eq(2018), eq(2022)))
                .thenReturn(List.of(memo));
        when(commentMapper.listTimelineNotes(eq(9L), isNull(), eq(2018), eq(2022)))
                .thenReturn(List.of(note));
        when(mediaFileService.previewUrl(1L, "img.jpg")).thenReturn("/api/v1/media/1/file");
        when(mediaFileService.previewUrl(2L, "memo.jpg")).thenReturn("/api/v1/media/2/file");

        List<TimelineItemVO> result = timelineService.list(9L, 2018, 2022, "all", null);

        assertEquals(3, result.size());
        assertEquals("official", result.get(0).getType());
        assertEquals("ugc", result.get(1).getType());
        assertEquals("comment", result.get(2).getType());
        assertNull(result.get(2).getPreviewUrl());
    }

    @Test
    void typeFilterLimitsQueries() {
        when(mediaMapper.listTimelineOfficial(isNull(), eq(2020), eq(2020))).thenReturn(List.of());

        timelineService.list(null, 2020, 2020, "official", null);

        verify(mediaMapper).listTimelineOfficial(isNull(), eq(2020), eq(2020));
        verifyNoInteractions(commentMapper);
    }

    @Test
    void invalidTypeThrows() {
        assertThrows(BizException.class,
                () -> timelineService.list(null, 2020, 2020, "invalid", null));
    }

    @Test
    void startYearAfterEndYearThrows() {
        assertThrows(BizException.class,
                () -> timelineService.list(null, 2022, 2020, "all", null));
    }

    private static TimelineItemVO officialItem(Long id, int year) {
        TimelineItemVO item = new TimelineItemVO();
        item.setId(id);
        item.setPoiId(10L);
        item.setPoiName("主楼");
        item.setYear(year);
        item.setDescription("说明文本");
        item.setImagePath("img.jpg");
        item.setCreateTime(LocalDateTime.of(2020, 1, 1, 0, 0));
        return item;
    }

    private static TimelineItemVO memoItem(Long id, int year) {
        TimelineItemVO item = new TimelineItemVO();
        item.setId(id);
        item.setPoiId(11L);
        item.setPoiName("图书馆");
        item.setYear(year);
        item.setDescription("备忘");
        item.setImagePath("memo.jpg");
        item.setCreateTime(LocalDateTime.of(2019, 6, 1, 0, 0));
        return item;
    }

    private static TimelineItemVO noteItem(Long id, int year) {
        TimelineItemVO item = new TimelineItemVO();
        item.setId(id);
        item.setPoiId(12L);
        item.setPoiName("操场");
        item.setYear(year);
        item.setDescription("我的笔记");
        item.setCreateTime(LocalDateTime.of(2018, 3, 1, 0, 0));
        return item;
    }
}
