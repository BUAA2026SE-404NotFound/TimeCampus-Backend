package com.notfound.timecampusserver.controller.publicapi;

import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampuspojo.vo.MapHomeVO;
import com.notfound.timecampusserver.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Portal-Map", description = "项目主页：公开校园地图数据")
@RestController
@RequestMapping("/api/v1/portal/map")
public class PortalMapController {

    private final MapService mapService;

    public PortalMapController(MapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/home")
    @Operation(summary = "项目主页地图 POI", description = "无需登录，返回已上架且存在已审核官方影像的 POI、年份与公开影像预览 URL。")
    public ApiResponse<MapHomeVO> home(
            @Parameter(description = "目标年份（可选，用于选择最接近年份的封面图）", example = "2000")
            @RequestParam(required = false) Integer year) {
        return ApiResponse.success(mapService.getPortalHome(year));
    }
}
