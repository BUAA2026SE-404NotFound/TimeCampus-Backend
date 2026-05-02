package com.notfound.timetrackpojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "微信登录请求")
public class WechatLoginRequest {

    @NotBlank(message = "code cannot be blank")
    @Schema(description = "微信登录临时码（小程序 wx.login 获取）", example = "021xYyyy0abcDEFghijkLmNopQrStuVw")
    private String code;

    @Schema(description = "用户昵称（可选，用于首次创建或更新用户信息）", example = "张三")
    private String nickname;

    @Schema(description = "用户头像 URL（可选）", example = "https://example.com/avatar.png")
    private String avatarUrl;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
