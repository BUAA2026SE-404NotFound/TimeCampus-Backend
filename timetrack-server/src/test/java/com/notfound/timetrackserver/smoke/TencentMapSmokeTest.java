package com.notfound.timetrackserver.smoke;

import com.notfound.timetrackserver.service.impl.TencentMapSignature;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TencentMapSmokeTest {

    @Test
    void tencentPlaceSearchRespondsWhenSmokeEnabled() {
        Assumptions.assumeTrue("true".equalsIgnoreCase(System.getenv("RUN_TENCENT_SMOKE")),
                "Set RUN_TENCENT_SMOKE=true with TENCENT_MAP_KEY and TENCENT_MAP_SK to run this third-party smoke test.");
        String key = requireEnv("TENCENT_MAP_KEY");
        String sk = requireEnv("TENCENT_MAP_SK");

        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("key", key);
        params.put("keyword", "图书馆");
        params.put("boundary", "region(北京,0)");
        URI uri = new TencentMapSignature().buildSignedUri("https://apis.map.qq.com/ws/place/v1/search", params, sk);

        Map<String, Object> body = RestClient.create()
                .get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assertThat(body).isNotNull();
        assertThat(body).containsKey("status");
        assertThat(String.valueOf(body.get("status"))).isIn("0", "110", "111", "112");
    }

    private String requireEnv(String name) {
        String value = System.getenv(name);
        Assumptions.assumeTrue(value != null && !value.isBlank(), name + " is required for smoke test.");
        return value;
    }
}
