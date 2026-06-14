package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.constant.ReviewStatuses;
import com.notfound.timecampuspojo.vo.CommentVO;
import com.notfound.timecampuspojo.vo.MediaVO;
import com.notfound.timecampuspojo.vo.UserReviewResultVO;
import com.notfound.timecampusserver.mapper.CommentMapper;
import com.notfound.timecampusserver.mapper.MediaMapper;
import com.notfound.timecampusserver.mapper.UserMapper;
import com.notfound.timecampusserver.service.ReviewResultService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewResultServiceImpl implements ReviewResultService {

    private static final String TYPE_UGC = "ugc";

    private final UserMapper userMapper;
    private final MediaMapper mediaMapper;
    private final CommentMapper commentMapper;
    private final MediaStructMapper mediaStructMapper;
    private final CommentStructMapper commentStructMapper;

    public ReviewResultServiceImpl(UserMapper userMapper,
                                   MediaMapper mediaMapper,
                                   CommentMapper commentMapper,
                                   MediaStructMapper mediaStructMapper,
                                   CommentStructMapper commentStructMapper) {
        this.userMapper = userMapper;
        this.mediaMapper = mediaMapper;
        this.commentMapper = commentMapper;
        this.mediaStructMapper = mediaStructMapper;
        this.commentStructMapper = commentStructMapper;
    }

    @Override
    public UserReviewResultVO listByUser(Long userId, String reviewStatus) {
        requireExistingUser(userId);
        String normalizedStatus = normalizeReviewStatusOrNull(reviewStatus);
        List<MediaVO> ugcItems = listUgcByUserUnchecked(userId, normalizedStatus);
        List<CommentVO> comments = listCommentsByUserUnchecked(userId, normalizedStatus);

        UserReviewResultVO result = new UserReviewResultVO();
        result.setUserId(userId);
        result.setReviewStatus(normalizedStatus);
        result.setUgcItems(ugcItems);
        result.setComments(comments);
        result.setUgcCount(ugcItems.size());
        result.setCommentCount(comments.size());
        return result;
    }

    @Override
    public List<MediaVO> listUgcByUser(Long userId, String reviewStatus) {
        requireExistingUser(userId);
        return listUgcByUserUnchecked(userId, normalizeReviewStatusOrNull(reviewStatus));
    }

    @Override
    public List<CommentVO> listCommentsByUser(Long userId, String reviewStatus) {
        requireExistingUser(userId);
        return listCommentsByUserUnchecked(userId, normalizeReviewStatusOrNull(reviewStatus));
    }

    private List<MediaVO> listUgcByUserUnchecked(Long userId, String normalizedStatus) {
        return mediaMapper.listByUploadUser(userId, TYPE_UGC, normalizedStatus)
                .stream()
                .map(mediaStructMapper::toVO)
                .toList();
    }

    private List<CommentVO> listCommentsByUserUnchecked(Long userId, String normalizedStatus) {
        return commentMapper.listByUser(userId, normalizedStatus)
                .stream()
                .map(commentStructMapper::toVO)
                .toList();
    }

    private void requireExistingUser(Long userId) {
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing user token");
        }
        if (userMapper.findById(userId) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "user not found: " + userId);
        }
    }

    private String normalizeReviewStatusOrNull(String reviewStatus) {
        if (reviewStatus == null || reviewStatus.isBlank()) {
            return null;
        }
        String normalized = reviewStatus.toLowerCase();
        if (ReviewStatuses.PENDING.equals(normalized)
                || ReviewStatuses.APPROVED.equals(normalized)
                || ReviewStatuses.REJECTED.equals(normalized)) {
            return normalized;
        }
        throw new BizException(ResultCode.VALIDATION_ERROR, "invalid review status: " + reviewStatus);
    }
}
