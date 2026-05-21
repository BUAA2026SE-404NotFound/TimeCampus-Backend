package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuspojo.vo.NoteVO;
import com.notfound.timecampusserver.service.impl.CommentStructMapper;
import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.constant.ReviewStatuses;
import com.notfound.timecampuspojo.constant.TargetTypes;
import com.notfound.timecampuspojo.dto.CommentCreateRequest;
import com.notfound.timecampuspojo.entity.CommentEntity;
import com.notfound.timecampuspojo.entity.MediaEntity;
import com.notfound.timecampuspojo.entity.PoiEntity;
import com.notfound.timecampuspojo.entity.UserEntity;
import com.notfound.timecampuspojo.vo.CommentVO;
import com.notfound.timecampusserver.mapper.CommentMapper;
import com.notfound.timecampusserver.mapper.MediaMapper;
import com.notfound.timecampusserver.mapper.PoiMapper;
import com.notfound.timecampusserver.mapper.UserMapper;
import com.notfound.timecampusserver.service.CommentService;
import com.notfound.timecampusserver.service.LogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PoiMapper poiMapper;
    private final MediaMapper mediaMapper;
    private final UserMapper userMapper;
    private final CommentStructMapper commentStructMapper;
    private final LogService logService;

    public CommentServiceImpl(CommentMapper commentMapper,
                              PoiMapper poiMapper,
                              MediaMapper mediaMapper,
                              UserMapper userMapper,
                              CommentStructMapper commentStructMapper,
                              LogService logService) {
        this.commentMapper = commentMapper;
        this.poiMapper = poiMapper;
        this.mediaMapper = mediaMapper;
        this.userMapper = userMapper;
        this.commentStructMapper = commentStructMapper;
        this.logService = logService;
    }

    @Override
    @Transactional
    public CommentVO create(CommentCreateRequest request, Long userId) {
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing user token");
        }
        String targetType = normalizeTargetType(request.getTargetType());
        validateTarget(targetType, request.getTargetId());

        CommentEntity entity = new CommentEntity();
        entity.setUserId(userId);
        entity.setTargetType(targetType);
        entity.setTargetId(request.getTargetId());
        entity.setContent(request.getContent().trim());
        entity.setReviewStatus(ReviewStatuses.PENDING);
        commentMapper.insert(entity);
        logService.record("USER", userId, "behavior", "create_comment", targetType, request.getTargetId(), entity.getContent());
        return commentStructMapper.toVO(entity);
    }

    @Override
    @Transactional
    public NoteVO createNote(Long userId, Long poiId, String content) {
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing user token");
        }

        if (poiId == null) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "poiId is required");
        }

        PoiEntity poi = poiMapper.findById(poiId);
        if (poi == null) {
            throw new BizException(ResultCode.NOT_FOUND, "poi not found: " + poiId);
        }

        if (content == null || content.isBlank()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "content is required");
        }

        if (content.length() > 1000) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "content length must be <= 1000");
        }

        CommentEntity entity = new CommentEntity();
        entity.setUserId(userId);
        entity.setTargetType(TargetTypes.NOTE);
        entity.setTargetId(poiId);
        entity.setContent(content.trim());
        entity.setReviewStatus(ReviewStatuses.APPROVED);

        commentMapper.insert(entity);

        logService.record("USER", userId, "behavior", "create_note", "poi", poiId, content);

        NoteVO noteVO = new NoteVO();
        noteVO.setId(entity.getId());
        noteVO.setUserId(userId);
        noteVO.setPoiId(poiId);
        noteVO.setPoiName(poi.getName());
        noteVO.setContent(content.trim());
        noteVO.setReviewStatus(ReviewStatuses.APPROVED);
        noteVO.setCreateTime(entity.getCreateTime());
        noteVO.setUpdateTime(entity.getUpdateTime());

        return noteVO;
    }

    @Override
    public List<NoteVO> listMyNotes(Long userId, Long poiId, String reviewStatus) {
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing user token");
        }

        return commentMapper.listMyNotes(userId, poiId, reviewStatus);
    }

    @Override
    @Transactional
    public void deleteNote(Long id, Long userId) {
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing user token");
        }
        int deleted = commentMapper.deleteNoteByIdAndUserId(id, userId);
        if (deleted == 0) {
            throw new BizException(ResultCode.NOT_FOUND, "note not found or permission denied");
        }

        logService.record("USER", userId, "note", "delete_note", "note", id, null);
    }
    @Override
    public List<CommentVO> listByTarget(String targetType, Long targetId) {
        String normalizedTargetType = normalizeTargetType(targetType);
        if (targetId == null) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "targetId is required");
        }
        return enrichUsers(commentMapper.listByTarget(normalizedTargetType, targetId, ReviewStatuses.APPROVED));
    }

    @Override
    public List<CommentVO> listMine(Long userId, String reviewStatus) {
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing user token");
        }
        return commentMapper.listByUser(userId, normalizeReviewStatusOrNull(reviewStatus))
                .stream()
                .map(commentStructMapper::toVO)
                .toList();
    }

    @Override
    public List<CommentVO> listForAdmin(String targetType, Long targetId, String reviewStatus) {
        String normalizedTargetType = targetType == null || targetType.isBlank() ? null : normalizeTargetType(targetType);
        return enrichUsers(commentMapper.listForAdmin(normalizedTargetType, targetId, normalizeReviewStatusOrNull(reviewStatus)));
    }

    @Override
    @Transactional
    public CommentVO approve(Long id, Long reviewerId) {
        CommentEntity entity = requirePending(id);
        commentMapper.updateReview(entity.getId(), ReviewStatuses.APPROVED, null, reviewerId);
        logService.record("ADMIN", reviewerId, "review", "approve_comment", "comment", id, null);
        return commentStructMapper.toVO(commentMapper.findById(id));
    }

    @Override
    @Transactional
    public CommentVO reject(Long id, String reason, Long reviewerId) {
        if (reason == null || reason.isBlank()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "reject reason is required");
        }
        CommentEntity entity = requirePending(id);
        commentMapper.updateReview(entity.getId(), ReviewStatuses.REJECTED, reason, reviewerId);
        logService.record("ADMIN", reviewerId, "review", "reject_comment", "comment", id, reason);
        return commentStructMapper.toVO(commentMapper.findById(id));
    }

    private CommentEntity requirePending(Long id) {
        CommentEntity entity = commentMapper.findById(id);
        if (entity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "comment not found: " + id);
        }
        if (!ReviewStatuses.PENDING.equals(entity.getReviewStatus())) {
            throw new BizException(ResultCode.BIZ_ERROR, "comment is already reviewed: " + id);
        }
        return entity;
    }

    private void validateTarget(String targetType, Long targetId) {
        if (targetId == null) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "targetId is required");
        }
        if (TargetTypes.POI.equals(targetType)) {
            PoiEntity poi = poiMapper.findById(targetId);
            if (poi == null) {
                throw new BizException(ResultCode.NOT_FOUND, "poi not found: " + targetId);
            }
            if (poi.getStatus() == null || poi.getStatus() != 1) {
                throw new BizException(ResultCode.BIZ_ERROR, "poi is inactive: " + targetId);
            }
            return;
        }
        MediaEntity media = mediaMapper.findById(targetId);
        if (media == null) {
            throw new BizException(ResultCode.NOT_FOUND, "media not found: " + targetId);
        }
        if (!ReviewStatuses.APPROVED.equals(media.getReviewStatus())) {
            throw new BizException(ResultCode.BIZ_ERROR, "media is not commentable: " + targetId);
        }
    }

    private String normalizeTargetType(String targetType) {
        if (targetType == null) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "targetType is required");
        }
        String normalized = targetType.toLowerCase();
        if (TargetTypes.POI.equals(normalized) || TargetTypes.MEDIA.equals(normalized)) {
            return normalized;
        }
        throw new BizException(ResultCode.VALIDATION_ERROR, "targetType must be poi or media");
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

    private List<CommentVO> enrichUsers(List<CommentEntity> comments) {
        if (comments.isEmpty()) {
            return List.of();
        }
        Map<Long, UserEntity> users = comments.stream()
                .map(CommentEntity::getUserId)
                .distinct()
                .map(userMapper::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserEntity::getId, user -> user));
        return comments.stream()
                .map(comment -> commentStructMapper.toVO(comment, users))
                .toList();
    }
}
