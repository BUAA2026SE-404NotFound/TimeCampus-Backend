package com.notfound.timecampusserver.security;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    public static final String ADMIN_TOKEN_PREFIX = "admin:token:";
    public static final Duration ADMIN_TOKEN_TTL = Duration.ofHours(2);

    private final StringRedisTemplate redisTemplate;

    public AdminAuthInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String issueToken(Long adminId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(ADMIN_TOKEN_PREFIX + token, String.valueOf(adminId), ADMIN_TOKEN_TTL);
        return token;
    }

    public void revokeToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        redisTemplate.delete(ADMIN_TOKEN_PREFIX + token);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = resolveBearerToken(request);
        if (token == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing admin token");
        }
        String adminIdStr = redisTemplate.opsForValue().get(ADMIN_TOKEN_PREFIX + token);
        if (adminIdStr == null || adminIdStr.isBlank()) {
            throw new BizException(ResultCode.UNAUTHORIZED, "invalid or expired admin token");
        }
        try {
            AdminContext.setAdminId(Long.parseLong(adminIdStr));
        } catch (NumberFormatException e) {
            throw new BizException(ResultCode.UNAUTHORIZED, "invalid admin token payload");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AdminContext.clear();
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || auth.isBlank()) {
            return null;
        }
        String prefix = "Bearer ";
        if (!auth.startsWith(prefix)) {
            return null;
        }
        String token = auth.substring(prefix.length()).trim();
        return token.isBlank() ? null : token;
    }
}

