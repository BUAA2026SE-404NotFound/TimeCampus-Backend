package com.notfound.timecampusserver.security;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuscommon.web.TokenUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;

@Component
public class UserAuthInterceptor implements HandlerInterceptor {

    public static final String USER_TOKEN_PREFIX = "user:token:";
    public static final Duration USER_TOKEN_TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    public UserAuthInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String issueToken(Long userId) {
        String token = TokenUtil.generateToken();
        redisTemplate.opsForValue().set(USER_TOKEN_PREFIX + token, String.valueOf(userId), USER_TOKEN_TTL);
        return token;
    }

    /**
     * Resolves the current user id when a valid Bearer token is present.
     * Returns null for anonymous requests or invalid/expired tokens.
     */
    public Long resolveUserId(HttpServletRequest request) {
        String token = TokenUtil.resolveBearerToken(request);
        if (token == null) {
            return null;
        }
        String userIdStr = redisTemplate.opsForValue().get(USER_TOKEN_PREFIX + token);
        if (userIdStr == null || userIdStr.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String token = TokenUtil.resolveBearerToken(request);
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
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        UserContext.clear();
    }
}

