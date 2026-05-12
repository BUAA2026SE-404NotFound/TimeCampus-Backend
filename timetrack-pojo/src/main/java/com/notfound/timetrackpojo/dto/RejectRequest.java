package com.notfound.timetrackpojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "驳回请求")
public class RejectRequest {

    @NotBlank(message = "rejectReason cannot be blank")
    @Schema(description = "驳回原因", example = "图片不清晰")
    private String rejectReason;

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
