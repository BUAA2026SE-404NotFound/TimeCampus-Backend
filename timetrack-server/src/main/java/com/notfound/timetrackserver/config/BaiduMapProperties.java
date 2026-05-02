package com.notfound.timetrackserver.config;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "baidu.map")
public record BaiduMapProperties(String ak, String geocoderUrl, String placeSearchUrl) {
}

