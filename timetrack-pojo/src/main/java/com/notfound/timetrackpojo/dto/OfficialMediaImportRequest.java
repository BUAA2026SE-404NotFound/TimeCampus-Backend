package com.notfound.timetrackpojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "官方历史影像批量导入请求")
public class OfficialMediaImportRequest {

    @NotEmpty(message = "items cannot be empty")
    @Valid
    @Schema(description = "导入条目列表")
    private List<OfficialMediaItem> items;

    public List<OfficialMediaItem> getItems() {
        return items;
    }

    public void setItems(List<OfficialMediaItem> items) {
        this.items = items;
    }

    public static class OfficialMediaItem {
        @Schema(description = "关联地点 ID（media.poi_id）", example = "1")
        private Long poiId;
        @Schema(description = "图片路径（media.image_path）", example = "https://example.com/images/poi-1-2000.jpg")
        private String imagePath;
        @Schema(description = "拍摄年份（media.year）", example = "2000")
        private Integer year;
        @Schema(description = "影像说明（可选）", example = "主楼 2000 年旧照")
        private String description;
        @Schema(description = "审核状态（可选）：pending/approved/rejected；不填则后端默认 approved", example = "approved")
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
}

