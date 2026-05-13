package com.notfound.timetrackserver.controller.admin;

import com.notfound.timetrackcommon.api.ApiResponse;
import com.notfound.timetrackpojo.entity.LogEntity;
import com.notfound.timetrackserver.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin-Log", description = "管理员：审计日志")
@RestController
@RequestMapping("/api/v1/admin/logs")
public class AdminLogController {

    private final LogService logService;

    public AdminLogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    @Operation(summary = "查询审计日志")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<LogEntity>> list(@RequestParam(required = false) String operatorType,
                                             @RequestParam(required = false) String type,
                                             @RequestParam(required = false) String action,
                                             @RequestParam(required = false) String targetType,
                                             @RequestParam(required = false) Integer limit) {
        return ApiResponse.success(logService.list(operatorType, type, action, targetType, limit));
    }
}
