package com.notfound.timecampuspojo.dto;

import com.notfound.timecampuspojo.constant.ReviewStatuses;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@Schema(description = "影像元数据更新请求")
public class MediaMetadataUpdateRequest {

    @Schema(description = "关联地点 ID（可选，不传则保持不变）", example = "1")
    private Long poiId;

    @Schema(description = "图片路径或 URL（可选，不传则保持不变）", example = "https://example.com/images/poi-1-2000.jpg")
    private String imagePath;

    @Min(value = 1953, message = "year must be >= 1953")
    @Max(value = 2100, message = "year must be <= 2100")
    @Schema(description = "拍摄年份（可选，不传则保持不变）", example = "2000")
    private Integer year;

    @Schema(description = "影像说明；传空字符串可清空说明，不传则保持不变", example = "主楼 2000 年旧照")
    private String description;

    @Pattern(regexp = ReviewStatuses.PATTERN, message = "reviewStatus must be pending, approved or rejected")
    @Schema(description = "审核状态（可选）：pending/approved/rejected；不传则保持不变", example = "approved")
    private String reviewStatus;

    public Long getPoiId() {
        return poiId;
    }

    public void setPoiId(Long poiId) {
        this.poiId = poiId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }
}
