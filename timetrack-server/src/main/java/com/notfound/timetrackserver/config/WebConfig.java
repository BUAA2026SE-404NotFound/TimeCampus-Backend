package com.notfound.timetrackserver.config;

import com.notfound.timetrackserver.security.AdminAuthInterceptor;
import com.notfound.timetrackserver.security.UserAuthInterceptor;
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
                .addPathPatterns("/api/v1/users/**")
                .excludePathPatterns(
                        "/api/v1/users/wx-login",
                        "/api/v1/health",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/**"
                );
    }
}

