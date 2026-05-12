package com.notfound.timetrackpojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "地图首页聚合响应")
public class MapHomeVO {
    @Schema(description = "地点列表（用于地图打点）")
    private List<MapPoiVO> pois;

    public List<MapPoiVO> getPois() {
        return pois;
    }

    public void setPois(List<MapPoiVO> pois) {
        this.pois = pois;
    }
}

