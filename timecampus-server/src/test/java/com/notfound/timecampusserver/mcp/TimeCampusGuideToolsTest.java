package com.notfound.timecampusserver.mcp;

import com.notfound.timecampuspojo.vo.MapHomeVO;
import com.notfound.timecampuspojo.vo.MapPoiVO;
import com.notfound.timecampusserver.service.MapService;
import com.notfound.timecampusserver.service.TencentRouteService;
import com.notfound.timecampusserver.service.TencentRouteService.RoutePoint;
import com.notfound.timecampusserver.service.TencentRouteService.WalkingRoutePlan;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TimeCampusGuideToolsTest {

    @Test
    void delegatesSearchAndRouteToBackendServices() {
        MapService mapService = mock(MapService.class);
        TencentRouteService routeService = mock(TencentRouteService.class);
        MapPoiVO mainBuilding = poi(1L, "主楼", "校园中轴");
        MapPoiVO gym = poi(2L, "体育馆", "体育设施");
        MapHomeVO home = new MapHomeVO();
        home.setPois(List.of(mainBuilding, gym));
        when(mapService.getPortalHome(null)).thenReturn(home);
        List<RoutePoint> points = List.of(
                new RoutePoint("主楼", 39.981, 116.3447),
                new RoutePoint("体育馆", 39.983, 116.346)
        );
        WalkingRoutePlan plan = new WalkingRoutePlan("walking", "tencent-map", 300, 240, List.of());
        when(routeService.planWalkingRoute(points)).thenReturn(plan);

        TimeCampusGuideTools tools = new TimeCampusGuideTools(mapService, routeService);

        assertThat(tools.searchPublicPois("中轴")).containsExactly(mainBuilding);
        assertThat(tools.walkingRoute(points)).isSameAs(plan);
        verify(routeService).planWalkingRoute(points);
    }

    private MapPoiVO poi(long id, String name, String description) {
        MapPoiVO poi = new MapPoiVO();
        poi.setId(id);
        poi.setName(name);
        poi.setDescription(description);
        return poi;
    }
}
