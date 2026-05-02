package com.notfound.timetrackserver.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackserver.config.BaiduMapProperties;
import com.notfound.timetrackserver.service.BaiduMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BaiduMapServiceImpl implements BaiduMapService {

    private static final Logger log = LoggerFactory.getLogger(BaiduMapServiceImpl.class);

    private final BaiduMapProperties baiduMapProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public BaiduMapServiceImpl(BaiduMapProperties baiduMapProperties) {
        this.baiduMapProperties = baiduMapProperties;
//        this.restClient = RestClient.builder().build();
        this.restClient = RestClient.builder()
                .defaultHeader("Referer", "http://localhost")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public BigDecimal[] geocoding(String address) {
        if (address == null || address.isBlank()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "address cannot be blank");
        }
        String url = UriComponentsBuilder.fromHttpUrl(baiduMapProperties.geocoderUrl())
                .queryParam("address", address)
                .queryParam("output", "json")
                .queryParam("ak", baiduMapProperties.ak())
                .toUriString();

        String response = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            int status = root.path("status").asInt();
            if (status != 0) {
                String msg = root.path("message").asText("unknown error");
                log.error("Geocoding failed: status={}, msg={}", status, msg);
                throw new BizException(ResultCode.BIZ_ERROR, "百度地理编码失败: " + msg);
            }
            JsonNode location = root.path("result").path("location");
            BigDecimal lat = new BigDecimal(location.path("lat").asText());
            BigDecimal lng = new BigDecimal(location.path("lng").asText());
            return new BigDecimal[]{lat, lng};
        } catch (Exception e) {
            log.error("Geocoding parse error", e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "解析百度响应失败");
        }
    }

    @Override
    public String reverseGeocoding(BigDecimal lat, BigDecimal lng) {
        if (lat == null || lng == null) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "lat/lng cannot be null");
        }
        String coord = lat.toPlainString() + "," + lng.toPlainString();
        String url = UriComponentsBuilder.fromHttpUrl(baiduMapProperties.geocoderUrl())
                .queryParam("location", coord)
                .queryParam("output", "json")
                .queryParam("ak", baiduMapProperties.ak())
                .toUriString();

        String response = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            int status = root.path("status").asInt();
            if (status != 0) {
                String msg = root.path("message").asText("unknown error");
                log.error("Reverse geocoding failed: status={}, msg={}", status, msg);
                throw new BizException(ResultCode.BIZ_ERROR, "百度逆地理编码失败: " + msg);
            }
            return root.path("result").path("formatted_address").asText();
        } catch (Exception e) {
            log.error("Reverse geocoding parse error", e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "解析百度响应失败");
        }
    }

    @Override
    public List<Map<String, Object>> placeSearch(String query, BigDecimal lat, BigDecimal lng, int radius) {
        if (query == null || query.isBlank()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "query cannot be blank");
        }
        if (lat == null || lng == null) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "lat/lng cannot be null");
        }
        String location = lat.toPlainString() + "," + lng.toPlainString();
        String url = UriComponentsBuilder.fromHttpUrl(baiduMapProperties.placeSearchUrl())
                .queryParam("query", query)
                .queryParam("location", location)
                .queryParam("radius", radius)
                .queryParam("output", "json")
                .queryParam("ak", baiduMapProperties.ak())
                .toUriString();

        String response = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            int status = root.path("status").asInt();
            if (status != 0) {
                String msg = root.path("message").asText("unknown error");
                log.error("Place search failed: status={}, msg={}", status, msg);
                throw new BizException(ResultCode.BIZ_ERROR, "百度地点检索失败: " + msg);
            }
            List<Map<String, Object>> results = new ArrayList<>();
            JsonNode resultsNode = root.path("results");
            for (JsonNode item : resultsNode) {
                Map<String, Object> poi = Map.of(
                        "name", item.path("name").asText(),
                        "address", item.path("address").asText(),
                        "lat", new BigDecimal(item.path("location").path("lat").asText()),
                        "lng", new BigDecimal(item.path("location").path("lng").asText())
                );
                results.add(poi);
            }
            return results;
        } catch (Exception e) {
            log.error("Place search parse error", e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "解析百度响应失败");
        }
    }
}
