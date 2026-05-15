package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理端地图 POI 概览")
public class AdminMapPoiVO {
    private Long id;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private Integer status;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer pendingCommentCount;
    private Integer mediaCount;
    private Integer ugcCount;
    private List<AdminMapFavoriteVO> favorites = new ArrayList<>();
    private List<AdminMapCommentVO> comments = new ArrayList<>();
    private List<AdminMapMediaVO> mediaList = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getPendingCommentCount() {
        return pendingCommentCount;
    }

    public void setPendingCommentCount(Integer pendingCommentCount) {
        this.pendingCommentCount = pendingCommentCount;
    }

    public Integer getMediaCount() {
        return mediaCount;
    }

    public void setMediaCount(Integer mediaCount) {
        this.mediaCount = mediaCount;
    }

    public Integer getUgcCount() {
        return ugcCount;
    }

    public void setUgcCount(Integer ugcCount) {
        this.ugcCount = ugcCount;
    }

    public List<AdminMapFavoriteVO> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<AdminMapFavoriteVO> favorites) {
        this.favorites = favorites;
    }

    public List<AdminMapCommentVO> getComments() {
        return comments;
    }

    public void setComments(List<AdminMapCommentVO> comments) {
        this.comments = comments;
    }

    public List<AdminMapMediaVO> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<AdminMapMediaVO> mediaList) {
        this.mediaList = mediaList;
    }
}
