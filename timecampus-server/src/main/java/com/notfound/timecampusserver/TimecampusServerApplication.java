package com.notfound.timecampusserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.Map;

import static java.util.Map.entry;

@SpringBootApplication(scanBasePackages = "com.notfound")
@ConfigurationPropertiesScan(basePackages = "com.notfound")
@MapperScan("com.notfound.timecampusserver.mapper")
public class TimecampusServerApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(TimecampusServerApplication.class);
        application.setDefaultProperties(Map.ofEntries(
                entry("spring.ai.mcp.server.enabled", env("TIMECAMPUS_MCP_ENABLED", "true")),
                entry("spring.ai.mcp.server.protocol", "STREAMABLE"),
                entry("spring.ai.mcp.server.name", "timecampus-admin-mcp"),
                entry("spring.ai.mcp.server.version", env("TIMECAMPUS_MCP_VERSION", "0.3.0-beta")),
                entry("spring.ai.mcp.server.type", "SYNC"),
                entry("spring.ai.mcp.server.instructions",
                        "TimeCampus admin MCP server for POI, media, review and copy maintenance. Use read tools/resources before destructive writes."),
                entry("spring.ai.mcp.server.streamable-http.mcp-endpoint", env("TIMECAMPUS_MCP_ENDPOINT", "/mcp")),
                entry("spring.ai.mcp.server.streamable-http.disallow-delete", "true"),
                entry("spring.ai.vectorstore.type", env("SPRING_AI_VECTORSTORE_TYPE", "none")),
                entry("timecampus.mcp.auth-required", env("TIMECAMPUS_MCP_AUTH_REQUIRED", "false")),
                entry("timecampus.mcp.token", env("TIMECAMPUS_MCP_TOKEN", "")),
                entry("timecampus.rag.vector-enabled", env("TIMECAMPUS_RAG_VECTOR_ENABLED", "false"))
        ));
        application.run(args);
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
