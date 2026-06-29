package com.notfound.timecampusserver.service;

import com.notfound.timecampusserver.config.TencentMapProperties;
import com.notfound.timecampusserver.service.TencentRouteService.RoutePoint;
import com.notfound.timecampusserver.service.TencentRouteService.WalkingRoutePlan;
import com.notfound.timecampusserver.service.impl.TencentMapSignature;
import com.notfound.timecampusserver.service.impl.TencentRouteServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TencentRouteServiceImplTest {

    @Test
    void planWalkingRouteAggregatesLegsAndConvertsDurationToSeconds() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TencentRouteServiceImpl service = new TencentRouteServiceImpl(
                new TencentMapProperties("test-key", "", null, null),
                new TencentMapSignature(),
                builder,
                "https://apis.map.qq.com/ws/direction/v1/walking/"
        );

        server.expect(once(), requestTo("https://apis.map.qq.com/ws/direction/v1/walking/?from=39.981%2C116.34&key=test-key&to=39.982%2C116.341"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(routeJson(320, 5), MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("https://apis.map.qq.com/ws/direction/v1/walking/?from=39.982%2C116.341&key=test-key&to=39.983%2C116.342"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(routeJson(280, 4), MediaType.APPLICATION_JSON));

        WalkingRoutePlan plan = service.planWalkingRoute(List.of(
                new RoutePoint("主楼", 39.981, 116.34),
                new RoutePoint("图书馆", 39.982, 116.341),
                new RoutePoint("校门", 39.983, 116.342)
        ));

        assertThat(plan.totalDistanceMeters()).isEqualTo(600);
        assertThat(plan.totalDurationSeconds()).isEqualTo(540);
        assertThat(plan.legs()).hasSize(2);
        assertThat(plan.legs().get(0).durationSeconds()).isEqualTo(300);
        assertThat(plan.legs().get(0).path()).containsExactly(
                new TencentRouteService.RouteCoordinate(39.981, 116.34),
                new TencentRouteService.RouteCoordinate(39.982, 116.341)
        );
        server.verify();
    }

    private String routeJson(int distanceMeters, int durationMinutes) {
        return """
                {
                  "status": 0,
                  "message": "query ok",
                  "result": {
                    "routes": [
                      {
                        "mode": "WALKING",
                        "distance": %d,
                        "duration": %d,
                        "polyline": [39.981, 116.34, 1000, 1000],
                        "steps": []
                      }
                    ]
                  }
                }
                """.formatted(distanceMeters, durationMinutes);
    }
}
