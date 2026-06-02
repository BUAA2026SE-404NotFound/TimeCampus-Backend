package com.notfound.timecampusserver.controller.user;

import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampusserver.service.TencentRouteService;
import com.notfound.timecampusserver.service.TencentRouteService.RoutePoint;
import com.notfound.timecampusserver.service.TencentRouteService.WalkingRoutePlan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Map-Route", description = "公开地图路线规划")
@RestController
@RequestMapping("/api/v1/map")
public class MapRouteController {

    private final TencentRouteService tencentRouteService;

    public MapRouteController(TencentRouteService tencentRouteService) {
        this.tencentRouteService = tencentRouteService;
    }

    @PostMapping("/walking-route")
    @Operation(summary = "腾讯地图步行路线规划", description = "按顺序为多个 POI 逐段规划步行路线。")
    public ApiResponse<WalkingRoutePlan> walkingRoute(@Valid @RequestBody WalkingRouteRequest request) {
        return ApiResponse.success(tencentRouteService.planWalkingRoute(request.points()));
    }

    public record WalkingRouteRequest(@NotEmpty List<RoutePoint> points) {
    }
}
