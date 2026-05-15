package com.notfound.timecampusserver.mapper;

import com.notfound.timecampuspojo.entity.MediaEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MediaMapper {

    MediaEntity findById(Long id);

    int insert(MediaEntity entity);

    int insertBatch(@Param("list") List<MediaEntity> list);

    int deleteById(Long id);

    int updateReview(@Param("id") Long id,
                     @Param("reviewStatus") String reviewStatus,
                     @Param("rejectReason") String rejectReason,
                     @Param("reviewerId") Long reviewerId);

    List<MediaEntity> list(@Param("poiId") Long poiId,
                           @Param("type") String type,
                           @Param("reviewStatus") String reviewStatus,
                           @Param("yearFrom") Integer yearFrom,
                           @Param("yearTo") Integer yearTo);

    List<MediaEntity> listByPoiIds(@Param("poiIds") List<Long> poiIds,
                                   @Param("type") String type,
                                   @Param("reviewStatus") String reviewStatus);

    List<MediaEntity> listByIds(@Param("ids") List<Long> ids);

    List<MediaEntity> listByUploadUser(@Param("uploadUserId") Long uploadUserId,
                                       @Param("type") String type,
                                       @Param("reviewStatus") String reviewStatus);

    MediaEntity findBestByPoiAndYear(@Param("poiId") Long poiId,
                                     @Param("year") Integer year,
                                     @Param("type") String type,
                                     @Param("reviewStatus") String reviewStatus);
}
