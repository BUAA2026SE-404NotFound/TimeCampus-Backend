package com.notfound.timecampusserver.service.impl;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class TencentMapSignatureTest {

    private final TencentMapSignature signature = new TencentMapSignature();

    @Test
    void signUsesTencentPathQueryAndSkRule() {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("address", "北京市");
        params.put("key", "test-key");

        String query = signature.toQueryString(params);
        String sig = signature.sign("/ws/geocoder/v1/", signature.toRawQueryString(params), "test-sk");

        assertThat(query).isEqualTo("address=%E5%8C%97%E4%BA%AC%E5%B8%82&key=test-key");
        assertThat(sig).isEqualTo("ff6c0e076aec2b94b49a54a2b8934833");
    }

    @Test
    void buildSignedUriAppendsSigAtTheEnd() {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("key", "test-key");
        params.put("keyword", "图书馆");
        params.put("boundary", "region(北京,0)");

        var uri = signature.buildSignedUri("https://apis.map.qq.com/ws/place/v1/search", params, "test-sk");

        assertThat(uri.toString()).contains("boundary=region%28");
        assertThat(uri.toString()).contains("&key=test-key&keyword=");
        assertThat(uri.toString()).endsWith("&sig=b18ba68976c588c7ff2fff926c662214");
    }
}
