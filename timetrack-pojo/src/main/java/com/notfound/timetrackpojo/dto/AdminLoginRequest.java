package com.notfound.timetrackpojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "管理员登录请求")
public class AdminLoginRequest {

    @NotBlank(message = "adminName cannot be blank")
    @Schema(description = "管理员登录名（admin.admin_name）", example = "admin")
    private String adminName;

    @NotBlank(message = "password cannot be blank")
    @Schema(description = "管理员密码（明文提交，服务端校验哈希）", example = "123456")
    private String password;

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

