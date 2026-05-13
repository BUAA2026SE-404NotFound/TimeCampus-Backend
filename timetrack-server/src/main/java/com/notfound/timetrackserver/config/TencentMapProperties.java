package com.notfound.timetrackserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tencent-map")
public record TencentMapProperties(String key, String sk, String geocoderUrl, String placeSearchUrl) {
}
