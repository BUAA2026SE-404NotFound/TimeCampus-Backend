package com.notfound.timecampusserver.mapper;

import com.notfound.timecampuspojo.entity.CommentEntity;
import com.notfound.timecampuspojo.vo.NoteVO;
import com.notfound.timecampuspojo.vo.TimelineItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    CommentEntity findById(Long id);

    int insert(CommentEntity entity);

    int updateReview(@Param("id") Long id,
                     @Param("reviewStatus") String reviewStatus,
                     @Param("rejectReason") String rejectReason,
                     @Param("reviewerId") Long reviewerId);

    List<CommentEntity> listByTarget(@Param("targetType") String targetType,
                                     @Param("targetId") Long targetId,
                                     @Param("reviewStatus") String reviewStatus);

    List<CommentEntity> listByUser(@Param("userId") Long userId,
                                   @Param("reviewStatus") String reviewStatus);

    List<CommentEntity> listForAdmin(@Param("targetType") String targetType,
                                     @Param("targetId") Long targetId,
                                     @Param("reviewStatus") String reviewStatus);

    List<NoteVO> listMyNotes(@Param("userId") Long userId,
                             @Param("poiId") Long poiId,
                             @Param("reviewStatus") String reviewStatus);

    int deleteNoteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<TimelineItemVO> listTimelineNotes(@Param("userId") Long userId,
                                           @Param("poiId") Long poiId,
                                           @Param("yearFrom") int yearFrom,
                                           @Param("yearTo") int yearTo);
}
