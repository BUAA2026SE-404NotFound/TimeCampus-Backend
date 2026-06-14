package com.notfound.timecampuspojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "笔记创建请求")
public class CreateNoteRequest {

    @NotNull(message = "poiId cannot be null")
    @Schema(description = "POI 地点 ID", example = "1")
    private Long poiId;

    @NotBlank(message = "content cannot be blank")
    @Size(max = 1000, message = "content length must be <= 1000")
    @Schema(description = "笔记内容", example = "今天来这里打卡了，风景很美")
    private String content;

    public Long getPoiId() {
        return poiId;
    }

    public void setPoiId(Long poiId) {
        this.poiId = poiId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}