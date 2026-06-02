package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampusserver.config.TencentMapProperties;
import com.notfound.timecampusserver.service.TencentRouteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TencentRouteServiceImpl implements TencentRouteService {

    private final TencentMapProperties tencentMapProperties;
    private final TencentMapSignature tencentMapSignature;
    private final RestClient restClient;
    private final String walkingRouteUrl;

    public TencentRouteServiceImpl(TencentMapProperties tencentMapProperties,
                                   TencentMapSignature tencentMapSignature,
                                   RestClient.Builder restClientBuilder,
                                   @Value("${tencent-map.walking-route-url:https://apis.map.qq.com/ws/direction/v1/walking/}")
                                   String walkingRouteUrl) {
        this.tencentMapProperties = tencentMapProperties;
        this.tencentMapSignature = tencentMapSignature;
        this.restClient = restClientBuilder.build();
        this.walkingRouteUrl = walkingRouteUrl;
    }

    @Override
    public WalkingRoutePlan planWalkingRoute(List<RoutePoint> points) {
        validatePoints(points);
        requireTencentKey();

        List<WalkingRouteLeg> legs = new ArrayList<>();
        int totalDistance = 0;
        int totalDuration = 0;
        for (int index = 0; index + 1 < points.size(); index++) {
            WalkingRouteLeg leg = routeLeg(points.get(index), points.get(index + 1));
            legs.add(leg);
            totalDistance += leg.distanceMeters();
            totalDuration += leg.durationSeconds();
        }
        return new WalkingRoutePlan("walking", "tencent-map", totalDistance, totalDuration, legs);
    }

    private WalkingRouteLeg routeLeg(RoutePoint from, RoutePoint to) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("key", tencentMapProperties.key());
        params.put("from", coordinate(from));
        params.put("to", coordinate(to));
        URI uri = tencentMapSignature.buildSignedUri(walkingRouteUrl, params, tencentMapProperties.sk());
        Map<String, Object> response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        Map<String, Object> route = firstRoute(response);
        return new WalkingRouteLeg(
                from,
                to,
                intValue(route.get("distance")),
                intValue(route.get("duration")),
                route.get("polyline"),
                route
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> firstRoute(Map<String, Object> response) {
        if (response == null) {
            throw new BizException(ResultCode.BIZ_ERROR, "tencent route response is empty");
        }
        Object status = response.get("status");
        if (status instanceof Number number && number.intValue() != 0) {
            throw new BizException(ResultCode.BIZ_ERROR,
                    "tencent route error: " + response.getOrDefault("message", status));
        }
        Object result = response.get("result");
        if (!(result instanceof Map<?, ?> rawResult)) {
            throw new BizException(ResultCode.BIZ_ERROR, "tencent route result is empty");
        }
        Object routes = rawResult.get("routes");
        if (!(routes instanceof List<?> routeList) || routeList.isEmpty()) {
            throw new BizException(ResultCode.BIZ_ERROR, "tencent route list is empty");
        }
        Object route = routeList.get(0);
        if (!(route instanceof Map<?, ?> rawRoute)) {
            throw new BizException(ResultCode.BIZ_ERROR, "tencent route format is invalid");
        }
        return (Map<String, Object>) rawRoute;
    }

    private void validatePoints(List<RoutePoint> points) {
        if (points == null || points.size() < 2) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "at least two route points are required");
        }
        if (points.size() > 8) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "route points cannot exceed 8");
        }
        for (RoutePoint point : points) {
            if (point == null
                    || point.lat() < -90 || point.lat() > 90
                    || point.lng() < -180 || point.lng() > 180) {
                throw new BizException(ResultCode.VALIDATION_ERROR, "invalid route point");
            }
        }
    }

    private String coordinate(RoutePoint point) {
        return point.lat() + "," + point.lng();
    }

    private int intValue(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private void requireTencentKey() {
        if (tencentMapProperties.key() == null || tencentMapProperties.key().isBlank()) {
            throw new BizException(ResultCode.BIZ_ERROR, "tencent map key not configured");
        }
    }
}
