package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "管理员账户信息")
public class AdminAccountVO {

    @Schema(description = "管理员主键 ID（admin.id）", example = "1")
    private Long id;

    @Schema(description = "管理员登录名（admin.admin_name）", example = "operator")
    private String adminName;

    @Schema(description = "角色：super / normal", example = "normal")
    private String role;

    @Schema(description = "状态：1 启用，0 禁用", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

