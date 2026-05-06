package com.notfound.timetrackpojo.dto;

import com.notfound.timetrackpojo.constant.TargetTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "用户评论创建请求")
public class CommentCreateRequest {

    @NotBlank(message = "targetType cannot be blank")
    @Pattern(regexp = TargetTypes.PATTERN, message = "targetType must be poi or media")
    @Schema(description = "评论目标类型", example = "poi", allowableValues = {"poi", "media"})
    private String targetType;

    @NotNull(message = "targetId cannot be null")
    @Schema(description = "评论目标 ID", example = "1")
    private Long targetId;

    @NotBlank(message = "content cannot be blank")
    @Size(max = 1000, message = "content length must be <= 1000")
    @Schema(description = "评论内容", example = "这里的历史照片很有意思")
    private String content;

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
