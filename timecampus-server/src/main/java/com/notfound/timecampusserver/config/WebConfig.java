package com.notfound.timecampusserver.config;

import com.notfound.timecampusserver.security.AdminAuthInterceptor;
import com.notfound.timecampusserver.security.UserAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;
    private final UserAuthInterceptor userAuthInterceptor;

    public WebConfig(AdminAuthInterceptor adminAuthInterceptor, UserAuthInterceptor userAuthInterceptor) {
        this.adminAuthInterceptor = adminAuthInterceptor;
        this.userAuthInterceptor = userAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/v1/admin/**")
                .excludePathPatterns(
                        "/api/v1/admin/login",
                        "/api/v1/health",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/**"
                );

        registry.addInterceptor(userAuthInterceptor)
                .addPathPatterns(
                        "/api/v1/users/**",
                        "/api/v1/me",
                        "/api/v1/me/**",
                        "/api/v1/favorites/**",
                        "/api/v1/ugc",
                        "/api/v1/comments",
                        "/api/v1/comments/mine",
                        "/api/v1/contents/**",
                        "/api/v1/pois/*/contents",
                        "/api/v1/pois/*/official-contents",
                        "/api/v1/pois/*/time-switch",
                        "/api/v1/map/home",
                        "/api/v1/map/poi/*/timemachine",
                        "/api/v1/timeline"
                )
                .excludePathPatterns(
                        "/api/v1/auth/wechat/login",
                        "/api/v1/health",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/**"
                );
    }
}
