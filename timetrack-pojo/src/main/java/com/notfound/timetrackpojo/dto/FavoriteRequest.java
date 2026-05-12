package com.notfound.timetrackpojo.dto;
import com.notfound.timetrackpojo.constant.TargetTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "收藏/取消收藏请求")
public class FavoriteRequest {
    @NotNull(message = "targetId cannot be null")
    @Schema(description = "目标 ID（地点 ID 或影像 ID）", example = "1")
    private Long targetId;

    @NotBlank(message = "targetType cannot be blank")
    @Pattern(regexp = TargetTypes.PATTERN, message = "targetType must be poi or media")
    @Schema(description = "目标类型：poi / media", example = "poi", allowableValues = {"poi", "media"})
    private String targetType;

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

}
