package com.notfound.timecampusserver.config;

import com.notfound.timecampusserver.security.AdminAuthInterceptor;
import com.notfound.timecampusserver.mcp.TimeCampusMcpAccessInterceptor;
import com.notfound.timecampusserver.mcp.TimeCampusMcpProperties;
import com.notfound.timecampusserver.security.UserAuthInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;
    private final UserAuthInterceptor userAuthInterceptor;
    private final TimeCampusMcpAccessInterceptor timeCampusMcpAccessInterceptor;
    private final TimeCampusMcpProperties timeCampusMcpProperties;
    private final Environment environment;

    public WebConfig(AdminAuthInterceptor adminAuthInterceptor,
                     UserAuthInterceptor userAuthInterceptor,
                     TimeCampusMcpAccessInterceptor timeCampusMcpAccessInterceptor,
                     TimeCampusMcpProperties timeCampusMcpProperties,
                     Environment environment) {
        this.adminAuthInterceptor = adminAuthInterceptor;
        this.userAuthInterceptor = userAuthInterceptor;
        this.timeCampusMcpAccessInterceptor = timeCampusMcpAccessInterceptor;
        this.timeCampusMcpProperties = timeCampusMcpProperties;
        this.environment = environment;
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String mcpEndpoint = normalizeEndpoint(environment.getProperty(
                "spring.ai.mcp.server.streamable-http.mcp-endpoint",
                timeCampusMcpProperties.getEndpoint()));
        registry.addInterceptor(timeCampusMcpAccessInterceptor)
                .addPathPatterns(mcpEndpoint, mcpEndpoint + "/**");

        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/v1/admin/**")
                .excludePathPatterns(
                        "/api/v1/admin/login",
                        "/api/v1/admin/register",
                        "/api/v1/admin/media",
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

    private String normalizeEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            return "/mcp";
        }
        return endpoint.startsWith("/") ? endpoint : "/" + endpoint;
    }
}
