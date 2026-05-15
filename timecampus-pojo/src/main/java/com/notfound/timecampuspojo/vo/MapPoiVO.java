package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "地图 POI 展示模型（首页打点用）")
public class MapPoiVO {
    @Schema(description = "地点 ID（poi.id）", example = "1")
    private Long id;
    @Schema(description = "地点名称", example = "主楼")
    private String name;
    @Schema(description = "纬度", example = "39.90420000")
    private BigDecimal latitude;
    @Schema(description = "经度", example = "116.40740000")
    private BigDecimal longitude;
    @Schema(description = "上架状态：1 上架，0 下架", example = "1")
    private Integer status;
    @Schema(description = "地点简介（可选）", example = "北航标志性建筑之一。")
    private String description;
    @Schema(description = "封面图片路径（根据 year 选择最接近年份的官方已通过影像）", example = "https://example.com/images/poi-1-2000.jpg")
    private String coverImagePath;
    @Schema(description = "该地点可用年份列表（官方已通过影像的年份集合）", example = "[1952, 1980, 2000]")
    private List<Integer> availableYears;

    @Schema(description = "该地点关联的历史影像列表（用于地点信息卡片展示）")
    private List<MapMediaVO> mediaList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public List<Integer> getAvailableYears() {
        return availableYears;
    }

    public void setAvailableYears(List<Integer> availableYears) {
        this.availableYears = availableYears;
    }

    public List<MapMediaVO> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<MapMediaVO> mediaList) {
        this.mediaList = mediaList;
    }
}

