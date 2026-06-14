package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "地点（POI）信息")
public class PoiVO {
    @Schema(description = "地点 ID（poi.id）", example = "1")
    private Long id;
    @Schema(description = "地点名称", example = "主楼")
    private String name;
    @Schema(description = "纬度", example = "39.90420000")
    private BigDecimal latitude;
    @Schema(description = "经度", example = "116.40740000")
    private BigDecimal longitude;
    @Schema(description = "地点简介", example = "北航标志性建筑之一。")
    private String description;
    @Schema(description = "冷知识/小故事", example = "传说这里曾经……")
    private String funFact;
    @Schema(description = "上架状态：1 上架，0 下架", example = "1")
    private Integer status;
    @Schema(description = "创建时间", example = "2026-04-28T18:00:00")
    private LocalDateTime createTime;
    @Schema(description = "更新时间", example = "2026-04-28T18:00:00")
    private LocalDateTime updateTime;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFunFact() {
        return funFact;
    }

    public void setFunFact(String funFact) {
        this.funFact = funFact;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

