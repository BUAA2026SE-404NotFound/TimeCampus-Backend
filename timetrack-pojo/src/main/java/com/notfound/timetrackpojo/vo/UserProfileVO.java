package com.notfound.timetrackpojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户基本资料")
public class UserProfileVO {
    @Schema(description = "用户 ID（user.id）", example = "1")
    private Long id;
    @Schema(description = "用户昵称", example = "TimeTrack User")
    private String nickname;
    @Schema(description = "用户头像 URL", example = "https://example.com/avatar.png")
    private String avatarUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
