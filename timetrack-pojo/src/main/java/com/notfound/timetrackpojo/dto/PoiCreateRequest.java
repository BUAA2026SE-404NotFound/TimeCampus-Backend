package com.notfound.timetrackpojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "创建地点（POI）请求")
public class PoiCreateRequest {

    @NotBlank(message = "name cannot be blank")
    @Schema(description = "地点名称", example = "主楼")
    private String name;

    @NotNull(message = "latitude cannot be null")
    @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "latitude must be <= 90")
    @Schema(description = "纬度", example = "39.90420000")
    private BigDecimal latitude;

    @NotNull(message = "longitude cannot be null")
    @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "longitude must be <= 180")
    @Schema(description = "经度", example = "116.40740000")
    private BigDecimal longitude;

    @Schema(description = "地点简介（可选）", example = "北航标志性建筑之一。")
    private String description;
    @Schema(description = "冷知识/小故事（可选）", example = "传说这里曾经……")
    private String funFact;
    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
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
