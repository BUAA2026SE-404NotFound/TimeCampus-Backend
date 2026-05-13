package com.notfound.timetrackserver.controller.user;

import com.notfound.timetrackcommon.api.ApiResponse;
import com.notfound.timetrackpojo.vo.MapHomeVO;
import com.notfound.timetrackpojo.vo.MapMediaVO;
import com.notfound.timetrackserver.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Map", description = "小程序地图相关接口")
@RestController
@RequestMapping("/api/v1/map")
public class MapController {

    private final MapService mapService;

    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/home")
    @Operation(summary = "地图首页聚合", description = "返回地图打点所需的 POI 基础信息，以及官方已通过影像的可用年份与封面图。")
    public ApiResponse<MapHomeVO> home(@Parameter(description = "目标年份（可选，用于选择最接近年份的封面图）", example = "2000")
                                       @RequestParam(required = false) Integer year) {
        return ApiResponse.success(mapService.getHome(year));
    }

    @GetMapping("/poi/{poiId}/timemachine")
    @Operation(summary = "时空切换核心接口", description = "获取指定地点在目标年份最接近的历史影像")
    public ApiResponse<MapMediaVO> getTimeMachineMedia(
            @Parameter(description = "地点 ID", required = true) @PathVariable Long poiId,
            @Parameter(description = "目标年份（可选，不传则返回最新影像）") @RequestParam(required = false) Integer year) {
        return ApiResponse.success(mapService.getTimeMachineMedia(poiId, year));
    }

    @GetMapping("/reverse-geocode")
    @Operation(summary = "腾讯地图逆地理编码")
    public ApiResponse<Map<String, Object>> reverseGeocode(@RequestParam Double lat, @RequestParam Double lng) {
        return ApiResponse.success(mapService.reverseGeocode(lat, lng));
    }

    @GetMapping("/poi-search")
    @Operation(summary = "腾讯地图地点搜索")
    public ApiResponse<Map<String, Object>> poiSearch(@RequestParam String keyword,
                                                      @RequestParam(required = false) String region) {
        return ApiResponse.success(mapService.poiSearch(keyword, region));
    }
}
