package com.notfound.timecampusserver.mapper;

import com.notfound.timecampuspojo.vo.AdminMapCommentVO;
import com.notfound.timecampuspojo.vo.AdminMapFavoriteVO;
import com.notfound.timecampuspojo.vo.AdminMapMediaVO;
import com.notfound.timecampuspojo.vo.AdminMapPoiVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMapMapper {
    List<AdminMapPoiVO> listPoiOverview(@Param("status") Integer status, @Param("keyword") String keyword);

    List<AdminMapFavoriteVO> listRecentFavorites(@Param("limit") Integer limit);

    List<AdminMapCommentVO> listRecentComments(@Param("reviewStatus") String reviewStatus, @Param("limit") Integer limit);

    List<AdminMapFavoriteVO> listPoiFavorites(@Param("poiIds") List<Long> poiIds);

    List<AdminMapCommentVO> listPoiComments(@Param("poiIds") List<Long> poiIds,
                                            @Param("reviewStatus") String reviewStatus);

    List<AdminMapMediaVO> listPoiMedia(@Param("poiIds") List<Long> poiIds);
}
