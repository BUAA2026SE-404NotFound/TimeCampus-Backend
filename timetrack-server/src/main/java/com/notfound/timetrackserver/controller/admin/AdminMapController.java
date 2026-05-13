package com.notfound.timetrackserver.controller.admin;

import com.notfound.timetrackcommon.api.ApiResponse;
import com.notfound.timetrackpojo.vo.AdminMapOverviewVO;
import com.notfound.timetrackserver.config.TencentMapProperties;
import com.notfound.timetrackserver.service.AdminMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Admin-Map", description = "管理员：地图运营总览")
@RestController
@RequestMapping("/api/v1/admin/map")
public class AdminMapController {

    private final AdminMapService adminMapService;
    private final TencentMapProperties tencentMapProperties;

    public AdminMapController(AdminMapService adminMapService, TencentMapProperties tencentMapProperties) {
        this.adminMapService = adminMapService;
        this.tencentMapProperties = tencentMapProperties;
    }

    @GetMapping("/overview")
    @Operation(summary = "地图运营总览", description = "返回所有 POI 及其收藏、评论、媒体等运营概览数据。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AdminMapOverviewVO> overview(@RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String commentStatus,
                                                    @RequestParam(required = false) Integer limit) {
        return ApiResponse.success(adminMapService.overview(status, keyword, commentStatus, limit));
    }

    @GetMapping("/config")
    @Operation(summary = "地图前端配置", description = "返回管理端地图渲染所需的腾讯地图 Key，不返回 SK。")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Map<String, String>> config() {
        return ApiResponse.success(Map.of("tencentMapKey", nullToBlank(tencentMapProperties.key())));
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
