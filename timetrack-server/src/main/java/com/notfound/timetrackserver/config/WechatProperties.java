package com.notfound.timetrackserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wechat")
public record WechatProperties(String appid, String secret) {
}

