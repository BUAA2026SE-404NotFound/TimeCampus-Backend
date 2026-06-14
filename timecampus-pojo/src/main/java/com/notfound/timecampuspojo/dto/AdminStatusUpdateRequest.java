package com.notfound.timecampuspojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理员启用/禁用请求")
public class AdminStatusUpdateRequest {

    @NotNull(message = "enabled cannot be null")
    @Schema(description = "是否启用（true 启用，false 禁用）", example = "true")
    private Boolean enabled;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}

