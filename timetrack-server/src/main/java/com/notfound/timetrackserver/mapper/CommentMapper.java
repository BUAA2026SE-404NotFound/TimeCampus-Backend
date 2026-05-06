package com.notfound.timetrackserver.mapper;

import com.notfound.timetrackpojo.entity.CommentEntity;
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
}
