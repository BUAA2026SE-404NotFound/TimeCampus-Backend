package com.notfound.timetrackserver.security;

import com.notfound.timetrackcommon.api.ResultCode;
import com.notfound.timetrackcommon.exception.BizException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.UUID;

@Component
public class UserAuthInterceptor implements HandlerInterceptor {

    public static final String USER_TOKEN_PREFIX = "user:token:";
    public static final Duration USER_TOKEN_TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    public UserAuthInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String issueToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(USER_TOKEN_PREFIX + token, String.valueOf(userId), USER_TOKEN_TTL);
        return token;
    }

    public void revokeToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        redisTemplate.delete(USER_TOKEN_PREFIX + token);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = resolveBearerToken(request);
        if (token == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "missing user token");
        }
        String userIdStr = redisTemplate.opsForValue().get(USER_TOKEN_PREFIX + token);
        if (userIdStr == null || userIdStr.isBlank()) {
            throw new BizException(ResultCode.UNAUTHORIZED, "invalid or expired user token");
        }
        try {
            UserContext.setUserId(Long.parseLong(userIdStr));
        } catch (NumberFormatException e) {
            throw new BizException(ResultCode.UNAUTHORIZED, "invalid user token payload");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
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

