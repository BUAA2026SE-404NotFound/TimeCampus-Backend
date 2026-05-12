package com.notfound.timetrackpojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "地点关联的历史影像（用于地点卡片展示）")
public class MapMediaVO {
    @Schema(description = "影像 ID（media.id）", example = "1")
    private Long id;

    @Schema(description = "拍摄年份（media.year）", example = "2000")
    private Integer year;

    @Schema(description = "图片路径（media.image_path）", example = "https://example.com/images/poi-1-2000.jpg")
    private String imagePath;

    @Schema(description = "影像说明（media.description）", example = "主楼 2000 年旧照")
    private String description;

    @Schema(description = "类型：official/ugc（media.type）", example = "official")
    private String type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

