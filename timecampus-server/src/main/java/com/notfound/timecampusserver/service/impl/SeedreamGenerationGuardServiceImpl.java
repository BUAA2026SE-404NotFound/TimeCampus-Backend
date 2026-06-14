package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampusserver.ai.SeedreamImageProperties;
import com.notfound.timecampusserver.service.CaptchaVerificationService;
import com.notfound.timecampusserver.service.SeedreamGenerationGuardService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class SeedreamGenerationGuardServiceImpl implements SeedreamGenerationGuardService {

    private static final ZoneId DAILY_LIMIT_ZONE = ZoneId.of("Asia/Shanghai");
    private static final String LIMIT_KEY_PREFIX = "timecampus:seedream:generation:";

    private final CaptchaVerificationService captchaVerificationService;
    private final StringRedisTemplate redisTemplate;
    private final SeedreamImageProperties properties;

    public SeedreamGenerationGuardServiceImpl(CaptchaVerificationService captchaVerificationService,
                                              StringRedisTemplate redisTemplate,
                                              SeedreamImageProperties properties) {
        this.captchaVerificationService = captchaVerificationService;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public void verifyBeforeGenerate(String capToken, String clientIp) {
        captchaVerificationService.verifySeedreamGenerationToken(capToken);
        consumeDailyQuota(clientIp);
    }

    private void consumeDailyQuota(String clientIp) {
        int dailyLimit = properties.getDailyIpLimit();
        String key = LIMIT_KEY_PREFIX + LocalDate.now(DAILY_LIMIT_ZONE) + ":" + normalizeClientIp(clientIp);
        try {
            Long currentCount = redisTemplate.opsForValue().increment(key);
            if (currentCount == null) {
                throw new BizException(ResultCode.INTERNAL_ERROR, "Seedream generation quota is unavailable");
            }
            redisTemplate.expire(key, ttlUntilNextDay());
            if (currentCount > dailyLimit) {
                throw new BizException(ResultCode.TOO_MANY_REQUESTS, "同一 IP 每天最多生成 %d 次".formatted(dailyLimit));
            }
        } catch (DataAccessException ex) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "Seedream generation quota is unavailable");
        }
    }

    private Duration ttlUntilNextDay() {
        return Duration.between(
                java.time.ZonedDateTime.now(DAILY_LIMIT_ZONE),
                LocalDate.now(DAILY_LIMIT_ZONE).plusDays(1).atStartOfDay(DAILY_LIMIT_ZONE)
        );
    }

    private String normalizeClientIp(String clientIp) {
        return StringUtils.hasText(clientIp) ? clientIp.trim().replace(':', '_') : "unknown";
    }
}
