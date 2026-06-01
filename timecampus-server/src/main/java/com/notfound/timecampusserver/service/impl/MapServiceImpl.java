package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuspojo.entity.MediaEntity;
import com.notfound.timecampuspojo.entity.PoiEntity;
import com.notfound.timecampuspojo.vo.MapHomeVO;
import com.notfound.timecampuspojo.vo.MapMediaVO;
import com.notfound.timecampuspojo.vo.MapPoiVO;
import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampusserver.config.TencentMapProperties;
import com.notfound.timecampusserver.mapper.MediaMapper;
import com.notfound.timecampusserver.mapper.PoiMapper;
import com.notfound.timecampusserver.service.MapService;
import com.notfound.timecampusserver.service.MediaFileService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Service;

import java.net.URI;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MapServiceImpl implements MapService {

    private static final String TYPE_OFFICIAL = "official";
    private static final String REVIEW_APPROVED = "approved";

    private final PoiMapper poiMapper;
    private final MediaMapper mediaMapper;
    private final TencentMapProperties tencentMapProperties;
    private final RestClient restClient;
    private final TencentMapSignature tencentMapSignature;
    private final MediaFileService mediaFileService;

    public MapServiceImpl(PoiMapper poiMapper,
                          MediaMapper mediaMapper,
                          TencentMapProperties tencentMapProperties,
                          TencentMapSignature tencentMapSignature,
                          MediaFileService mediaFileService) {
        this.poiMapper = poiMapper;
        this.mediaMapper = mediaMapper;
        this.tencentMapProperties = tencentMapProperties;
        this.tencentMapSignature = tencentMapSignature;
        this.mediaFileService = mediaFileService;
        this.restClient = RestClient.create();
    }

    @Override
    public MapHomeVO getHome(Integer year) {
        List<PoiEntity> pois = poiMapper.list(1, null);
        MapHomeVO homeVO = new MapHomeVO();
        if (pois.isEmpty()) {
            homeVO.setPois(List.of());
            return homeVO;
        }

        List<Long> poiIds = pois.stream().map(PoiEntity::getId).collect(Collectors.toList());
        List<MediaEntity> mediaList = mediaMapper.listByPoiIds(poiIds, TYPE_OFFICIAL, REVIEW_APPROVED);

        Map<Long, List<MediaEntity>> mediaByPoi = new HashMap<>();
        for (MediaEntity media : mediaList) {
            mediaByPoi.computeIfAbsent(media.getPoiId(), k -> new ArrayList<>()).add(media);
        }

        List<MapPoiVO> mapPois = new ArrayList<>();
        for (PoiEntity poi : pois) {
            MapPoiVO vo = new MapPoiVO();
            vo.setId(poi.getId());
            vo.setName(poi.getName());
            vo.setLatitude(poi.getLatitude());
            vo.setLongitude(poi.getLongitude());
            vo.setStatus(poi.getStatus());
            vo.setDescription(poi.getDescription());

            List<MediaEntity> list = mediaByPoi.getOrDefault(poi.getId(), List.of());
            vo.setAvailableYears(list.stream().map(MediaEntity::getYear).distinct().sorted().collect(Collectors.toList()));
            MediaEntity cover = selectCover(list, year);
            String coverPreviewUrl = previewUrl(cover);
            vo.setCoverImagePath(coverPreviewUrl);
            vo.setCoverPreviewUrl(coverPreviewUrl);
            vo.setCoverThumbnailUrl(thumbnailUrl(cover));
            vo.setMediaList(list.stream().map(this::toMapMediaVO).collect(Collectors.toList()));

            mapPois.add(vo);
        }

        homeVO.setPois(mapPois);
        return homeVO;
    }

    @Override
    public MapMediaVO getTimeMachineMedia(Long poiId, Integer year) {
        PoiEntity poi = poiMapper.findById(poiId);
        if (poi == null) {
            throw new BizException(ResultCode.NOT_FOUND, "poi not found: " + poiId);
        }
        if (poi.getStatus() == null || poi.getStatus() != 1) {
            throw new BizException(ResultCode.BIZ_ERROR, "poi is inactive: " + poiId);
        }
        MediaEntity media = year == null
                ? mediaMapper.list(poiId, TYPE_OFFICIAL, REVIEW_APPROVED, null, null)
                        .stream()
                        .max(Comparator.comparing(MediaEntity::getYear)
                                .thenComparing(MediaEntity::getId))
                        .orElse(null)
                : mediaMapper.findBestByPoiAndYear(poiId, year, TYPE_OFFICIAL, REVIEW_APPROVED);
        if (media == null) {
            throw new BizException(ResultCode.NOT_FOUND, "content not found for poi: " + poiId);
        }
        return toMapMediaVO(media);
    }

    @Override
    public Map<String, Object> reverseGeocode(Double lat, Double lng) {
        if (lat == null || lng == null || lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "invalid lat/lng");
        }
        requireTencentKey();
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("key", tencentMapProperties.key());
        params.put("location", lat + "," + lng);
        params.put("get_poi", "1");
        URI uri = tencentMapSignature.buildSignedUri(tencentMapProperties.geocoderUrl(), params, tencentMapProperties.sk());
        Map<String, Object> response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        enrichCoordinateMeta(response, lat, lng);
        return response;
    }

    @Override
    public Map<String, Object> poiSearch(String keyword, String region) {
        if (keyword == null || keyword.isBlank()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "keyword is required");
        }
        requireTencentKey();
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("key", tencentMapProperties.key());
        params.put("keyword", keyword);
        params.put("boundary", "region(" + (region == null || region.isBlank() ? "全国" : region) + ",0)");
        URI uri = tencentMapSignature.buildSignedUri(tencentMapProperties.placeSearchUrl(), params, tencentMapProperties.sk());
        Map<String, Object> response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        enrichPoiSearchCoordinates(response);
        return response;
    }

    private void enrichCoordinateMeta(Map<String, Object> response,
                                      double gcj02Lat,
                                      double gcj02Lng) {
        if (response == null) {
            return;
        }
        response.put("coordinateSystem", "GCJ02");
        response.put("requestLocation", coordinateMap(gcj02Lat, gcj02Lng));
    }

    @SuppressWarnings("unchecked")
    private void enrichPoiSearchCoordinates(Map<String, Object> response) {
        if (response == null) {
            return;
        }
        Object results = response.get("results");
        if (!(results instanceof List<?> list)) {
            return;
        }
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> rawItem)) {
                continue;
            }
            Map<String, Object> result = (Map<String, Object>) rawItem;
            Object location = result.get("location");
            if (!(location instanceof Map<?, ?> rawLocation)) {
                continue;
            }
            Object latValue = rawLocation.get("lat");
            Object lngValue = rawLocation.get("lng");
            if (!(latValue instanceof Number latNumber) || !(lngValue instanceof Number lngNumber)) {
                continue;
            }
            result.put("coordinateSystem", "GCJ02");
            result.put("gcj02Location", coordinateMap(latNumber.doubleValue(), lngNumber.doubleValue()));
        }
    }

    private Map<String, Object> coordinateMap(double lat, double lng) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lat", lat);
        result.put("lng", lng);
        return result;
    }

    private MediaEntity selectCover(List<MediaEntity> list, Integer year) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        if (year == null) {
            return list.getFirst();
        }
        return list.stream()
                .min(Comparator.comparingInt(m -> Math.abs(m.getYear() - year)))
                .orElse(list.getFirst());
    }

    private MapMediaVO toMapMediaVO(MediaEntity entity) {
        MapMediaVO vo = new MapMediaVO();
        vo.setId(entity.getId());
        vo.setYear(entity.getYear());
        String previewUrl = previewUrl(entity);
        vo.setImagePath(previewUrl);
        vo.setPreviewUrl(previewUrl);
        vo.setThumbnailUrl(thumbnailUrl(entity));
        vo.setDescription(entity.getDescription());
        vo.setType(entity.getType());
        return vo;
    }

    private String previewUrl(MediaEntity entity) {
        return entity == null ? null : mediaFileService.previewUrl(entity.getId(), entity.getImagePath());
    }

    private String thumbnailUrl(MediaEntity entity) {
        return entity == null ? null : mediaFileService.previewUrl(entity.getId(), entity.getImagePath(), 192);
    }

    private void requireTencentKey() {
        if (tencentMapProperties.key() == null || tencentMapProperties.key().isBlank()) {
            throw new BizException(ResultCode.BIZ_ERROR, "tencent map key not configured");
        }
    }
}
