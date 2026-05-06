package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.dto.CommentCreateRequest;
import com.notfound.timetrackpojo.entity.CommentEntity;
import com.notfound.timetrackpojo.entity.MediaEntity;
import com.notfound.timetrackpojo.entity.PoiEntity;
import com.notfound.timetrackserver.mapper.CommentMapper;
import com.notfound.timetrackserver.mapper.MediaMapper;
import com.notfound.timetrackserver.mapper.PoiMapper;
import com.notfound.timetrackserver.mapper.UserMapper;
import com.notfound.timetrackserver.service.LogService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommentServiceImplTest {

    private final CommentMapper commentMapper = mock(CommentMapper.class);
    private final PoiMapper poiMapper = mock(PoiMapper.class);
    private final MediaMapper mediaMapper = mock(MediaMapper.class);
    private final UserMapper userMapper = mock(UserMapper.class);
    private final LogService logService = mock(LogService.class);
    private final CommentServiceImpl service = new CommentServiceImpl(
            commentMapper,
            poiMapper,
            mediaMapper,
            userMapper,
            new CommentStructMapper(),
            logService
    );

    @Test
    void createPoiCommentAsPending() {
        PoiEntity poi = new PoiEntity();
        poi.setId(7L);
        poi.setStatus(1);
        when(poiMapper.findById(7L)).thenReturn(poi);

        CommentCreateRequest request = new CommentCreateRequest();
        request.setTargetType("poi");
        request.setTargetId(7L);
        request.setContent("  很有意思  ");

        var result = service.create(request, 3L);

        ArgumentCaptor<CommentEntity> captor = ArgumentCaptor.forClass(CommentEntity.class);
        verify(commentMapper).insert(captor.capture());
        CommentEntity inserted = captor.getValue();
        assertThat(inserted.getUserId()).isEqualTo(3L);
        assertThat(inserted.getTargetType()).isEqualTo("poi");
        assertThat(inserted.getTargetId()).isEqualTo(7L);
        assertThat(inserted.getContent()).isEqualTo("很有意思");
        assertThat(inserted.getReviewStatus()).isEqualTo("pending");
        assertThat(result.getReviewStatus()).isEqualTo("pending");
    }

    @Test
    void createRejectsUnapprovedMedia() {
        MediaEntity media = new MediaEntity();
        media.setId(9L);
        media.setReviewStatus("pending");
        when(mediaMapper.findById(9L)).thenReturn(media);

        CommentCreateRequest request = new CommentCreateRequest();
        request.setTargetType("media");
        request.setTargetId(9L);
        request.setContent("test");

        assertThatThrownBy(() -> service.create(request, 3L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("media is not commentable");
    }

    @Test
    void publicListOnlyQueriesApprovedComments() {
        service.listByTarget("poi", 7L);

        verify(commentMapper).listByTarget("poi", 7L, "approved");
    }

    @Test
    void rejectRequiresReason() {
        assertThatThrownBy(() -> service.reject(1L, " ", 99L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("reject reason is required");
    }
}
