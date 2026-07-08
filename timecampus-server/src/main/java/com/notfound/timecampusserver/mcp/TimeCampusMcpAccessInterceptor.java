package com.notfound.timecampusserver.mcp;

import com.notfound.timecampusserver.security.AdminContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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
}
