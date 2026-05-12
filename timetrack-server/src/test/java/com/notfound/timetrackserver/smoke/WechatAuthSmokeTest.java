package com.notfound.timetrackserver.smoke;

import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackserver.config.WechatProperties;
import com.notfound.timetrackserver.service.impl.WechatAuthServiceImpl;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WechatAuthSmokeTest {

    @Test
    void wechatCode2SessionRespondsWhenSmokeEnabled() {
        Assumptions.assumeTrue("true".equalsIgnoreCase(System.getenv("RUN_WECHAT_SMOKE")),
                "Set RUN_WECHAT_SMOKE=true with WECHAT_APPID and WECHAT_SECRET to run this third-party smoke test.");
        String appid = requireEnv("WECHAT_APPID");
        String secret = requireEnv("WECHAT_SECRET");

        WechatAuthServiceImpl service = new WechatAuthServiceImpl(new WechatProperties(appid, secret));

        assertThatThrownBy(() -> service.code2SessionOpenId("invalid-smoke-code"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("wechat code2Session failed");
    }

    private String requireEnv(String name) {
        String value = System.getenv(name);
        Assumptions.assumeTrue(value != null && !value.isBlank(), name + " is required for smoke test.");
        return value;
    }
}
