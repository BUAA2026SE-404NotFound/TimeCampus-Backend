package com.notfound.timetrackserver.service;

import com.notfound.timetrackpojo.vo.MapHomeVO;

import java.util.Map;

public interface MapService {

    MapHomeVO getHome(Integer year);

    Map<String, Object> reverseGeocode(Double lat, Double lng);

    Map<String, Object> poiSearch(String keyword, String region);
}
