package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.dto.CommentCreateRequest;
import com.notfound.timecampuspojo.entity.CommentEntity;
import com.notfound.timecampuspojo.entity.MediaEntity;
import com.notfound.timecampuspojo.entity.PoiEntity;
import com.notfound.timecampuspojo.entity.UserEntity;
import com.notfound.timecampusserver.mapper.CommentMapper;
import com.notfound.timecampusserver.mapper.MediaMapper;
import com.notfound.timecampusserver.mapper.PoiMapper;
import com.notfound.timecampusserver.mapper.UserMapper;
import com.notfound.timecampusserver.service.LogService;
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
    void createMediaCommentWhenMediaApproved() {
        MediaEntity media = new MediaEntity();
        media.setId(9L);
        media.setReviewStatus("approved");
        when(mediaMapper.findById(9L)).thenReturn(media);

        CommentCreateRequest request = new CommentCreateRequest();
        request.setTargetType("MEDIA");
        request.setTargetId(9L);
        request.setContent("great");

        service.create(request, 3L);

        ArgumentCaptor<CommentEntity> captor = ArgumentCaptor.forClass(CommentEntity.class);
        verify(commentMapper).insert(captor.capture());
        assertThat(captor.getValue().getTargetType()).isEqualTo("media");
        assertThat(captor.getValue().getReviewStatus()).isEqualTo("pending");
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

    @Test
    void listMineRejectsInvalidReviewStatus() {
        assertThatThrownBy(() -> service.listMine(3L, "unknown"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("invalid review status");
    }

    @Test
    void listForAdminCanEnrichNickname() {
        CommentEntity comment = comment(8L);
        UserEntity user = new UserEntity();
        user.setId(3L);
        user.setNickname("student");
        when(commentMapper.listForAdmin(null, null, "pending")).thenReturn(java.util.List.of(comment));
        when(userMapper.findById(3L)).thenReturn(user);

        var result = service.listForAdmin(null, null, "pending");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getNickname()).isEqualTo("student");
    }

    @Test
    void approveRequiresPendingCommentAndUpdatesReview() {
        CommentEntity comment = comment(8L);
        when(commentMapper.findById(8L)).thenReturn(comment);

        service.approve(8L, 99L);

        verify(commentMapper).updateReview(8L, "approved", null, 99L);
    }

    @Test
    void approveRejectsAlreadyReviewedComment() {
        CommentEntity comment = comment(8L);
        comment.setReviewStatus("approved");
        when(commentMapper.findById(8L)).thenReturn(comment);

        assertThatThrownBy(() -> service.approve(8L, 99L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("already reviewed");
    }

    private CommentEntity comment(Long id) {
        CommentEntity entity = new CommentEntity();
        entity.setId(id);
        entity.setUserId(3L);
        entity.setTargetType("poi");
        entity.setTargetId(7L);
        entity.setContent("nice");
        entity.setReviewStatus("pending");
        return entity;
    }
}
