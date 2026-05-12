package com.notfound.timetrackpojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户登录响应")
public class UserLoginVO {

    @Schema(description = "用户 token（后续需要登录态的接口需放到 Authorization: Bearer <token>）")
    private String token;

    @Schema(description = "用户资料")
    private UserProfileVO profile;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserProfileVO getProfile() {
        return profile;
    }

    public void setProfile(UserProfileVO profile) {
        this.profile = profile;
    }
}

