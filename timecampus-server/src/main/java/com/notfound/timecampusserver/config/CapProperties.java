package com.notfound.timecampusserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "cap")
public record CapProperties(
        boolean enabled,
        String siteverifyUrl,
        String secret,
        Duration connectTimeout,
        Duration readTimeout
) {
    public CapProperties {
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(2);
        }
        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(5);
        }
    }
}
