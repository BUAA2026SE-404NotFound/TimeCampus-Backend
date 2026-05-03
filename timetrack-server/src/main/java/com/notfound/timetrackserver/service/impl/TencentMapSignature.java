package com.notfound.timetrackserver.service.impl;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@Component
public class TencentMapSignature {

    public URI buildSignedUri(String baseUrl, LinkedHashMap<String, String> params, String sk) {
        URI base = URI.create(baseUrl);
        Map<String, String> sortedParams = new TreeMap<>(params);
        String query = toQueryString(sortedParams);
        if (sk != null && !sk.isBlank()) {
            String sig = sign(base.getRawPath(), toRawQueryString(sortedParams), sk);
            query = query + "&sig=" + sig;
        }
        return URI.create(baseUrl + "?" + query);
    }

    String sign(String path, String query, String sk) {
        return md5(path + "?" + query + sk);
    }

    String toQueryString(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!builder.isEmpty()) {
                builder.append('&');
            }
            builder.append(entry.getKey())
                    .append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    String toRawQueryString(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!builder.isEmpty()) {
                builder.append('&');
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return builder.toString();
    }

    private String md5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }
}
