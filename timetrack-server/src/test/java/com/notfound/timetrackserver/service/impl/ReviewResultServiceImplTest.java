package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackpojo.entity.CommentEntity;
import com.notfound.timetrackpojo.entity.MediaEntity;
import com.notfound.timetrackpojo.entity.UserEntity;
import com.notfound.timetrackserver.mapper.CommentMapper;
import com.notfound.timetrackserver.mapper.MediaMapper;
import com.notfound.timetrackserver.mapper.UserMapper;
import com.notfound.timetrackserver.service.MediaFileService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewResultServiceImplTest {

    private final UserMapper userMapper = mock(UserMapper.class);
    private final MediaMapper mediaMapper = mock(MediaMapper.class);
    private final CommentMapper commentMapper = mock(CommentMapper.class);
    private final MediaFileService mediaFileService = mock(MediaFileService.class);
    private final ReviewResultServiceImpl service = new ReviewResultServiceImpl(
            userMapper,
            mediaMapper,
            commentMapper,
            new MediaStructMapper(mediaFileService),
            new CommentStructMapper()
    );

    @Test
    void listByUserReturnsUgcAndCommentReviewResults() {
        UserEntity user = new UserEntity();
        user.setId(7L);
        when(userMapper.findById(7L)).thenReturn(user);

        MediaEntity media = new MediaEntity();
        media.setId(10L);
        media.setType("ugc");
        media.setUploadUserId(7L);
        media.setReviewStatus("rejected");
        media.setRejectReason("图片不清晰");
        when(mediaFileService.previewUrl(10L, null)).thenReturn("/api/v1/media/10/file");
        when(mediaMapper.listByUploadUser(7L, "ugc", "rejected")).thenReturn(List.of(media));

        CommentEntity comment = new CommentEntity();
        comment.setId(20L);
        comment.setUserId(7L);
        comment.setTargetType("poi");
        comment.setTargetId(1L);
        comment.setContent("hello");
        comment.setReviewStatus("rejected");
        comment.setRejectReason("内容不合适");
        when(commentMapper.listByUser(7L, "rejected")).thenReturn(List.of(comment));

        var result = service.listByUser(7L, "REJECTED");

        assertThat(result.getUserId()).isEqualTo(7L);
        assertThat(result.getReviewStatus()).isEqualTo("rejected");
        assertThat(result.getUgcCount()).isEqualTo(1);
        assertThat(result.getCommentCount()).isEqualTo(1);
        assertThat(result.getUgcItems().getFirst().getRejectReason()).isEqualTo("图片不清晰");
        assertThat(result.getComments().getFirst().getRejectReason()).isEqualTo("内容不合适");
        verify(mediaMapper).listByUploadUser(7L, "ugc", "rejected");
        verify(commentMapper).listByUser(7L, "rejected");
    }

    @Test
    void listByUserRejectsInvalidReviewStatus() {
        UserEntity user = new UserEntity();
        user.setId(7L);
        when(userMapper.findById(7L)).thenReturn(user);

        assertThatThrownBy(() -> service.listByUser(7L, "done"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("invalid review status");
    }

    @Test
    void listUgcByUserReturnsOnlyUgcReviewResults() {
        UserEntity user = new UserEntity();
        user.setId(7L);
        when(userMapper.findById(7L)).thenReturn(user);

        MediaEntity media = new MediaEntity();
        media.setId(10L);
        media.setReviewStatus("pending");
        when(mediaMapper.listByUploadUser(7L, "ugc", "pending")).thenReturn(List.of(media));

        var result = service.listUgcByUser(7L, "pending");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(10L);
        verify(mediaMapper).listByUploadUser(7L, "ugc", "pending");
    }

    @Test
    void listCommentsByUserReturnsOnlyCommentReviewResults() {
        UserEntity user = new UserEntity();
        user.setId(7L);
        when(userMapper.findById(7L)).thenReturn(user);

        CommentEntity comment = new CommentEntity();
        comment.setId(20L);
        comment.setReviewStatus("approved");
        when(commentMapper.listByUser(7L, "approved")).thenReturn(List.of(comment));

        var result = service.listCommentsByUser(7L, "approved");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(20L);
        verify(commentMapper).listByUser(7L, "approved");
    }

    @Test
    void listByUserThrowsNotFoundWhenUserMissing() {
        when(userMapper.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.listByUser(99L, null))
                .isInstanceOf(BizException.class)
                .extracting("resultCode")
                .isEqualTo(ResultCode.NOT_FOUND);
    }
}
