package com.notfound.timecampusserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public record StorageProperties(String localRootDir, Long maxFileSizeMb) {
}
