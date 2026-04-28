package com.notfound.timetrackserver.controller;

import com.notfound.timetrackcommon.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@Tag(name = "Health", description = "服务健康检查")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    @Operation(summary = "健康检查", description = "用于探活与联调，返回服务状态与时间。")
    public ApiResponse<Map<String, Object>> ping() {
        return ApiResponse.success(Map.of(
                "service", "timetrack-server",
                "status", "UP",
                "time", OffsetDateTime.now().toString()
        ));
    }
}

