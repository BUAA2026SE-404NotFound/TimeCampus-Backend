package com.notfound.timetrackpojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "创建地点（POI）请求")
public class PoiCreateRequest {

    @NotBlank(message = "name cannot be blank")
    @Schema(description = "地点名称", example = "主楼")
    private String name;

    @NotNull(message = "latitude cannot be null")
    @Schema(description = "纬度", example = "39.90420000")
    private BigDecimal latitude;

    @NotNull(message = "longitude cannot be null")
    @Schema(description = "经度", example = "116.40740000")
    private BigDecimal longitude;

    @Schema(description = "地点简介（可选）", example = "北航标志性建筑之一。")
    private String description;
    @Schema(description = "冷知识/小故事（可选）", example = "传说这里曾经……")
    private String funFact;
    @Schema(description = "上架状态：1 上架，0 下架（可选，默认 1）", example = "1")
    private Integer status;

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
}

