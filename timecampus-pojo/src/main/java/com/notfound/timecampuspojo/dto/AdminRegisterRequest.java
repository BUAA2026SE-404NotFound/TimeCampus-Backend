package com.notfound.timecampuspojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "管理员注册请求（需通过人机验证）")
public class AdminRegisterRequest {

    @NotBlank(message = "adminName cannot be blank")
    @Schema(description = "管理员登录名（admin.admin_name）", example = "operator")
    private String adminName;

    @NotBlank(message = "password cannot be blank")
    @Schema(description = "管理员密码（明文提交，服务端保存哈希）", example = "P@ssw0rd")
    private String password;

    @NotBlank(message = "capToken cannot be blank")
    @Schema(description = "Cap 前端验证码 token；注册必填", example = "cap-token-from-widget")
    private String capToken;

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

    public String getCapToken() {
        return capToken;
    }

    public void setCapToken(String capToken) {
        this.capToken = capToken;
    }
}
