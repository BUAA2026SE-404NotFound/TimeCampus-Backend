package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "用户提交内容的审核结果汇总")
public class UserReviewResultVO {
    @Schema(description = "用户 ID", example = "1")
    private Long userId;
    @Schema(description = "筛选的审核状态；为空表示全部", example = "pending")
    private String reviewStatus;
    @Schema(description = "用户上传的 UGC 影像审核结果")
    private List<MediaVO> ugcItems;
    @Schema(description = "用户评论审核结果")
    private List<CommentVO> comments;
    @Schema(description = "UGC 数量", example = "2")
    private int ugcCount;
    @Schema(description = "评论数量", example = "3")
    private int commentCount;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public List<MediaVO> getUgcItems() {
        return ugcItems;
    }

    public void setUgcItems(List<MediaVO> ugcItems) {
        this.ugcItems = ugcItems;
    }

    public List<CommentVO> getComments() {
        return comments;
    }

    public void setComments(List<CommentVO> comments) {
        this.comments = comments;
    }

    public int getUgcCount() {
        return ugcCount;
    }

    public void setUgcCount(int ugcCount) {
        this.ugcCount = ugcCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
}
