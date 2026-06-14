package com.notfound.timecampuspojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理端地图总览")
public class AdminMapOverviewVO {
    private List<AdminMapPoiVO> pois = new ArrayList<>();
    private List<AdminMapFavoriteVO> recentFavorites = new ArrayList<>();
    private List<AdminMapCommentVO> recentComments = new ArrayList<>();

    public List<AdminMapPoiVO> getPois() {
        return pois;
    }

    public void setPois(List<AdminMapPoiVO> pois) {
        this.pois = pois;
    }

    public List<AdminMapFavoriteVO> getRecentFavorites() {
        return recentFavorites;
    }

    public void setRecentFavorites(List<AdminMapFavoriteVO> recentFavorites) {
        this.recentFavorites = recentFavorites;
    }

    public List<AdminMapCommentVO> getRecentComments() {
        return recentComments;
    }

    public void setRecentComments(List<AdminMapCommentVO> recentComments) {
        this.recentComments = recentComments;
    }
}
