package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.vo.MapHomeVO;
import com.notfound.timecampuspojo.vo.MapMediaVO;

import java.util.Map;

public interface MapService {

    MapHomeVO getHome(Integer year);

    MapHomeVO getPortalHome(Integer year);

    MapMediaVO getTimeMachineMedia(Long poiId, Integer year);

    Map<String, Object> reverseGeocode(Double lat, Double lng);

    Map<String, Object> poiSearch(String keyword, String region);
}
