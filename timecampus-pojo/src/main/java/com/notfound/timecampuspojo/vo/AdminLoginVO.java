package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理员登录响应")
public class AdminLoginVO {

    @Schema(description = "管理员 token（后续访问 /api/v1/admin/** 需放到 Authorization: Bearer <token>）")
    private String token;

    @Schema(description = "管理员主键 ID（admin.id）", example = "1")
    private Long adminId;

    @Schema(description = "管理员登录名（admin.admin_name）", example = "admin")
    private String adminName;

    @Schema(description = "角色：super / admin / read / none", example = "admin")
    private String role;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
