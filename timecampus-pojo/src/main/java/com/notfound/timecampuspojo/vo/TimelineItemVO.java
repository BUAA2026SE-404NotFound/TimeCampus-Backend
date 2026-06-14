package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "时间线聚合条目")
public class TimelineItemVO {

    @Schema(description = "内容 ID")
    private Long id;

    @Schema(description = "内容类型：official / ugc / comment")
    private String type;

    @Schema(description = "所属地点 ID")
    private Long poiId;

    @Schema(description = "地点名称")
    private String poiName;

    @Schema(description = "内容所属年份")
    private Integer year;

    @Schema(description = "标题，可为空")
    private String title;

    @Schema(description = "内容摘要")
    private String description;

    @Schema(description = "图片预览地址，笔记可为空")
    private String previewUrl;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** Internal mapper field; cleared before API response. */
    private String imagePath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getPoiId() {
        return poiId;
    }

    public void setPoiId(Long poiId) {
        this.poiId = poiId;
    }

    public String getPoiName() {
        return poiName;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
