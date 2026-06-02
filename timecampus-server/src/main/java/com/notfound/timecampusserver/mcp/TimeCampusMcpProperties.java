package com.notfound.timecampusserver.mcp;

import com.notfound.timecampuspojo.constant.AdminRoles;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "timecampus.mcp")
public class TimeCampusMcpProperties {

    private String endpoint = "/mcp";
    private boolean authRequired = true;
    private String tokenHeader = "X-TimeCampus-MCP-Token";
    private String token;
    private Long adminId = 1L;
    private String adminRole = AdminRoles.ADMIN;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            this.endpoint = "/mcp";
            return;
        }
        this.endpoint = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
    }

    public boolean isAuthRequired() {
        return authRequired;
    }

    public void setAuthRequired(boolean authRequired) {
        this.authRequired = authRequired;
    }

    public String getTokenHeader() {
        return tokenHeader;
    }

    public void setTokenHeader(String tokenHeader) {
        this.tokenHeader = tokenHeader == null || tokenHeader.isBlank()
                ? "X-TimeCampus-MCP-Token"
                : tokenHeader;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId == null ? 1L : adminId;
    }

    public String getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(String adminRole) {
        this.adminRole = adminRole == null || adminRole.isBlank() ? AdminRoles.ADMIN : adminRole;
    }
}
