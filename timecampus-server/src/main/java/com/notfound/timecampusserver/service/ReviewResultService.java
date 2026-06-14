package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.vo.UserReviewResultVO;
import com.notfound.timecampuspojo.vo.CommentVO;
import com.notfound.timecampuspojo.vo.MediaVO;

import java.util.List;

public interface ReviewResultService {

    UserReviewResultVO listByUser(Long userId, String reviewStatus);

    List<MediaVO> listUgcByUser(Long userId, String reviewStatus);

    List<CommentVO> listCommentsByUser(Long userId, String reviewStatus);
}
