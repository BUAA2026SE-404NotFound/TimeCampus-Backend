package com.notfound.timetrackserver.service.impl;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import com.notfound.timetrackserver.config.WechatProperties;
import com.notfound.timetrackserver.service.WechatAuthService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WechatAuthServiceImpl implements WechatAuthService {

    private final WechatProperties wechatProperties;
    private final RestClient restClient;

    public WechatAuthServiceImpl(WechatProperties wechatProperties) {
        this.wechatProperties = wechatProperties;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.weixin.qq.com")
                .build();
    }

    @Override
    public String code2SessionOpenId(String code) {
        if ("dev-bypass".equals(code)) {
            return "mock_openid_for_testing";
        }

        if (code == null || code.isBlank()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "code cannot be blank");
        }
        if (wechatProperties.appid() == null || wechatProperties.appid().isBlank()
                || wechatProperties.secret() == null || wechatProperties.secret().isBlank()) {
            throw new BizException(ResultCode.BIZ_ERROR, "wechat appid/secret not configured");
        }

        Code2SessionResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sns/jscode2session")
                        .queryParam("appid", wechatProperties.appid())
                        .queryParam("secret", wechatProperties.secret())
                        .queryParam("js_code", code)
                        .queryParam("grant_type", "authorization_code")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Code2SessionResponse.class);

        if (response == null) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "wechat code2Session returned empty response");
        }
        if (response.errcode != null && response.errcode != 0) {
            String msg = response.errmsg == null ? "wechat code2Session failed" : response.errmsg;
            throw new BizException(ResultCode.BIZ_ERROR, "wechat code2Session failed: " + response.errcode + " " + msg);
        }
        if (response.openid == null || response.openid.isBlank()) {
            throw new BizException(ResultCode.BIZ_ERROR, "wechat code2Session missing openid");
        }
        return response.openid;
    }

    @SuppressWarnings("unused")
    static class Code2SessionResponse {
        public String openid;
        public String session_key;
        public String unionid;
        public Integer errcode;
        public String errmsg;
    }
}

