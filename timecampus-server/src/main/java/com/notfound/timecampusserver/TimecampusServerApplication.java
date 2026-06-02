package com.notfound.timecampusserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.Map;

@SpringBootApplication(scanBasePackages = "com.notfound")
@ConfigurationPropertiesScan(basePackages = "com.notfound")
@MapperScan("com.notfound.timecampusserver.mapper")
public class TimecampusServerApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(TimecampusServerApplication.class);
        application.setDefaultProperties(Map.of(
                "spring.ai.mcp.server.enabled", env("TIMECAMPUS_MCP_ENABLED", "false"),
                "spring.ai.mcp.server.protocol", "STREAMABLE",
                "spring.ai.mcp.server.name", "timecampus-admin-mcp",
                "spring.ai.mcp.server.version", env("TIMECAMPUS_MCP_VERSION", "0.1.0-alpha"),
                "spring.ai.mcp.server.type", "SYNC",
                "spring.ai.mcp.server.instructions",
                "TimeCampus admin MCP server for POI, media, review and copy maintenance. Use read tools/resources before destructive writes.",
                "spring.ai.mcp.server.streamable-http.mcp-endpoint", env("TIMECAMPUS_MCP_ENDPOINT", "/mcp"),
                "spring.ai.mcp.server.streamable-http.disallow-delete", "true"
        ));
        application.run(args);
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
