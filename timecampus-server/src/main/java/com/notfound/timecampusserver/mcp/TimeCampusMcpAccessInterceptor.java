package com.notfound.timecampusserver.mcp;

import com.notfound.timecampusserver.security.AdminContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class TimeCampusMcpAccessInterceptor implements HandlerInterceptor {

    private final TimeCampusMcpProperties properties;

    public TimeCampusMcpAccessInterceptor(TimeCampusMcpProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if (properties.isAuthRequired()) {
            String expected = properties.getToken();
            if (expected == null || expected.isBlank()) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "TimeCampus MCP token is not configured");
                return false;
            }
            String actual = resolveToken(request);
            if (!constantTimeEquals(expected, actual)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "invalid TimeCampus MCP token");
                return false;
            }
        }
        AdminContext.setAdminId(properties.getAdminId());
        AdminContext.setAdminRole(properties.getAdminRole());
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        AdminContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String headerToken = request.getHeader(properties.getTokenHeader());
        if (headerToken != null && !headerToken.isBlank()) {
            return headerToken;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authorization.substring(7);
        }
        return null;
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (actual == null) {
            return false;
        }
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }
}
