package com.notfound.timecampusserver.controller.admin;

import com.notfound.timecampuscommon.api.ApiResponse;
import com.notfound.timecampuspojo.vo.AdminDashboardStatsVO;
import com.notfound.timecampusserver.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin-Dashboard", description = "管理员：仪表盘统计")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/stats")
    @Operation(summary = "仪表盘统计", description = "返回指标卡、近 14 天内容趋势、审核状态和媒体类型分布。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AdminDashboardStatsVO> stats() {
        return ApiResponse.success(adminDashboardService.stats());
    }
}
