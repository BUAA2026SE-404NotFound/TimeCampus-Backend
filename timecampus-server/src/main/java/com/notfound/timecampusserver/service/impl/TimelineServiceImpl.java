package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.vo.TimelineItemVO;
import com.notfound.timecampusserver.mapper.CommentMapper;
import com.notfound.timecampusserver.mapper.MediaMapper;
import com.notfound.timecampusserver.service.MediaFileService;
import com.notfound.timecampusserver.service.TimelineService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class TimelineServiceImpl implements TimelineService {

    static final String TYPE_ALL = "all";
    static final String TYPE_OFFICIAL = "official";
    static final String TYPE_UGC = "ugc";
    static final String TYPE_COMMENT = "comment";

    private static final Set<String> ALLOWED_TYPES = Set.of(TYPE_ALL, TYPE_OFFICIAL, TYPE_UGC, TYPE_COMMENT);

    private final MediaMapper mediaMapper;
    private final CommentMapper commentMapper;
    private final MediaFileService mediaFileService;

    public TimelineServiceImpl(MediaMapper mediaMapper,
                               CommentMapper commentMapper,
                               MediaFileService mediaFileService) {
        this.mediaMapper = mediaMapper;
        this.commentMapper = commentMapper;
        this.mediaFileService = mediaFileService;
    }

    @Override
    public List<TimelineItemVO> list(Long userId, int startYear, int endYear, String type, Long poiId) {
        if (startYear > endYear) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "startYear must be <= endYear");
        }

        String normalizedType = normalizeType(type);
        List<TimelineItemVO> items = new ArrayList<>();

        if (includeOfficial(normalizedType)) {
            for (TimelineItemVO item : mediaMapper.listTimelineOfficial(poiId, startYear, endYear)) {
                enrichMediaItem(item, TYPE_OFFICIAL);
                items.add(item);
            }
        }
        if (userId != null) {
            if (includeUgc(normalizedType)) {
                for (TimelineItemVO item : mediaMapper.listTimelineMemos(userId, poiId, startYear, endYear)) {
                    enrichMediaItem(item, TYPE_UGC);
                    items.add(item);
                }
            }
            if (includeComment(normalizedType)) {
                for (TimelineItemVO item : commentMapper.listTimelineNotes(userId, poiId, startYear, endYear)) {
                    enrichNoteItem(item);
                    items.add(item);
                }
            }
        }

        items.sort(TIMELINE_ORDER);
        return items;
    }

    private void enrichMediaItem(TimelineItemVO item, String type) {
        item.setType(type);
        item.setPreviewUrl(mediaFileService.previewUrl(item.getId(), item.getImagePath()));
        item.setTitle(buildMediaTitle(item));
        item.setImagePath(null);
    }

    private void enrichNoteItem(TimelineItemVO item) {
        item.setType(TYPE_COMMENT);
        item.setPreviewUrl(null);
        item.setTitle(null);
        item.setImagePath(null);
    }

    private String buildMediaTitle(TimelineItemVO item) {
        String description = item.getDescription();
        if (description != null && !description.isBlank()) {
            return description.length() > 50 ? description.substring(0, 50) : description;
        }
        if (item.getPoiName() != null && !item.getPoiName().isBlank()) {
            return item.getPoiName() + "历史影像";
        }
        return null;
    }

    private boolean includeOfficial(String type) {
        return TYPE_ALL.equals(type) || TYPE_OFFICIAL.equals(type);
    }

    private boolean includeUgc(String type) {
        return TYPE_ALL.equals(type) || TYPE_UGC.equals(type);
    }

    private boolean includeComment(String type) {
        return TYPE_ALL.equals(type) || TYPE_COMMENT.equals(type);
    }

    private String normalizeType(String type) {
        String normalized = type == null || type.isBlank() ? TYPE_ALL : type.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new BizException(ResultCode.VALIDATION_ERROR,
                    "type must be one of: all, official, ugc, comment");
        }
        return normalized;
    }

    private static final Comparator<TimelineItemVO> TIMELINE_ORDER = Comparator
            .comparing(TimelineItemVO::getYear, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(TimelineItemVO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(TimelineItemVO::getId, Comparator.nullsLast(Comparator.reverseOrder()));
}
