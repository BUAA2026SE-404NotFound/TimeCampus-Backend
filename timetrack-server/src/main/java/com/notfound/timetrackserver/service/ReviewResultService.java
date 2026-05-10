package com.notfound.timetrackserver.service;

import com.notfound.timetrackpojo.vo.UserReviewResultVO;
import com.notfound.timetrackpojo.vo.CommentVO;
import com.notfound.timetrackpojo.vo.MediaVO;

import java.util.List;

public interface ReviewResultService {

    UserReviewResultVO listByUser(Long userId, String reviewStatus);

    List<MediaVO> listUgcByUser(Long userId, String reviewStatus);

    List<CommentVO> listCommentsByUser(Long userId, String reviewStatus);
}
