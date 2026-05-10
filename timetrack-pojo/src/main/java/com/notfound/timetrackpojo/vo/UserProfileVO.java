package com.notfound.timetrackpojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "用户基本资料")
public class UserProfileVO {
    @Schema(description = "用户 ID（user.id）", example = "1")
    private Long id;
    @Schema(description = "用户昵称", example = "TimeTrack User")
    private String nickname;
    @Schema(description = "用户头像 URL", example = "https://example.com/avatar.png")
    private String avatarUrl;
    @Schema(description = "用户身份（user.identity）", example = "STUDENT")
    private String identity;
    @Schema(description = "入学年份（user.enroll_year）", example = "2022")
    private Integer enrollYear;
    @Schema(description = "创建时间（user.create_time）", example = "2026-05-10T17:00:00")
    private LocalDateTime createTime;
    @Schema(description = "更新时间（user.update_time）", example = "2026-05-10T17:00:00")
    private LocalDateTime updateTime;

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

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Integer getEnrollYear() {
        return enrollYear;
    }

    public void setEnrollYear(Integer enrollYear) {
        this.enrollYear = enrollYear;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
