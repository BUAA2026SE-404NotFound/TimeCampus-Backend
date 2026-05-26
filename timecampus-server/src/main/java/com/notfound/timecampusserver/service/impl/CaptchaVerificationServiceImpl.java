package com.notfound.timecampusserver.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampusserver.config.CapProperties;
import com.notfound.timecampusserver.service.CaptchaVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class CaptchaVerificationServiceImpl implements CaptchaVerificationService {

    private static final Logger log = LoggerFactory.getLogger(CaptchaVerificationServiceImpl.class);

    private final CapProperties properties;
    private final RestClient restClient;

    @Autowired
    public CaptchaVerificationServiceImpl(CapProperties properties) {
        this(properties, createRestClient(properties));
    }

    CaptchaVerificationServiceImpl(CapProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    public void verifyLoginToken(String capToken) {
        if (!properties.enabled()) {
            return;
        }
        if (capToken == null || capToken.isBlank()) {
            throw new BizException(ResultCode.VALIDATION_ERROR, "capToken cannot be blank");
        }
        if (isBlank(properties.siteverifyUrl()) || isBlank(properties.secret())) {
            log.error("Cap captcha is enabled but siteverifyUrl or secret is missing");
            throw new BizException(ResultCode.INTERNAL_ERROR, "captcha service is not configured");
        }

        try {
            CapVerifyResponse response = restClient.post()
                    .uri(properties.siteverifyUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CapVerifyRequest(properties.secret(), capToken))
                    .retrieve()
                    .body(CapVerifyResponse.class);
            if (response == null || !response.success()) {
                throw new BizException(ResultCode.UNAUTHORIZED, "captcha verification failed");
            }
        } catch (RestClientException ex) {
            log.warn("Cap siteverify request failed", ex);
            throw new BizException(ResultCode.UNAUTHORIZED, "captcha verification failed");
        }
    }

    private static RestClient createRestClient(CapProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        requestFactory.setReadTimeout((int) properties.readTimeout().toMillis());
        return RestClient.builder().requestFactory(requestFactory).build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record CapVerifyRequest(String secret, String response) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record CapVerifyResponse(boolean success) {
    }
}
