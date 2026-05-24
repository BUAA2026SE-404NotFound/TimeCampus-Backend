package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "影像（media）信息")
public class MediaVO {
    @Schema(description = "影像 ID（media.id）", example = "1")
    private Long id;
    @Schema(description = "关联地点 ID（media.poi_id）", example = "1")
    private Long poiId;
    @Schema(description = "类型：official/ugc（media.type）", example = "official")
    private String type;
    @Schema(description = "前端可访问的图片 URL；不暴露服务端存储路径", example = "https://example.com/images/poi-1-2000.jpg")
    private String imagePath;
    @Schema(description = "前端可访问的预览 URL")
    private String previewUrl;
    @Schema(description = "拍摄年份（media.year）", example = "2000")
    private Integer year;
    @Schema(description = "影像说明（可选）", example = "主楼 2000 年旧照")
    private String description;
    @Schema(description = "UGC 上传者用户 ID（media.upload_user_id；官方一般为 null）", example = "12")
    private Long uploadUserId;
    @Schema(description = "审核状态：pending/approved/rejected（media.review_status）", example = "approved")
    private String reviewStatus;
    @Schema(description = "驳回原因（media.reject_reason）", example = "图片不清晰")
    private String rejectReason;
    @Schema(description = "审核时间（media.review_time）", example = "2026-04-28T18:00:00")
    private LocalDateTime reviewTime;
    @Schema(description = "审核人 ID（media.reviewer_id）", example = "1")
    private Long reviewerId;
    @Schema(description = "创建时间（media.create_time）", example = "2026-04-28T18:00:00")
    private LocalDateTime createTime;
    @Schema(description = "更新时间（media.update_time）", example = "2026-04-28T18:00:00")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPoiId() {
        return poiId;
    }

    public void setPoiId(Long poiId) {
        this.poiId = poiId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUploadUserId() {
        return uploadUserId;
    }

    public void setUploadUserId(Long uploadUserId) {
        this.uploadUserId = uploadUserId;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
