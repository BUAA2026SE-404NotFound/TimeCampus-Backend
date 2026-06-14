package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "收藏列表项（带目标对象简要信息）")
public class FavoriteItemVO {

    @Schema(description = "收藏记录 ID")
    private Long favoriteId;

    @Schema(description = "目标类型：poi / media")
    private String targetType;

    @Schema(description = "目标 ID")
    private Long targetId;

    @Schema(description = "收藏时间")
    private LocalDateTime createTime;

    // 以下字段根据 targetType 动态填充
    @Schema(description = "地点名称（当 targetType=poi 时）")
    private String poiName;

    @Schema(description = "地点简介（当 targetType=poi 时）")
    private String poiDescription;

    @Schema(description = "影像年份（当 targetType=media 时）")
    private Integer mediaYear;

    @Schema(description = "影像图片路径（当 targetType=media 时）")
    private String mediaImagePath;

    @Schema(description = "影像说明（当 targetType=media 时）")
    private String mediaDescription;

    // getters & setters
    public Long getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(Long favoriteId) {
        this.favoriteId = favoriteId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getPoiName() {
        return poiName;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    public String getPoiDescription() {
        return poiDescription;
    }

    public void setPoiDescription(String poiDescription) {
        this.poiDescription = poiDescription;
    }

    public Integer getMediaYear() {
        return mediaYear;
    }

    public void setMediaYear(Integer mediaYear) {
        this.mediaYear = mediaYear;
    }

    public String getMediaImagePath() {
        return mediaImagePath;
    }

    public void setMediaImagePath(String mediaImagePath) {
        this.mediaImagePath = mediaImagePath;
    }

    public String getMediaDescription() {
        return mediaDescription;
    }

    public void setMediaDescription(String mediaDescription) {
        this.mediaDescription = mediaDescription;
    }

}
