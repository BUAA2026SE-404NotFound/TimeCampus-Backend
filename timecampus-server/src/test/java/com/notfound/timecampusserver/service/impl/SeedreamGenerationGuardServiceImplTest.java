package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampusserver.ai.SeedreamImageProperties;
import com.notfound.timecampusserver.service.CaptchaVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SeedreamGenerationGuardServiceImplTest {

    @Test
    void verifiesCaptchaAndConsumesFirstDailyQuota() {
        CaptchaVerificationService captchaVerificationService = mock(CaptchaVerificationService.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = valueOperations(redisTemplate);
        when(valueOperations.increment(any(String.class))).thenReturn(1L);

        SeedreamGenerationGuardServiceImpl guardService = new SeedreamGenerationGuardServiceImpl(
                captchaVerificationService,
                redisTemplate,
                new SeedreamImageProperties()
        );

        guardService.verifyBeforeGenerate("cap-token", "203.0.113.10");

        verify(captchaVerificationService).verifySeedreamGenerationToken("cap-token");
        verify(valueOperations).increment(startsWith("timecampus:seedream:generation:"));
        verify(redisTemplate).expire(startsWith("timecampus:seedream:generation:"), any(Duration.class));
    }

    @Test
    void rejectsWhenIpExceedsDailyQuota() {
        CaptchaVerificationService captchaVerificationService = mock(CaptchaVerificationService.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = valueOperations(redisTemplate);
        when(valueOperations.increment(any(String.class))).thenReturn(6L);

        SeedreamGenerationGuardServiceImpl guardService = new SeedreamGenerationGuardServiceImpl(
                captchaVerificationService,
                redisTemplate,
                new SeedreamImageProperties()
        );

        assertThatThrownBy(() -> guardService.verifyBeforeGenerate("cap-token", "203.0.113.10"))
                .isInstanceOf(BizException.class)
                .satisfies(ex -> assertThat(((BizException) ex).getResultCode()).isEqualTo(ResultCode.TOO_MANY_REQUESTS))
                .hasMessage("同一 IP 每天最多生成 5 次");
    }

    @SuppressWarnings("unchecked")
    private ValueOperations<String, String> valueOperations(StringRedisTemplate redisTemplate) {
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        return valueOperations;
    }
}
