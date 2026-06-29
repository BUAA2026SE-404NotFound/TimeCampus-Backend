package com.notfound.timecampusserver.service;

import java.util.List;
import java.util.Map;

public interface TencentRouteService {

    WalkingRoutePlan planWalkingRoute(List<RoutePoint> points);

    record RoutePoint(String name, double lat, double lng) {
    }

    record RouteCoordinate(double lat, double lng) {
    }

    record WalkingRouteLeg(RoutePoint from,
                           RoutePoint to,
                           int distanceMeters,
                           int durationSeconds,
                           Object polyline,
                           List<RouteCoordinate> path,
                           Map<String, Object> rawRoute) {
    }

    record WalkingRoutePlan(String mode,
                            String provider,
                            int totalDistanceMeters,
                            int totalDurationSeconds,
                            List<WalkingRouteLeg> legs) {
    }
}
