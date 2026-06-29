package com.notfound.timecampusserver.mcp;

import com.notfound.timecampuspojo.vo.MapPoiVO;
import com.notfound.timecampusserver.service.MapService;
import com.notfound.timecampusserver.service.TencentRouteService;
import com.notfound.timecampusserver.service.TencentRouteService.RoutePoint;
import com.notfound.timecampusserver.service.TencentRouteService.WalkingRoutePlan;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class TimeCampusGuideTools {

    private final MapService mapService;
    private final TencentRouteService routeService;

    public TimeCampusGuideTools(MapService mapService, TencentRouteService routeService) {
        this.mapService = mapService;
        this.routeService = routeService;
    }

    @McpTool(
            name = "timecampus_public_poi_search",
            description = "查询公开发布的 TimeCampus 校园 POI、简介和历史影像资料。涉及景点资料时必须先调用本工具。",
            annotations = @McpTool.McpAnnotations(
                    title = "Search Public TimeCampus POIs",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false))
    public List<MapPoiVO> searchPublicPois(
            @McpToolParam(description = "景点名称或简介关键字；为空返回全部公开 POI", required = false)
            String keyword) {
        List<MapPoiVO> pois = mapService.getPortalHome(null).getPois();
        if (keyword == null || keyword.isBlank()) {
            return pois;
        }
        String query = keyword.strip().toLowerCase(Locale.ROOT);
        return pois.stream()
                .filter(poi -> contains(poi.getName(), query) || contains(poi.getDescription(), query))
                .toList();
    }

    @McpTool(
            name = "timecampus_walking_route",
            description = "调用腾讯地图为 2 到 8 个 GCJ-02 校园点位规划确定性的步行路线；LLM 不得自行计算路径。",
            annotations = @McpTool.McpAnnotations(
                    title = "Plan TimeCampus Walking Route",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = true))
    public WalkingRoutePlan walkingRoute(
            @McpToolParam(description = "按游览顺序排列的 2 到 8 个点位", required = true)
            List<RoutePoint> points) {
        return routeService.planWalkingRoute(points);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
