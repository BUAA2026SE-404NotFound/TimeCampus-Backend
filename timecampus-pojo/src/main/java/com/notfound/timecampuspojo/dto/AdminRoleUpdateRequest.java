package com.notfound.timecampuspojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "管理员角色调整请求")
public class AdminRoleUpdateRequest {

    @NotBlank(message = "role cannot be blank")
    @Schema(description = "角色：admin / read / none；super 不能通过接口直接授予", example = "read")
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
