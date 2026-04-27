package com.notfound.timetrackpojo.dto;

import jakarta.validation.constraints.NotBlank;

public class WechatLoginRequest {

    @NotBlank(message = "code cannot be blank")
    private String code;

    private String nickname;
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
