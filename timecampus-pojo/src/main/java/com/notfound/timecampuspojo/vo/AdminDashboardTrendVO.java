package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理员仪表盘趋势点")
public class AdminDashboardTrendVO {

    @Schema(description = "日期，格式 MM-dd")
    private String date;

    @Schema(description = "新增媒体数")
    private Integer mediaCount;

    @Schema(description = "新增 UGC 数")
    private Integer ugcCount;

    @Schema(description = "新增评论数")
    private Integer commentCount;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getMediaCount() {
        return mediaCount;
    }

    public void setMediaCount(Integer mediaCount) {
        this.mediaCount = mediaCount;
    }

    public Integer getUgcCount() {
        return ugcCount;
    }

    public void setUgcCount(Integer ugcCount) {
        this.ugcCount = ugcCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }
}
