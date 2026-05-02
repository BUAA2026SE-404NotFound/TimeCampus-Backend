package com.notfound.timetrackpojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "用户 UGC 上传请求")
public class UgcUploadRequest {

    @NotNull(message = "poiId cannot be null")
    @Schema(description = "关联地点 ID", example = "1")
    private Long poiId;

    @NotNull(message = "year cannot be null")
    @Schema(description = "拍摄年份", example = "2023")
    private Integer year;

    @Schema(description = "影像说明", example = "2023年主楼雪景")
    private String description;

    // getters & setters
    public Long getPoiId() { return poiId; }
    public void setPoiId(Long poiId) { this.poiId = poiId; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
